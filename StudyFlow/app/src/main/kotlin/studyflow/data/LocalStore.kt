package studyflow.data

import studyflow.domain.model.FocusSession
import studyflow.domain.model.Habit
import studyflow.domain.model.Note
import studyflow.domain.model.Recurrence
import studyflow.domain.model.Subject
import studyflow.domain.model.StudyTask
import studyflow.domain.model.TaskPriority
import studyflow.domain.model.TaskStatus
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.Properties
import kotlin.io.path.exists
import kotlin.io.path.extension

class LocalStore(
    rootDir: Path = Paths.get(System.getProperty("user.home"), ".studyflow"),
    @Suppress("unused") private val forceAtomicMoveFailure: Boolean = false
) {
    val dataDir: Path = rootDir
    private val dbFile: Path = dataDir.resolve("studyflow.sqlite")
    private val rawBackupFile: Path = dataDir.resolve("studyflow_raw_backup.sqlite")
    private val legacyPropertiesFile: Path = dataDir.resolve("studyflow.properties")

    init {
        Files.createDirectories(dataDir)
        runCatching { Class.forName("org.sqlite.JDBC") }
    }

    fun exists(): Boolean = dbFile.exists() || legacyPropertiesFile.exists()

    fun save(
        subjects: List<Subject>,
        tasks: List<StudyTask>,
        notes: List<Note>,
        sessions: List<FocusSession>,
        habits: List<Habit> = emptyList()
    ) {
        withConnection(dbFile) { c ->
            createSchema(c)
            c.autoCommit = false
            runCatching {
                c.createStatement().use { st ->
                    st.executeUpdate("DELETE FROM habit_logs")
                    st.executeUpdate("DELETE FROM habits")
                    st.executeUpdate("DELETE FROM sessions")
                    st.executeUpdate("DELETE FROM notes")
                    st.executeUpdate("DELETE FROM tasks")
                    st.executeUpdate("DELETE FROM subjects")
                }
                insertSubjects(c, subjects)
                insertTasks(c, tasks)
                insertNotes(c, notes)
                insertSessions(c, sessions)
                insertHabits(c, habits)
                c.commit()
            }.getOrElse { e ->
                c.rollback()
                throw e
            }
        }
    }

    fun load(): StoreSnapshot? {
        if (dbFile.exists()) return loadSqlite(dbFile)
        if (legacyPropertiesFile.exists()) {
            val snapshot = loadLegacyProperties(legacyPropertiesFile) ?: return null
            save(snapshot.subjects, snapshot.tasks, snapshot.notes, snapshot.sessions, snapshot.habits)
            return snapshot
        }
        return null
    }

    fun loadFrom(path: Path): StoreSnapshot? {
        if (!path.exists()) return null
        return if (path.extension.equals("properties", ignoreCase = true)) {
            loadLegacyProperties(path)
        } else {
            loadSqlite(path)
        }
    }

    fun reset() {
        if (dbFile.exists()) Files.delete(dbFile)
    }

    fun exportRawBackup(): Path {
        if (!dbFile.exists()) {
            val snapshot = load() ?: throw IllegalStateException("Nothing to back up yet")
            save(snapshot.subjects, snapshot.tasks, snapshot.notes, snapshot.sessions, snapshot.habits)
        }
        Files.copy(dbFile, rawBackupFile, StandardCopyOption.REPLACE_EXISTING)
        return rawBackupFile
    }

    fun importRawBackup(): Boolean {
        if (!rawBackupFile.exists()) return false
        val snapshot = loadSqlite(rawBackupFile) ?: return false
        save(snapshot.subjects, snapshot.tasks, snapshot.notes, snapshot.sessions, snapshot.habits)
        return true
    }

    private fun withConnection(path: Path, block: (Connection) -> Unit) {
        Files.createDirectories(path.parent)
        DriverManager.getConnection("jdbc:sqlite:${path.toAbsolutePath()}").use { c ->
            c.createStatement().use { it.execute("PRAGMA foreign_keys = ON") }
            block(c)
        }
    }

    private fun loadSqlite(path: Path): StoreSnapshot? = runCatching {
        var result: StoreSnapshot? = null
        withConnection(path) { c ->
            createSchema(c)
            result = StoreSnapshot(
                subjects = loadSubjects(c),
                tasks = loadTasks(c),
                notes = loadNotes(c),
                sessions = loadSessions(c),
                habits = loadHabits(c)
            )
        }
        result
    }.getOrNull()

    private fun createSchema(c: Connection) {
        c.createStatement().use { st ->
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS subjects(
                    id INTEGER PRIMARY KEY,
                    name TEXT NOT NULL,
                    description TEXT NOT NULL,
                    color_hex TEXT NOT NULL,
                    icon TEXT NOT NULL,
                    created_at INTEGER NOT NULL
                )
            """.trimIndent())
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS tasks(
                    id INTEGER PRIMARY KEY,
                    subject_id INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT NOT NULL,
                    status TEXT NOT NULL,
                    priority TEXT NOT NULL,
                    deadline_at INTEGER,
                    estimated_minutes INTEGER,
                    spent_minutes INTEGER NOT NULL,
                    created_at INTEGER NOT NULL,
                    completed_at INTEGER,
                    recurrence TEXT NOT NULL DEFAULT 'None',
                    source_recurring_task_id INTEGER
                )
            """.trimIndent())
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS notes(
                    id INTEGER PRIMARY KEY,
                    subject_id INTEGER,
                    title TEXT NOT NULL,
                    content TEXT NOT NULL,
                    tags TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL
                )
            """.trimIndent())
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS sessions(
                    id INTEGER PRIMARY KEY,
                    task_id INTEGER,
                    subject_id INTEGER,
                    started_at INTEGER NOT NULL,
                    duration_minutes INTEGER NOT NULL
                )
            """.trimIndent())
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS habits(
                    id INTEGER PRIMARY KEY,
                    name TEXT NOT NULL,
                    description TEXT NOT NULL,
                    color_hex TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    archived INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent())
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS habit_logs(
                    habit_id INTEGER NOT NULL,
                    date TEXT NOT NULL,
                    PRIMARY KEY(habit_id, date)
                )
            """.trimIndent())
        }
        ensureColumn(c, "tasks", "recurrence", "TEXT NOT NULL DEFAULT 'None'")
        ensureColumn(c, "tasks", "source_recurring_task_id", "INTEGER")
    }

    private fun ensureColumn(c: Connection, table: String, column: String, definition: String) {
        val columns = mutableSetOf<String>()
        c.createStatement().use { st ->
            st.executeQuery("PRAGMA table_info($table)").use { rs ->
                while (rs.next()) columns += rs.getString("name")
            }
        }
        if (column !in columns) c.createStatement().use { it.executeUpdate("ALTER TABLE $table ADD COLUMN $column $definition") }
    }

    private fun insertSubjects(c: Connection, subjects: List<Subject>) {
        c.prepareStatement("INSERT INTO subjects(id,name,description,color_hex,icon,created_at) VALUES(?,?,?,?,?,?)").use { ps ->
            subjects.forEach { s ->
                ps.setLong(1, s.id); ps.setString(2, s.name); ps.setString(3, s.description); ps.setString(4, s.colorHex); ps.setString(5, s.icon); ps.setLong(6, s.createdAt); ps.addBatch()
            }
            ps.executeBatch()
        }
    }

    private fun insertTasks(c: Connection, tasks: List<StudyTask>) {
        c.prepareStatement("""
            INSERT INTO tasks(id,subject_id,title,description,status,priority,deadline_at,estimated_minutes,spent_minutes,created_at,completed_at,recurrence,source_recurring_task_id)
            VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)
        """.trimIndent()).use { ps ->
            tasks.forEach { t ->
                ps.setLong(1, t.id)
                ps.setLong(2, t.subjectId)
                ps.setString(3, t.title)
                ps.setString(4, t.description)
                ps.setString(5, t.status.name)
                ps.setString(6, t.priority.name)
                setNullableLong(ps, 7, t.deadlineAt)
                setNullableInt(ps, 8, t.estimatedMinutes)
                ps.setInt(9, t.spentMinutes)
                ps.setLong(10, t.createdAt)
                setNullableLong(ps, 11, t.completedAt)
                ps.setString(12, t.recurrence.name)
                setNullableLong(ps, 13, t.sourceRecurringTaskId)
                ps.addBatch()
            }
            ps.executeBatch()
        }
    }

    private fun insertNotes(c: Connection, notes: List<Note>) {
        c.prepareStatement("INSERT INTO notes(id,subject_id,title,content,tags,created_at,updated_at) VALUES(?,?,?,?,?,?,?)").use { ps ->
            notes.forEach { n ->
                ps.setLong(1, n.id)
                setNullableLong(ps, 2, n.subjectId)
                ps.setString(3, n.title)
                ps.setString(4, n.content)
                ps.setString(5, n.tags.joinToString("\u001F"))
                ps.setLong(6, n.createdAt)
                ps.setLong(7, n.updatedAt)
                ps.addBatch()
            }
            ps.executeBatch()
        }
    }

    private fun insertSessions(c: Connection, sessions: List<FocusSession>) {
        c.prepareStatement("INSERT INTO sessions(id,task_id,subject_id,started_at,duration_minutes) VALUES(?,?,?,?,?)").use { ps ->
            sessions.forEach { s ->
                ps.setLong(1, s.id)
                setNullableLong(ps, 2, s.taskId)
                setNullableLong(ps, 3, s.subjectId)
                ps.setLong(4, s.startedAt)
                ps.setInt(5, s.durationMinutes)
                ps.addBatch()
            }
            ps.executeBatch()
        }
    }

    private fun insertHabits(c: Connection, habits: List<Habit>) {
        c.prepareStatement("INSERT INTO habits(id,name,description,color_hex,created_at,archived) VALUES(?,?,?,?,?,?)").use { ps ->
            habits.forEach { h ->
                ps.setLong(1, h.id)
                ps.setString(2, h.name)
                ps.setString(3, h.description)
                ps.setString(4, h.colorHex)
                ps.setLong(5, h.createdAt)
                ps.setInt(6, if (h.archived) 1 else 0)
                ps.addBatch()
            }
            ps.executeBatch()
        }
        c.prepareStatement("INSERT INTO habit_logs(habit_id,date) VALUES(?,?)").use { ps ->
            habits.forEach { h ->
                h.completedDates.forEach { date ->
                    ps.setLong(1, h.id)
                    ps.setString(2, date)
                    ps.addBatch()
                }
            }
            ps.executeBatch()
        }
    }

    private fun loadSubjects(c: Connection): List<Subject> = query(c, "SELECT * FROM subjects ORDER BY id") { rs ->
        Subject(rs.getLong("id"), rs.getString("name"), rs.getString("description"), rs.getString("color_hex"), rs.getString("icon"), rs.getLong("created_at"))
    }

    private fun loadTasks(c: Connection): List<StudyTask> = query(c, "SELECT * FROM tasks ORDER BY id") { rs ->
        StudyTask(
            id = rs.getLong("id"),
            subjectId = rs.getLong("subject_id"),
            title = rs.getString("title"),
            description = rs.getString("description"),
            status = enumValue(rs.getString("status"), TaskStatus.Todo),
            priority = enumValue(rs.getString("priority"), TaskPriority.Medium),
            deadlineAt = nullableLong(rs, "deadline_at"),
            estimatedMinutes = nullableInt(rs, "estimated_minutes"),
            spentMinutes = rs.getInt("spent_minutes"),
            createdAt = rs.getLong("created_at"),
            completedAt = nullableLong(rs, "completed_at"),
            recurrence = enumValue(rs.getString("recurrence"), Recurrence.None),
            sourceRecurringTaskId = nullableLong(rs, "source_recurring_task_id")
        )
    }

    private fun loadNotes(c: Connection): List<Note> = query(c, "SELECT * FROM notes ORDER BY id") { rs ->
        val tags = rs.getString("tags").orEmpty().split('\u001F', ',', ';').map { it.trim() }.filter { it.isNotEmpty() }
        Note(rs.getLong("id"), nullableLong(rs, "subject_id"), rs.getString("title"), rs.getString("content"), tags, rs.getLong("created_at"), rs.getLong("updated_at"))
    }

    private fun loadSessions(c: Connection): List<FocusSession> = query(c, "SELECT * FROM sessions ORDER BY id") { rs ->
        FocusSession(rs.getLong("id"), nullableLong(rs, "task_id"), nullableLong(rs, "subject_id"), rs.getLong("started_at"), rs.getInt("duration_minutes"))
    }

    private fun loadHabits(c: Connection): List<Habit> {
        val logs = mutableMapOf<Long, MutableSet<String>>()
        query(c, "SELECT habit_id,date FROM habit_logs") { rs -> rs.getLong("habit_id") to rs.getString("date") }.forEach { (id, date) ->
            logs.getOrPut(id) { mutableSetOf() } += date
        }
        return query(c, "SELECT * FROM habits ORDER BY id") { rs ->
            val id = rs.getLong("id")
            Habit(id, rs.getString("name"), rs.getString("description"), rs.getString("color_hex"), rs.getLong("created_at"), rs.getInt("archived") != 0, logs[id].orEmpty())
        }
    }

    private fun <T> query(c: Connection, sql: String, mapper: (ResultSet) -> T): List<T> {
        val out = mutableListOf<T>()
        c.createStatement().use { st -> st.executeQuery(sql).use { rs -> while (rs.next()) out += mapper(rs) } }
        return out
    }

    private fun setNullableLong(ps: java.sql.PreparedStatement, index: Int, value: Long?) { if (value == null) ps.setNull(index, java.sql.Types.INTEGER) else ps.setLong(index, value) }
    private fun setNullableInt(ps: java.sql.PreparedStatement, index: Int, value: Int?) { if (value == null) ps.setNull(index, java.sql.Types.INTEGER) else ps.setInt(index, value) }
    private fun nullableLong(rs: ResultSet, column: String): Long? = rs.getLong(column).let { if (rs.wasNull()) null else it }
    private fun nullableInt(rs: ResultSet, column: String): Int? = rs.getInt(column).let { if (rs.wasNull()) null else it }

    private inline fun <reified T : Enum<T>> enumValue(value: String?, fallback: T): T = runCatching { enumValueOf<T>(value ?: fallback.name) }.getOrDefault(fallback)

    private fun loadLegacyProperties(path: Path): StoreSnapshot? {
        val p = Properties()
        return runCatching {
            Files.newInputStream(path).use { p.load(it) }
            StoreSnapshot(loadLegacySubjects(p), loadLegacyTasks(p), loadLegacyNotes(p), loadLegacySessions(p), emptyList())
        }.getOrNull()
    }

    private fun loadLegacySubjects(p: Properties): List<Subject> {
        val count = p.getProperty("subjects.count", "0").toIntOrNull() ?: 0
        return (0 until count).mapNotNull { i ->
            val id = p.getProperty("subject.$i.id")?.toLongOrNull() ?: return@mapNotNull null
            Subject(id, p.getProperty("subject.$i.name", "Subject"), p.getProperty("subject.$i.description", ""), p.getProperty("subject.$i.colorHex", "#7C3AED"), p.getProperty("subject.$i.icon", "•"), p.getProperty("subject.$i.createdAt", "0").toLongOrNull() ?: 0L)
        }
    }

    private fun loadLegacyTasks(p: Properties): List<StudyTask> {
        val count = p.getProperty("tasks.count", "0").toIntOrNull() ?: 0
        return (0 until count).mapNotNull { i ->
            val id = p.getProperty("task.$i.id")?.toLongOrNull() ?: return@mapNotNull null
            StudyTask(
                id = id,
                subjectId = p.getProperty("task.$i.subjectId", "0").toLongOrNull() ?: 0L,
                title = p.getProperty("task.$i.title", "Task"),
                description = p.getProperty("task.$i.description", ""),
                status = enumValue(p.getProperty("task.$i.status"), TaskStatus.Todo),
                priority = enumValue(p.getProperty("task.$i.priority"), TaskPriority.Medium),
                deadlineAt = p.getProperty("task.$i.deadlineAt").takeIf { !it.isNullOrBlank() }?.toLongOrNull(),
                estimatedMinutes = p.getProperty("task.$i.estimatedMinutes").takeIf { !it.isNullOrBlank() }?.toIntOrNull(),
                spentMinutes = p.getProperty("task.$i.spentMinutes", "0").toIntOrNull() ?: 0,
                createdAt = p.getProperty("task.$i.createdAt", "0").toLongOrNull() ?: 0L,
                completedAt = p.getProperty("task.$i.completedAt").takeIf { !it.isNullOrBlank() }?.toLongOrNull(),
                recurrence = enumValue(p.getProperty("task.$i.recurrence"), Recurrence.None),
                sourceRecurringTaskId = p.getProperty("task.$i.sourceRecurringTaskId").takeIf { !it.isNullOrBlank() }?.toLongOrNull()
            )
        }
    }

    private fun loadLegacyNotes(p: Properties): List<Note> {
        val count = p.getProperty("notes.count", "0").toIntOrNull() ?: 0
        return (0 until count).mapNotNull { i ->
            val id = p.getProperty("note.$i.id")?.toLongOrNull() ?: return@mapNotNull null
            Note(id, p.getProperty("note.$i.subjectId").takeIf { !it.isNullOrBlank() }?.toLongOrNull(), p.getProperty("note.$i.title", "Note"), p.getProperty("note.$i.content", ""), p.getProperty("note.$i.tags", "").split(',', ';').map { it.trim() }.filter { it.isNotEmpty() }, p.getProperty("note.$i.createdAt", "0").toLongOrNull() ?: 0L, p.getProperty("note.$i.updatedAt", "0").toLongOrNull() ?: 0L)
        }
    }

    private fun loadLegacySessions(p: Properties): List<FocusSession> {
        val count = p.getProperty("sessions.count", "0").toIntOrNull() ?: 0
        return (0 until count).mapNotNull { i ->
            val id = p.getProperty("session.$i.id")?.toLongOrNull() ?: return@mapNotNull null
            FocusSession(id, p.getProperty("session.$i.taskId").takeIf { !it.isNullOrBlank() }?.toLongOrNull(), p.getProperty("session.$i.subjectId").takeIf { !it.isNullOrBlank() }?.toLongOrNull(), p.getProperty("session.$i.startedAt", "0").toLongOrNull() ?: 0L, p.getProperty("session.$i.durationMinutes", "0").toIntOrNull() ?: 0)
        }
    }
}

data class StoreSnapshot(
    val subjects: List<Subject>,
    val tasks: List<StudyTask>,
    val notes: List<Note>,
    val sessions: List<FocusSession>,
    val habits: List<Habit> = emptyList()
)
