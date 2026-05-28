package studyflow.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import studyflow.domain.model.FocusSession
import studyflow.domain.model.Habit
import studyflow.domain.model.Note
import studyflow.domain.model.Recurrence
import studyflow.domain.model.Subject
import studyflow.domain.model.StudyTask
import studyflow.domain.model.TaskPriority
import studyflow.domain.model.TaskStatus
import studyflow.util.DateUtils
import studyflow.util.safeColorHex
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class StudyRepository(private val store: LocalStore = LocalStore()) {
    var subjects by mutableStateOf<List<Subject>>(emptyList())
        private set
    var tasks by mutableStateOf<List<StudyTask>>(emptyList())
        private set
    var notes by mutableStateOf<List<Note>>(emptyList())
        private set
    var focusSessions by mutableStateOf<List<FocusSession>>(emptyList())
        private set
    var habits by mutableStateOf<List<Habit>>(emptyList())
        private set
    var lastMessage by mutableStateOf("SQLite database: ${store.dataDir.resolve("studyflow.sqlite")}")
        private set

    private var nextSubjectId = 1L
    private var nextTaskId = 1L
    private var nextNoteId = 1L
    private var nextSessionId = 1L
    private var nextHabitId = 1L

    init {
        val snapshot = store.load()
        if (snapshot == null || snapshot.subjects.isEmpty()) {
            subjects = SeedData.subjects()
            tasks = SeedData.tasks()
            notes = SeedData.notes()
            focusSessions = SeedData.focusSessions()
            habits = SeedData.habits()
            save("Demo data created")
        } else {
            subjects = snapshot.subjects
            tasks = snapshot.tasks
            notes = snapshot.notes
            focusSessions = snapshot.sessions
            habits = snapshot.habits
        }
        recalcIds()
    }

    fun subjectById(id: Long): Subject? = subjects.firstOrNull { it.id == id }
    fun taskById(id: Long): StudyTask? = tasks.firstOrNull { it.id == id }
    // habitById removed as it was unused
    fun tasksForSubject(subjectId: Long): List<StudyTask> = tasks.filter { it.subjectId == subjectId }
    fun notesForSubject(subjectId: Long): List<Note> = notes.filter { it.subjectId == subjectId }
    fun subjectName(id: Long?): String = id?.let { subjectById(it)?.name } ?: "No subject"

    fun addSubject(name: String, description: String, colorHex: String, icon: String) {
        if (name.isBlank()) return
        subjects = subjects + Subject(nextSubjectId++, name.trim(), description.trim(), safeColorHex(colorHex), icon.ifBlank { "•" }, DateUtils.nowMillis())
        save("Subject added")
    }

    fun updateSubject(subject: Subject, name: String, description: String, colorHex: String, icon: String) {
        subjects = subjects.map { if (it.id == subject.id) it.copy(name = name.trim().ifBlank { it.name }, description = description.trim(), colorHex = safeColorHex(colorHex), icon = icon.ifBlank { "•" }) else it }
        save("Subject updated")
    }

    fun deleteSubject(id: Long) {
        subjects = subjects.filterNot { it.id == id }
        tasks = tasks.filterNot { it.subjectId == id }
        notes = notes.map { if (it.subjectId == id) it.copy(subjectId = null) else it }
        focusSessions = focusSessions.map { if (it.subjectId == id) it.copy(subjectId = null, taskId = null) else it }
        save("Subject deleted")
    }

    fun addTask(subjectId: Long, title: String, description: String, priority: TaskPriority, deadlineDaysFromNow: Int?, estimatedMinutes: Int?) {
        addTaskWithDeadline(subjectId, title, description, priority, deadlineDaysFromNow?.let { DateUtils.daysFromNow(it.toLong()) }, estimatedMinutes)
    }

    fun addTaskWithDeadline(
        subjectId: Long,
        title: String,
        description: String,
        priority: TaskPriority,
        deadlineAt: Long?,
        estimatedMinutes: Int?,
        recurrence: Recurrence = Recurrence.None
    ) {
        if (title.isBlank() || subjects.none { it.id == subjectId }) return
        tasks = tasks + StudyTask(
            id = nextTaskId++,
            subjectId = subjectId,
            title = title.trim(),
            description = description.trim(),
            status = TaskStatus.Todo,
            priority = priority,
            deadlineAt = deadlineAt,
            estimatedMinutes = estimatedMinutes,
            spentMinutes = 0,
            createdAt = DateUtils.nowMillis(),
            completedAt = null,
            recurrence = recurrence,
            sourceRecurringTaskId = null
        )
        save("Task added")
    }

    fun updateTask(task: StudyTask, subjectId: Long, title: String, description: String, status: TaskStatus, priority: TaskPriority, deadlineDaysFromNow: Int?, estimatedMinutes: Int?) {
        updateTaskWithDeadline(task, subjectId, title, description, status, priority, deadlineDaysFromNow?.let { DateUtils.daysFromNow(it.toLong()) }, estimatedMinutes, task.recurrence)
    }

    fun updateTaskWithDeadline(
        task: StudyTask,
        subjectId: Long,
        title: String,
        description: String,
        status: TaskStatus,
        priority: TaskPriority,
        deadlineAt: Long?,
        estimatedMinutes: Int?,
        recurrence: Recurrence = task.recurrence
    ) {
        if (subjects.none { it.id == subjectId }) return
        val completed = if (status == TaskStatus.Done && task.completedAt == null) DateUtils.nowMillis() else if (status != TaskStatus.Done) null else task.completedAt
        var completedTask: StudyTask? = null
        tasks = tasks.map {
            if (it.id == task.id) {
                val updated = it.copy(
                    subjectId = subjectId,
                    title = title.trim().ifBlank { it.title },
                    description = description.trim(),
                    status = status,
                    priority = priority,
                    deadlineAt = deadlineAt,
                    estimatedMinutes = estimatedMinutes,
                    completedAt = completed,
                    recurrence = recurrence
                )
                completedTask = if (status == TaskStatus.Done && it.status != TaskStatus.Done) updated else null
                updated
            } else it
        }
        completedTask?.let { appendNextRecurringTask(it) }
        save("Task updated")
    }

    fun updateTaskStatus(taskId: Long, status: TaskStatus) {
        val original = taskById(taskId) ?: return
        if (original.status == status) return
        val completedAt = if (status == TaskStatus.Done) DateUtils.nowMillis() else null
        val updated = original.copy(status = status, completedAt = completedAt)
        tasks = tasks.map { if (it.id == taskId) updated else it }
        if (status == TaskStatus.Done) appendNextRecurringTask(updated)
        save("Task status changed")
    }

    fun deleteTask(id: Long) {
        tasks = tasks.filterNot { it.id == id }
        focusSessions = focusSessions.map { if (it.taskId == id) it.copy(taskId = null) else it }
        save("Task deleted")
    }

    fun cycleTaskStatus(taskId: Long) {
        val task = taskById(taskId) ?: return
        updateTaskStatus(taskId, task.status.next())
    }

    fun addSpentMinutes(taskId: Long, minutes: Int) {
        tasks = tasks.map { if (it.id == taskId) it.copy(spentMinutes = (it.spentMinutes + minutes).coerceAtLeast(0)) else it }
        save("Time added to task")
    }

    fun addNote(subjectId: Long?, title: String, content: String, tagsText: String) {
        if (title.isBlank()) return
        notes = notes + Note(nextNoteId++, subjectId, title.trim(), content.trim(), parseTags(tagsText), DateUtils.nowMillis(), DateUtils.nowMillis())
        save("Note added")
    }

    fun updateNote(note: Note, subjectId: Long?, title: String, content: String, tagsText: String) {
        notes = notes.map { if (it.id == note.id) it.copy(subjectId = subjectId, title = title.trim().ifBlank { it.title }, content = content.trim(), tags = parseTags(tagsText), updatedAt = DateUtils.nowMillis()) else it }
        save("Note updated")
    }

    fun deleteNote(id: Long) {
        notes = notes.filterNot { it.id == id }
        save("Note deleted")
    }

    fun logFocusSession(taskId: Long?, subjectId: Long?, minutes: Int) {
        if (minutes <= 0) return
        focusSessions = focusSessions + FocusSession(nextSessionId++, taskId, subjectId, DateUtils.nowMillis(), minutes)
        if (taskId != null) addSpentMinutes(taskId, minutes) else save("Focus session logged")
    }

    fun addHabit(name: String, description: String, colorHex: String) {
        if (name.isBlank()) return
        habits = habits + Habit(nextHabitId++, name.trim(), description.trim(), safeColorHex(colorHex), DateUtils.nowMillis())
        save("Habit added")
    }

    fun updateHabit(habit: Habit, name: String, description: String, colorHex: String) {
        habits = habits.map { if (it.id == habit.id) it.copy(name = name.trim().ifBlank { it.name }, description = description.trim(), colorHex = safeColorHex(colorHex)) else it }
        save("Habit updated")
    }

    fun deleteHabit(id: Long) {
        habits = habits.filterNot { it.id == id }
        save("Habit deleted")
    }

    fun toggleHabitToday(id: Long) = toggleHabitDate(id, DateUtils.today().toString())

    fun toggleHabitDate(id: Long, date: String) {
        habits = habits.map { habit ->
            if (habit.id != id) habit else {
                val dates = habit.completedDates.toMutableSet()
                if (!dates.add(date)) dates.remove(date)
                habit.copy(completedDates = dates)
            }
        }
        save("Habit updated")
    }

    fun habitCompletedToday(habit: Habit): Boolean = DateUtils.today().toString() in habit.completedDates

    fun habitStreak(habit: Habit): Int {
        var streak = 0
        var day = DateUtils.today()
        while (day.toString() in habit.completedDates) {
            streak++
            day = day.minusDays(1)
        }
        return streak
    }

    fun resetDemoData() {
        store.reset()
        subjects = SeedData.subjects()
        tasks = SeedData.tasks()
        notes = SeedData.notes()
        focusSessions = SeedData.focusSessions()
        habits = SeedData.habits()
        recalcIds()
        save("Demo data reset")
    }

    fun saveNow() = save("Saved")

    fun totalFocusMinutes(): Int = focusSessions.sumOf { it.durationMinutes }
    fun doneTasks(): Int = tasks.count { it.status == TaskStatus.Done }
    fun activeTasks(): Int = tasks.count { it.status != TaskStatus.Done }
    fun overdueTasks(): Int = tasks.count { it.status != TaskStatus.Done && DateUtils.isOverdue(it.deadlineAt) }
    fun todayTasks(): List<StudyTask> = tasks.filter { DateUtils.isToday(it.deadlineAt) && it.status != TaskStatus.Done }
    fun upcomingTasks(): List<StudyTask> = tasks.filter { it.status != TaskStatus.Done && it.deadlineAt != null }.sortedBy { it.deadlineAt }.take(6)
    fun tasksOnDate(dateMillis: Long): List<StudyTask> = tasks.filter { it.deadlineAt != null && DateUtils.millisToDate(it.deadlineAt) == DateUtils.millisToDate(dateMillis) }

    fun weeklyDoneCounts(): List<Int> {
        val start = DateUtils.weekStart()
        return (0..6).map { offset ->
            val date = start.plusDays(offset.toLong())
            tasks.count { it.completedAt != null && DateUtils.millisToDate(it.completedAt) == date }
        }
    }

    fun focusMinutesBySubject(): Map<Long?, Int> = focusSessions.groupBy { it.subjectId }.mapValues { it.value.sumOf { session -> session.durationMinutes } }

    fun subjectProgress(subjectId: Long): Float {
        val list = tasksForSubject(subjectId)
        if (list.isEmpty()) return 0f
        return list.count { it.status == TaskStatus.Done }.toFloat() / list.size
    }

    fun exportTasksCsv(): Path {
        val path = stampedPath("tasks_export", "csv")
        val text = buildString {
            appendLine("id,subject,title,status,priority,deadline,estimated_minutes,spent_minutes,recurrence")
            tasks.forEach { t ->
                appendLine(listOf(t.id, subjectName(t.subjectId), t.title, t.status.title, t.priority.title, DateUtils.formatIso(t.deadlineAt), t.estimatedMinutes ?: "", t.spentMinutes, t.recurrence.title).joinToString(",") { csv(it.toString()) })
            }
        }
        Files.writeString(path, text)
        lastMessage = "Exported: $path"
        return path
    }

    fun exportNotesMarkdown(): Path {
        val path = stampedPath("notes_export", "md")
        val text = buildString {
            appendLine("# StudyFlow notes")
            notes.forEach { n ->
                appendLine("\n## ${n.title}")
                appendLine("Subject: ${subjectName(n.subjectId)}")
                if (n.tags.isNotEmpty()) appendLine("Tags: ${n.tags.joinToString()}")
                appendLine()
                appendLine(n.content)
            }
        }
        Files.writeString(path, text)
        lastMessage = "Exported: $path"
        return path
    }

    fun exportHabitsCsv(): Path {
        val path = stampedPath("habits_export", "csv")
        val text = buildString {
            appendLine("id,name,description,streak,completed_dates")
            habits.forEach { h -> appendLine(listOf(h.id, h.name, h.description, habitStreak(h), h.completedDates.sorted().joinToString(";")).joinToString(",") { csv(it.toString()) }) }
        }
        Files.writeString(path, text)
        lastMessage = "Exported: $path"
        return path
    }

    fun exportBackup(): Path {
        val path = stampedPath("studyflow_backup", "txt")
        val text = buildString {
            appendLine("STUDYFLOW BACKUP")
            appendLine("Subjects: ${subjects.size}")
            subjects.forEach { appendLine("SUBJECT | ${it.id} | ${it.name} | ${it.colorHex}") }
            appendLine("\nTasks: ${tasks.size}")
            tasks.forEach { appendLine("TASK | ${it.id} | ${it.title} | ${subjectName(it.subjectId)} | ${it.status.title} | ${it.recurrence.title}") }
            appendLine("\nNotes: ${notes.size}")
            notes.forEach { appendLine("NOTE | ${it.id} | ${it.title} | ${subjectName(it.subjectId)}") }
            appendLine("\nHabits: ${habits.size}")
            habits.forEach { appendLine("HABIT | ${it.id} | ${it.name} | streak=${habitStreak(it)}") }
            appendLine("\nFocus sessions: ${focusSessions.size}")
            focusSessions.forEach { appendLine("FOCUS | ${it.id} | ${it.durationMinutes} minutes | ${subjectName(it.subjectId)}") }
        }
        Files.writeString(path, text)
        lastMessage = "Backup exported: $path"
        return path
    }

    fun exportRawBackup(): Path {
        store.save(subjects, tasks, notes, focusSessions, habits)
        val path = store.exportRawBackup()
        lastMessage = "SQLite backup exported: $path"
        return path
    }

    fun restoreRawBackup(): Boolean {
        val restored = store.importRawBackup()
        if (!restored) {
            lastMessage = "No restorable backup found"
            return false
        }
        val snapshot = store.load()
        if (snapshot == null) {
            lastMessage = "Backup restore failed: database is unreadable."
            return false
        }
        subjects = snapshot.subjects
        tasks = snapshot.tasks
        notes = snapshot.notes
        focusSessions = snapshot.sessions
        habits = snapshot.habits
        recalcIds()
        lastMessage = "Restored from raw backup"
        return true
    }

    fun importTasksCsv(pathText: String): Int {
        val path = Path.of(pathText.trim().trim('"'))
        if (!Files.exists(path)) {
            lastMessage = "CSV import failed: file not found"
            return 0
        }
        val lines = Files.readAllLines(path).dropWhile { it.isBlank() }
        if (lines.isEmpty()) return 0
        var imported = 0
        val defaultSubject = subjects.firstOrNull()?.id ?: return 0
        lines.drop(1).forEach { line ->
            val cells = parseCsvLine(line)
            if (cells.size >= 3) {
                val subjectId = cells.getOrNull(1)?.let { name -> subjects.firstOrNull { it.name.equals(name, ignoreCase = true) }?.id } ?: defaultSubject
                val title = cells.getOrNull(2).orEmpty().ifBlank { cells.getOrNull(0).orEmpty() }
                if (title.isNotBlank()) {
                    val priority = enumByTitleOrName(cells.getOrNull(4), TaskPriority.Medium)
                    val deadline = cells.getOrNull(5)?.takeIf { it.isNotBlank() }?.let { runCatching { DateUtils.parseIsoDateToMillis(it) }.getOrNull() }
                    val recurrence = enumByTitleOrName(cells.getOrNull(8), Recurrence.None)
                    tasks = tasks + StudyTask(nextTaskId++, subjectId, title, cells.getOrNull(3).orEmpty(), TaskStatus.Todo, priority, deadline, cells.getOrNull(6)?.toIntOrNull(), 0, DateUtils.nowMillis(), null, recurrence)
                    imported++
                }
            }
        }
        save("Imported $imported tasks from CSV")
        return imported
    }

    fun importNotesMarkdown(pathText: String): Int {
        val path = Path.of(pathText.trim().trim('"'))
        if (!Files.exists(path)) {
            lastMessage = "Markdown import failed: file not found"
            return 0
        }
        val text = Files.readString(path)
        val blocks = Regex("(?m)^##\\s+(.+)$").findAll(text).toList()
        var imported = 0
        if (blocks.isEmpty()) {
            if (text.isNotBlank()) {
                addNote(null, path.fileName.toString().removeSuffix(".md"), text, "import")
                imported = 1
            }
        } else {
            blocks.forEachIndexed { index, match ->
                val title = match.groupValues[1].trim()
                val start = match.range.last + 1
                val end = blocks.getOrNull(index + 1)?.range?.first ?: text.length
                val content = text.substring(start, end).trim()
                addNote(null, title, content, "import, markdown")
                imported++
            }
        }
        lastMessage = "Imported $imported notes from Markdown"
        return imported
    }

    private fun appendNextRecurringTask(task: StudyTask) {
        if (task.recurrence == Recurrence.None || task.deadlineAt == null) return
        val rootId = task.sourceRecurringTaskId ?: task.id
        val nextDeadlineDate = task.recurrence.nextDate(DateUtils.millisToDate(task.deadlineAt))
        val nextDeadlineAt = DateUtils.dateToMillis(nextDeadlineDate)
        val alreadyExists = tasks.any { it.sourceRecurringTaskId == rootId && it.deadlineAt == nextDeadlineAt }
        if (alreadyExists) return
        tasks = tasks + task.copy(
            id = nextTaskId++,
            status = TaskStatus.Todo,
            deadlineAt = nextDeadlineAt,
            spentMinutes = 0,
            createdAt = DateUtils.nowMillis(),
            completedAt = null,
            sourceRecurringTaskId = rootId
        )
    }

    private fun stampedPath(name: String, ext: String): Path {
        val ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        return store.dataDir.resolve("${name}_${ts}.${ext}")
    }

    private fun parseTags(text: String): List<String> = text.split(',', '#', ';').map { it.trim() }.filter { it.isNotEmpty() }.distinct()

    private fun save(message: String) {
        store.save(subjects, tasks, notes, focusSessions, habits)
        lastMessage = "$message. SQLite database: ${store.dataDir.resolve("studyflow.sqlite")}"
        recalcIds()
    }

    private fun recalcIds() {
        nextSubjectId = (subjects.maxOfOrNull { it.id } ?: 0L) + 1
        nextTaskId = (tasks.maxOfOrNull { it.id } ?: 0L) + 1
        nextNoteId = (notes.maxOfOrNull { it.id } ?: 0L) + 1
        nextSessionId = (focusSessions.maxOfOrNull { it.id } ?: 0L) + 1
        nextHabitId = (habits.maxOfOrNull { it.id } ?: 0L) + 1
    }

    private fun csv(value: String): String = "\"${value.replace("\"", "\"\"")}\""

    private fun parseCsvLine(line: String): List<String> {
        val out = mutableListOf<String>()
        val current = StringBuilder()
        var quoted = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' && quoted && i + 1 < line.length && line[i + 1] == '"' -> { current.append('"'); i++ }
                ch == '"' -> quoted = !quoted
                ch == ',' && !quoted -> { out += current.toString(); current.clear() }
                else -> current.append(ch)
            }
            i++
        }
        out += current.toString()
        return out.map { it.trim() }
    }

    private inline fun <reified T : Enum<T>> enumByTitleOrName(raw: String?, fallback: T): T {
        val value = raw?.trim().orEmpty()
        if (value.isBlank()) return fallback
        return enumValues<T>().firstOrNull { it.name.equals(value, true) || titleOf(it).equals(value, true) } ?: fallback
    }

    private fun titleOf(value: Enum<*>): String = when (value) {
        is TaskPriority -> value.title
        is TaskStatus -> value.title
        is Recurrence -> value.title
        else -> value.name
    }
}
