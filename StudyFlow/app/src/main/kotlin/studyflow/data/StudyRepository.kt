package studyflow.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import studyflow.domain.model.FocusSession
import studyflow.domain.model.Note
import studyflow.domain.model.Subject
import studyflow.domain.model.StudyTask
import studyflow.domain.model.TaskPriority
import studyflow.domain.model.TaskStatus
import studyflow.util.DateUtils
import studyflow.util.safeColorHex
import java.nio.file.Files
import java.nio.file.Path

class StudyRepository(private val store: LocalStore = LocalStore()) {
    var subjects by mutableStateOf<List<Subject>>(emptyList())
        private set
    var tasks by mutableStateOf<List<StudyTask>>(emptyList())
        private set
    var notes by mutableStateOf<List<Note>>(emptyList())
        private set
    var focusSessions by mutableStateOf<List<FocusSession>>(emptyList())
        private set
    var lastMessage by mutableStateOf("Data folder: ${store.dataDir}")
        private set

    private var nextSubjectId = 1L
    private var nextTaskId = 1L
    private var nextNoteId = 1L
    private var nextSessionId = 1L

    init {
        val snapshot = store.load()
        if (snapshot == null || snapshot.subjects.isEmpty()) {
            subjects = SeedData.subjects()
            tasks = SeedData.tasks()
            notes = SeedData.notes()
            focusSessions = SeedData.focusSessions()
            save("Demo data created")
        } else {
            subjects = snapshot.subjects
            tasks = snapshot.tasks
            notes = snapshot.notes
            focusSessions = snapshot.sessions
        }
        recalcIds()
    }

    fun subjectById(id: Long): Subject? = subjects.firstOrNull { it.id == id }
    fun taskById(id: Long): StudyTask? = tasks.firstOrNull { it.id == id }
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
        if (title.isBlank() || subjects.none { it.id == subjectId }) return
        tasks = tasks + StudyTask(
            id = nextTaskId++,
            subjectId = subjectId,
            title = title.trim(),
            description = description.trim(),
            status = TaskStatus.Todo,
            priority = priority,
            deadlineAt = deadlineDaysFromNow?.let { DateUtils.daysFromNow(it.toLong()) },
            estimatedMinutes = estimatedMinutes,
            spentMinutes = 0,
            createdAt = DateUtils.nowMillis(),
            completedAt = null
        )
        save("Task added")
    }

    fun updateTask(task: StudyTask, subjectId: Long, title: String, description: String, status: TaskStatus, priority: TaskPriority, deadlineDaysFromNow: Int?, estimatedMinutes: Int?) {
        val completed = if (status == TaskStatus.Done && task.completedAt == null) DateUtils.nowMillis() else if (status != TaskStatus.Done) null else task.completedAt
        tasks = tasks.map {
            if (it.id == task.id) it.copy(
                subjectId = subjectId,
                title = title.trim().ifBlank { it.title },
                description = description.trim(),
                status = status,
                priority = priority,
                deadlineAt = deadlineDaysFromNow?.let { days -> DateUtils.daysFromNow(days.toLong()) },
                estimatedMinutes = estimatedMinutes,
                completedAt = completed
            ) else it
        }
        save("Task updated")
    }

    fun deleteTask(id: Long) {
        tasks = tasks.filterNot { it.id == id }
        focusSessions = focusSessions.map { if (it.taskId == id) it.copy(taskId = null) else it }
        save("Task deleted")
    }

    fun cycleTaskStatus(taskId: Long) {
        tasks = tasks.map { task ->
            if (task.id != taskId) task else {
                val next = task.status.next()
                task.copy(status = next, completedAt = if (next == TaskStatus.Done) DateUtils.nowMillis() else null)
            }
        }
        save("Task status changed")
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

    fun resetDemoData() {
        store.reset()
        subjects = SeedData.subjects()
        tasks = SeedData.tasks()
        notes = SeedData.notes()
        focusSessions = SeedData.focusSessions()
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
        val path = store.dataDir.resolve("tasks_export.csv")
        val text = buildString {
            appendLine("id,subject,title,status,priority,deadline,estimated_minutes,spent_minutes")
            tasks.forEach { t ->
                appendLine(listOf(t.id, subjectName(t.subjectId), t.title, t.status.title, t.priority.title, DateUtils.formatFull(t.deadlineAt), t.estimatedMinutes ?: "", t.spentMinutes).joinToString(",") { csv(it.toString()) })
            }
        }
        Files.writeString(path, text)
        lastMessage = "Exported: $path"
        return path
    }

    fun exportNotesMarkdown(): Path {
        val path = store.dataDir.resolve("notes_export.md")
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

    fun exportBackup(): Path {
        val path = store.dataDir.resolve("studyflow_backup.txt")
        val text = buildString {
            appendLine("STUDYFLOW BACKUP")
            appendLine("Subjects: ${subjects.size}")
            subjects.forEach { appendLine("SUBJECT | ${it.id} | ${it.name} | ${it.colorHex}") }
            appendLine("\nTasks: ${tasks.size}")
            tasks.forEach { appendLine("TASK | ${it.id} | ${it.title} | ${subjectName(it.subjectId)} | ${it.status.title}") }
            appendLine("\nNotes: ${notes.size}")
            notes.forEach { appendLine("NOTE | ${it.id} | ${it.title} | ${subjectName(it.subjectId)}") }
            appendLine("\nFocus sessions: ${focusSessions.size}")
            focusSessions.forEach { appendLine("FOCUS | ${it.id} | ${it.durationMinutes} minutes | ${subjectName(it.subjectId)}") }
        }
        Files.writeString(path, text)
        lastMessage = "Backup exported: $path"
        return path
    }

    private fun parseTags(text: String): List<String> = text.split(',', '#', ';').map { it.trim() }.filter { it.isNotEmpty() }.distinct()

    private fun save(message: String) {
        store.save(subjects, tasks, notes, focusSessions)
        lastMessage = "$message. Data folder: ${store.dataDir}"
        recalcIds()
    }

    private fun recalcIds() {
        nextSubjectId = (subjects.maxOfOrNull { it.id } ?: 0L) + 1
        nextTaskId = (tasks.maxOfOrNull { it.id } ?: 0L) + 1
        nextNoteId = (notes.maxOfOrNull { it.id } ?: 0L) + 1
        nextSessionId = (focusSessions.maxOfOrNull { it.id } ?: 0L) + 1
    }

    private fun csv(value: String): String = "\"${value.replace("\"", "\"\"")}\""
}
