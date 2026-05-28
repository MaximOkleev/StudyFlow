
package studyflow.web

import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import studyflow.data.LocalStore
import studyflow.data.SeedData
import studyflow.data.StoreSnapshot
import studyflow.domain.model.Exam
import studyflow.domain.model.FocusSession
import studyflow.domain.model.Habit
import studyflow.domain.model.Note
import studyflow.domain.model.Recurrence
import studyflow.domain.model.Subject
import studyflow.domain.model.StudyTask
import studyflow.domain.model.TaskPriority
import studyflow.domain.model.TaskStatus
import studyflow.util.DateUtils
import java.awt.Desktop
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread
import kotlin.math.max

/**
 * Local HTTP server for the web UI.
 *
 * It serves StudyFlowWeb at http://127.0.0.1:5173 and reads/writes the same
 * SQLite database as the desktop app: %USERPROFILE%/.studyflow/studyflow.sqlite.
 */
fun main(args: Array<String>) {
    val port = args.firstOrNull { it.startsWith("--port=") }?.substringAfter("=")?.toIntOrNull() ?: 5173
    val staticRoot = findStaticRoot()
    val store = LocalStore()
    ensureInitialDatabase(store)

    val server = HttpServer.create(InetSocketAddress("127.0.0.1", port), 0)
    server.createContext("/api/health") { exchange ->
        exchange.addCors()
        if (exchange.requestMethod == "OPTIONS") return@createContext exchange.sendText(204, "")
        exchange.sendJson(200, "{\"ok\":true,\"mode\":\"sqlite\"}")
    }
    server.createContext("/api/snapshot") { exchange ->
        exchange.addCors()
        if (exchange.requestMethod == "OPTIONS") return@createContext exchange.sendText(204, "")
        try {
            when (exchange.requestMethod.uppercase()) {
                "GET" -> {
                    val snapshot = store.load() ?: ensureInitialDatabase(store)
                    exchange.sendJson(200, snapshot.toWebJson())
                }
                "POST" -> {
                    val body = exchange.requestBody.readBytes().toString(StandardCharsets.UTF_8)
                    val value = SimpleJson.parse(body) as? Map<*, *> ?: error("Root JSON must be an object")
                    val snapshot = webSnapshotToStore(value)
                    store.save(snapshot.subjects, snapshot.tasks, snapshot.notes, snapshot.sessions, snapshot.habits, snapshot.exams)
                    exchange.sendJson(200, (store.load() ?: snapshot).toWebJson())
                }
                else -> exchange.sendText(405, "Method not allowed")
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            exchange.sendJson(400, "{\"ok\":false,\"error\":${jsonString(e.message ?: e::class.simpleName ?: "Unknown error")}}")
        }
    }
    server.createContext("/") { exchange ->
        exchange.addCors()
        if (exchange.requestMethod == "OPTIONS") return@createContext exchange.sendText(204, "")
        serveStatic(exchange, staticRoot)
    }
    server.executor = java.util.concurrent.Executors.newCachedThreadPool()
    server.start()

    val url = "http://127.0.0.1:$port/"
    println("StudyFlow Web is running: $url")
    println("Static files: $staticRoot")
    println("Shared SQLite database: ${Paths.get(System.getProperty("user.home"), ".studyflow", "studyflow.sqlite")}")

    runCatching {
        if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(java.net.URI(url))
    }

    Runtime.getRuntime().addShutdownHook(Thread { server.stop(0) })
    while (true) Thread.sleep(60_000)
}

private fun findStaticRoot(): Path {
    val userDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize()
    val candidates = listOf(
        userDir.resolve("StudyFlowWeb"),
        userDir.parent?.resolve("StudyFlowWeb"),
        userDir.resolve("../StudyFlowWeb").normalize(),
        Paths.get("StudyFlowWeb").toAbsolutePath().normalize()
    ).filterNotNull()
    return candidates.firstOrNull { Files.exists(it.resolve("index.html")) }
        ?: error("StudyFlowWeb/index.html not found. Run this task from the StudyFlow project root.")
}

private fun ensureInitialDatabase(store: LocalStore): StoreSnapshot {
    val existing = store.load()
    if (existing != null) return existing
    val subjects = SeedData.subjects()
    val exams = SeedData.exams(subjects)
    val snapshot = StoreSnapshot(subjects, emptyList(), emptyList(), emptyList(), emptyList(), exams)
    store.save(snapshot.subjects, snapshot.tasks, snapshot.notes, snapshot.sessions, snapshot.habits, snapshot.exams)
    return snapshot
}

private fun serveStatic(exchange: HttpExchange, staticRoot: Path) {
    val rawPath = exchange.requestURI.path.substringAfter('/').ifBlank { "index.html" }
    val decoded = URLDecoder.decode(rawPath, StandardCharsets.UTF_8)
    val target = staticRoot.resolve(decoded).normalize()
    if (!target.startsWith(staticRoot.normalize()) || !Files.exists(target) || Files.isDirectory(target)) {
        return exchange.sendText(404, "Not found")
    }
    val bytes = Files.readAllBytes(target)
    exchange.responseHeaders.set("Content-Type", mime(target))
    exchange.sendResponseHeaders(200, bytes.size.toLong())
    exchange.responseBody.use { it.write(bytes) }
}

private fun mime(path: Path): String = when (path.fileName.toString().substringAfterLast('.', "").lowercase()) {
    "html" -> "text/html; charset=utf-8"
    "css" -> "text/css; charset=utf-8"
    "js" -> "application/javascript; charset=utf-8"
    "json" -> "application/json; charset=utf-8"
    "csv" -> "text/csv; charset=utf-8"
    "svg" -> "image/svg+xml"
    "png" -> "image/png"
    else -> "application/octet-stream"
}

private fun HttpExchange.addCors() {
    responseHeaders.set("Access-Control-Allow-Origin", "*")
    responseHeaders.set("Access-Control-Allow-Methods", "GET,POST,OPTIONS")
    responseHeaders.set("Access-Control-Allow-Headers", "Content-Type")
}

private fun HttpExchange.sendJson(code: Int, text: String) {
    responseHeaders.set("Content-Type", "application/json; charset=utf-8")
    sendText(code, text)
}

private fun HttpExchange.sendText(code: Int, text: String) {
    val bytes = text.toByteArray(StandardCharsets.UTF_8)
    sendResponseHeaders(code, bytes.size.toLong())
    responseBody.use { it.write(bytes) }
}

private val zone: ZoneId = ZoneId.systemDefault()
private fun millisToIsoDate(millis: Long?): String = millis?.let { DateUtils.millisToDate(it).toString() } ?: ""
private fun millisToIsoDateTime(millis: Long?): String = millis?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDateTime().toString() } ?: ""
private fun parseDateMillis(text: String?): Long? = text?.takeIf { it.isNotBlank() }?.let { DateUtils.parseIsoDateToMillis(it.take(10)) }
private fun parseDateTimeMillis(text: String?): Long {
    if (text.isNullOrBlank()) return DateUtils.nowMillis()
    val cleaned = text.trim()
    return runCatching {
        if (cleaned.endsWith("Z")) Instant.parse(cleaned).toEpochMilli()
        else LocalDateTime.parse(if (cleaned.length == 16) "$cleaned:00" else cleaned).atZone(zone).toInstant().toEpochMilli()
    }.recoverCatching {
        LocalDate.parse(cleaned.take(10)).atStartOfDay(zone).toInstant().toEpochMilli()
    }.getOrDefault(DateUtils.nowMillis())
}

private fun StoreSnapshot.toWebJson(): String {
    val subjectNames = subjects.associate { it.id to it.name }
    return buildString {
        append('{')
        append("\"subjects\":"); appendArray(subjects) { s ->
            "{\"id\":${s.id},\"name\":${jsonString(s.name)},\"description\":${jsonString(s.description)},\"color\":${jsonString(s.colorHex)},\"icon\":${jsonString(s.icon)}}"
        }
        append(',')
        append("\"exams\":"); appendArray(exams.sortedBy { it.startAt }) { e ->
            "{\"id\":${e.id},\"subject\":${jsonString(e.subjectName)},\"start\":${jsonString(millisToIsoDateTime(e.startAt))},\"end\":${jsonString(millisToIsoDateTime(e.endAt))},\"teachers\":${jsonString(e.teachers)},\"location\":${jsonString(e.location)}}"
        }
        append(',')
        append("\"tasks\":"); appendArray(tasks) { t ->
            "{\"id\":${jsonString(t.id.toString())},\"subject\":${jsonString(subjectNames[t.subjectId].orEmpty())},\"title\":${jsonString(t.title)},\"description\":${jsonString(t.description)},\"status\":${jsonString(t.status.web)},\"priority\":${jsonString(t.priority.web)},\"deadline\":${jsonString(millisToIsoDate(t.deadlineAt))},\"estimatedMinutes\":${t.estimatedMinutes ?: 0},\"spentMinutes\":${t.spentMinutes},\"recurrence\":${jsonString(t.recurrence.web)},\"createdAt\":${jsonString(millisToIsoDateTime(t.createdAt))},\"completedAt\":${nullableJsonString(millisToIsoDateTime(t.completedAt))}}"
        }
        append(',')
        append("\"notes\":"); appendArray(notes) { n ->
            "{\"id\":${jsonString(n.id.toString())},\"subject\":${jsonString(n.subjectId?.let { subjectNames[it] }.orEmpty())},\"title\":${jsonString(n.title)},\"body\":${jsonString(n.content)},\"tags\":${jsonString(n.tags.joinToString(","))},\"createdAt\":${jsonString(millisToIsoDateTime(n.createdAt))}}"
        }
        append(',')
        append("\"habits\":"); appendArray(habits) { h ->
            "{\"id\":${jsonString(h.id.toString())},\"title\":${jsonString(h.name)},\"subject\":${jsonString(h.description)},\"history\":${h.completedDates.sorted().joinToString(prefix="[", postfix="]") { jsonString(it) }}}"
        }
        append(',')
        append("\"focusSessions\":"); appendArray(sessions) { f ->
            "{\"id\":${jsonString(f.id.toString())},\"taskId\":${nullableJsonString(f.taskId?.toString())},\"subject\":${jsonString(f.subjectId?.let { subjectNames[it] }.orEmpty())},\"startedAt\":${jsonString(millisToIsoDateTime(f.startedAt))},\"minutes\":${f.durationMinutes}}"
        }
        append(',')
        append("\"settings\":{\"timerMinutes\":25,\"storage\":\"sqlite\"}")
        append('}')
    }
}

private fun <T> StringBuilder.appendArray(list: Iterable<T>, item: (T) -> String) {
    append('[')
    var first = true
    for (x in list) {
        if (!first) append(',')
        append(item(x))
        first = false
    }
    append(']')
}

@Suppress("UNCHECKED_CAST")
private fun webSnapshotToStore(root: Map<*, *>): StoreSnapshot {
    val now = DateUtils.nowMillis()
    val subjects = arr(root["subjects"]).mapIndexed { idx, m ->
        val id = long(m["id"]) ?: (idx + 1L)
        Subject(
            id = id,
            name = str(m["name"]).ifBlank { "Subject $id" },
            description = str(m["description"]),
            colorHex = str(m["color"]).ifBlank { str(m["colorHex"]).ifBlank { "#60A5FA" } },
            icon = str(m["icon"]).ifBlank { "SF" },
            createdAt = now
        )
    }
    val subjectByName = subjects.associateBy { it.name }
    val subjectById = subjects.associateBy { it.id }
    fun subjectIdByName(name: String): Long? = subjectByName[name]?.id ?: subjects.firstOrNull { it.name.equals(name, ignoreCase = true) }?.id
    fun defaultSubjectId() = subjects.firstOrNull()?.id ?: 1L

    val tasks = arr(root["tasks"]).mapIndexed { idx, m ->
        val id = long(m["id"]) ?: (idx + 1L)
        val subjectName = str(m["subject"])
        StudyTask(
            id = id,
            subjectId = subjectIdByName(subjectName) ?: defaultSubjectId(),
            title = str(m["title"]).ifBlank { "Task $id" },
            description = str(m["description"]),
            status = taskStatus(str(m["status"])),
            priority = taskPriority(str(m["priority"])),
            deadlineAt = parseDateMillis(str(m["deadline"])),
            estimatedMinutes = int(m["estimatedMinutes"]).takeIf { it > 0 },
            spentMinutes = int(m["spentMinutes"]),
            createdAt = parseDateTimeMillis(str(m["createdAt"]).ifBlank { null }),
            completedAt = str(m["completedAt"]).takeIf { it.isNotBlank() }?.let { parseDateTimeMillis(it) },
            recurrence = recurrence(str(m["recurrence"])),
            sourceRecurringTaskId = long(m["sourceRecurringTaskId"])
        )
    }
    val tasksById = tasks.associateBy { it.id }

    val notes = arr(root["notes"]).mapIndexed { idx, m ->
        val id = long(m["id"]) ?: (idx + 1L)
        val subjectId = subjectIdByName(str(m["subject"]))
        val createdAt = parseDateTimeMillis(str(m["createdAt"]).ifBlank { null })
        Note(
            id = id,
            subjectId = subjectId,
            title = str(m["title"]).ifBlank { "Note $id" },
            content = str(m["body"]).ifBlank { str(m["content"]) },
            tags = str(m["tags"]).split(',', ';', '#').map { it.trim() }.filter { it.isNotBlank() },
            createdAt = createdAt,
            updatedAt = createdAt
        )
    }

    val habits = arr(root["habits"]).mapIndexed { idx, m ->
        val id = long(m["id"]) ?: (idx + 1L)
        Habit(
            id = id,
            name = str(m["title"]).ifBlank { str(m["name"]).ifBlank { "Habit $id" } },
            description = str(m["subject"]).ifBlank { str(m["description"]) },
            colorHex = str(m["color"]).ifBlank { str(m["colorHex"]).ifBlank { "#22C55E" } },
            createdAt = now,
            completedDates = list(m["history"]).map { str(it) }.filter { it.isNotBlank() }.toSet()
        )
    }

    val sessions = arr(root["focusSessions"]).mapIndexed { idx, m ->
        val id = long(m["id"]) ?: (idx + 1L)
        val taskId = long(m["taskId"]).takeIf { it == null || it in tasksById }
        FocusSession(
            id = id,
            taskId = taskId,
            subjectId = subjectIdByName(str(m["subject"])),
            startedAt = parseDateTimeMillis(str(m["startedAt"]).ifBlank { null }),
            durationMinutes = max(0, int(m["minutes"]).takeIf { it > 0 } ?: int(m["durationMinutes"]))
        )
    }

    val exams = arr(root["exams"]).mapIndexed { idx, m ->
        val id = long(m["id"]) ?: (idx + 1L)
        val subjectName = str(m["subject"]).ifBlank { str(m["subjectName"]) }
        val sid = subjectIdByName(subjectName)
        Exam(
            id = id,
            subjectId = sid,
            subjectName = subjectName.ifBlank { sid?.let { subjectById[it]?.name }.orEmpty() },
            type = "",
            teachers = str(m["teachers"]),
            startAt = parseDateTimeMillis(str(m["start"]).ifBlank { null }),
            endAt = parseDateTimeMillis(str(m["end"]).ifBlank { null }),
            location = str(m["location"]).ifBlank { str(m["room"]) },
            createdAt = now
        )
    }

    return StoreSnapshot(subjects, tasks, notes, sessions, habits, exams)
}

private fun arr(value: Any?): List<Map<*, *>> = (value as? List<*>)?.mapNotNull { it as? Map<*, *> }.orEmpty()
private fun list(value: Any?): List<Any?> = value as? List<*> ?: emptyList()
private fun str(value: Any?): String = when (value) { null -> ""; is String -> value; else -> value.toString() }
private fun int(value: Any?): Int = when (value) { is Number -> value.toInt(); is String -> value.toDoubleOrNull()?.toInt() ?: 0; else -> 0 }
private fun long(value: Any?): Long? = when (value) { is Number -> value.toLong(); is String -> value.toLongOrNull() ?: value.filter { it.isDigit() }.takeIf { it.isNotBlank() }?.toLongOrNull(); else -> null }

private val TaskStatus.web: String get() = when (this) { TaskStatus.Todo -> "todo"; TaskStatus.InProgress -> "progress"; TaskStatus.Done -> "done" }
private val TaskPriority.web: String get() = name.lowercase()
private val Recurrence.web: String get() = name.lowercase()
private fun taskStatus(v: String): TaskStatus = when (v.lowercase()) { "progress", "inprogress", "in progress" -> TaskStatus.InProgress; "done" -> TaskStatus.Done; else -> TaskStatus.Todo }
private fun taskPriority(v: String): TaskPriority = when (v.lowercase()) { "low" -> TaskPriority.Low; "high" -> TaskPriority.High; else -> TaskPriority.Medium }
private fun recurrence(v: String): Recurrence = when (v.lowercase()) { "daily" -> Recurrence.Daily; "weekly" -> Recurrence.Weekly; "monthly" -> Recurrence.Monthly; else -> Recurrence.None }

private fun jsonString(value: String): String = buildString {
    append('"')
    value.forEach { ch ->
        when (ch) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> if (ch.code < 32) append("\\u%04x".format(ch.code)) else append(ch)
        }
    }
    append('"')
}
private fun nullableJsonString(value: String?): String = if (value.isNullOrBlank()) "null" else jsonString(value)

/** Minimal JSON parser for trusted StudyFlow snapshot payloads. */
private object SimpleJson {
    fun parse(text: String): Any? = Parser(text).parse()

    private class Parser(private val s: String) {
        private var i = 0
        fun parse(): Any? { skip(); val v = value(); skip(); return v }
        private fun value(): Any? {
            skip()
            return when (peek()) {
                '{' -> obj()
                '[' -> arr()
                '"' -> string()
                't' -> { expect("true"); true }
                'f' -> { expect("false"); false }
                'n' -> { expect("null"); null }
                else -> number()
            }
        }
        private fun obj(): Map<String, Any?> {
            val m = linkedMapOf<String, Any?>(); take('{'); skip(); if (peek() == '}') { take('}'); return m }
            while (true) { skip(); val k = string(); skip(); take(':'); m[k] = value(); skip(); if (peek() == '}') { take('}'); break }; take(',') }
            return m
        }
        private fun arr(): List<Any?> {
            val out = mutableListOf<Any?>(); take('['); skip(); if (peek() == ']') { take(']'); return out }
            while (true) { out += value(); skip(); if (peek() == ']') { take(']'); break }; take(',') }
            return out
        }
        private fun string(): String {
            take('"'); val out = StringBuilder()
            while (i < s.length) { val ch = s[i++]; if (ch == '"') break; if (ch == '\\') { val e = s[i++]; out.append(when (e) { '"' -> '"'; '\\' -> '\\'; '/' -> '/'; 'b' -> '\b'; 'f' -> '\u000C'; 'n' -> '\n'; 'r' -> '\r'; 't' -> '\t'; 'u' -> s.substring(i, i + 4).toInt(16).toChar().also { i += 4 }; else -> e }) } else out.append(ch) }
            return out.toString()
        }
        private fun number(): Number {
            val start = i
            while (i < s.length && s[i] !in listOf(',', '}', ']', ' ', '\n', '\r', '\t')) i++
            val raw = s.substring(start, i)
            return if (raw.contains('.') || raw.contains('e', true)) raw.toDouble() else raw.toLong()
        }
        private fun expect(x: String) { require(s.startsWith(x, i)); i += x.length }
        private fun take(ch: Char) { skip(); require(peek() == ch) { "Expected '$ch' at $i" }; i++ }
        private fun peek(): Char = if (i < s.length) s[i] else '\u0000'
        private fun skip() { while (i < s.length && s[i].isWhitespace()) i++ }
    }
}
