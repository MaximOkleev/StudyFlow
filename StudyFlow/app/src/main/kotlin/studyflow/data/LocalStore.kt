package studyflow.data

import studyflow.domain.model.FocusSession
import studyflow.domain.model.Note
import studyflow.domain.model.Subject
import studyflow.domain.model.StudyTask
import studyflow.domain.model.TaskPriority
import studyflow.domain.model.TaskStatus
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Properties
import kotlin.io.path.exists

class LocalStore {
    val dataDir: Path = Paths.get(System.getProperty("user.home"), ".studyflow")
    private val file: Path = dataDir.resolve("studyflow.properties")

    init {
        Files.createDirectories(dataDir)
    }

    fun exists(): Boolean = file.exists()

    fun save(subjects: List<Subject>, tasks: List<StudyTask>, notes: List<Note>, sessions: List<FocusSession>) {
        val p = Properties()
        p["subjects.count"] = subjects.size.toString()
        subjects.forEachIndexed { i, s ->
            p["subject.$i.id"] = s.id.toString()
            p["subject.$i.name"] = s.name
            p["subject.$i.description"] = s.description
            p["subject.$i.colorHex"] = s.colorHex
            p["subject.$i.icon"] = s.icon
            p["subject.$i.createdAt"] = s.createdAt.toString()
        }
        p["tasks.count"] = tasks.size.toString()
        tasks.forEachIndexed { i, t ->
            p["task.$i.id"] = t.id.toString()
            p["task.$i.subjectId"] = t.subjectId.toString()
            p["task.$i.title"] = t.title
            p["task.$i.description"] = t.description
            p["task.$i.status"] = t.status.name
            p["task.$i.priority"] = t.priority.name
            p["task.$i.deadlineAt"] = t.deadlineAt?.toString() ?: ""
            p["task.$i.estimatedMinutes"] = t.estimatedMinutes?.toString() ?: ""
            p["task.$i.spentMinutes"] = t.spentMinutes.toString()
            p["task.$i.createdAt"] = t.createdAt.toString()
            p["task.$i.completedAt"] = t.completedAt?.toString() ?: ""
        }
        p["notes.count"] = notes.size.toString()
        notes.forEachIndexed { i, n ->
            p["note.$i.id"] = n.id.toString()
            p["note.$i.subjectId"] = n.subjectId?.toString() ?: ""
            p["note.$i.title"] = n.title
            p["note.$i.content"] = n.content
            p["note.$i.tags"] = n.tags.joinToString(",")
            p["note.$i.createdAt"] = n.createdAt.toString()
            p["note.$i.updatedAt"] = n.updatedAt.toString()
        }
        p["sessions.count"] = sessions.size.toString()
        sessions.forEachIndexed { i, s ->
            p["session.$i.id"] = s.id.toString()
            p["session.$i.taskId"] = s.taskId?.toString() ?: ""
            p["session.$i.subjectId"] = s.subjectId?.toString() ?: ""
            p["session.$i.startedAt"] = s.startedAt.toString()
            p["session.$i.durationMinutes"] = s.durationMinutes.toString()
        }
        Files.newOutputStream(file).use { p.store(it, "StudyFlow local data") }
    }

    fun load(): StoreSnapshot? {
        if (!file.exists()) return null
        val p = Properties()
        Files.newInputStream(file).use { p.load(it) }
        return StoreSnapshot(
            subjects = loadSubjects(p),
            tasks = loadTasks(p),
            notes = loadNotes(p),
            sessions = loadSessions(p)
        )
    }

    fun reset() {
        if (file.exists()) Files.delete(file)
    }

    private fun loadSubjects(p: Properties): List<Subject> {
        val count = p.getProperty("subjects.count", "0").toIntOrNull() ?: 0
        return (0 until count).mapNotNull { i ->
            val id = p.getProperty("subject.$i.id")?.toLongOrNull() ?: return@mapNotNull null
            Subject(
                id = id,
                name = p.getProperty("subject.$i.name", "Subject"),
                description = p.getProperty("subject.$i.description", ""),
                colorHex = p.getProperty("subject.$i.colorHex", "#7C3AED"),
                icon = p.getProperty("subject.$i.icon", "•"),
                createdAt = p.getProperty("subject.$i.createdAt", "0").toLongOrNull() ?: 0L
            )
        }
    }

    private fun loadTasks(p: Properties): List<StudyTask> {
        val count = p.getProperty("tasks.count", "0").toIntOrNull() ?: 0
        return (0 until count).mapNotNull { i ->
            val id = p.getProperty("task.$i.id")?.toLongOrNull() ?: return@mapNotNull null
            StudyTask(
                id = id,
                subjectId = p.getProperty("task.$i.subjectId", "0").toLongOrNull() ?: 0L,
                title = p.getProperty("task.$i.title", "Task"),
                description = p.getProperty("task.$i.description", ""),
                status = runCatching { TaskStatus.valueOf(p.getProperty("task.$i.status", "Todo")) }.getOrDefault(TaskStatus.Todo),
                priority = runCatching { TaskPriority.valueOf(p.getProperty("task.$i.priority", "Medium")) }.getOrDefault(TaskPriority.Medium),
                deadlineAt = p.getProperty("task.$i.deadlineAt").takeIf { !it.isNullOrBlank() }?.toLongOrNull(),
                estimatedMinutes = p.getProperty("task.$i.estimatedMinutes").takeIf { !it.isNullOrBlank() }?.toIntOrNull(),
                spentMinutes = p.getProperty("task.$i.spentMinutes", "0").toIntOrNull() ?: 0,
                createdAt = p.getProperty("task.$i.createdAt", "0").toLongOrNull() ?: 0L,
                completedAt = p.getProperty("task.$i.completedAt").takeIf { !it.isNullOrBlank() }?.toLongOrNull()
            )
        }
    }

    private fun loadNotes(p: Properties): List<Note> {
        val count = p.getProperty("notes.count", "0").toIntOrNull() ?: 0
        return (0 until count).mapNotNull { i ->
            val id = p.getProperty("note.$i.id")?.toLongOrNull() ?: return@mapNotNull null
            Note(
                id = id,
                subjectId = p.getProperty("note.$i.subjectId").takeIf { !it.isNullOrBlank() }?.toLongOrNull(),
                title = p.getProperty("note.$i.title", "Note"),
                content = p.getProperty("note.$i.content", ""),
                tags = p.getProperty("note.$i.tags", "").split(',').map { it.trim() }.filter { it.isNotEmpty() },
                createdAt = p.getProperty("note.$i.createdAt", "0").toLongOrNull() ?: 0L,
                updatedAt = p.getProperty("note.$i.updatedAt", "0").toLongOrNull() ?: 0L
            )
        }
    }

    private fun loadSessions(p: Properties): List<FocusSession> {
        val count = p.getProperty("sessions.count", "0").toIntOrNull() ?: 0
        return (0 until count).mapNotNull { i ->
            val id = p.getProperty("session.$i.id")?.toLongOrNull() ?: return@mapNotNull null
            FocusSession(
                id = id,
                taskId = p.getProperty("session.$i.taskId").takeIf { !it.isNullOrBlank() }?.toLongOrNull(),
                subjectId = p.getProperty("session.$i.subjectId").takeIf { !it.isNullOrBlank() }?.toLongOrNull(),
                startedAt = p.getProperty("session.$i.startedAt", "0").toLongOrNull() ?: 0L,
                durationMinutes = p.getProperty("session.$i.durationMinutes", "0").toIntOrNull() ?: 0
            )
        }
    }
}

data class StoreSnapshot(
    val subjects: List<Subject>,
    val tasks: List<StudyTask>,
    val notes: List<Note>,
    val sessions: List<FocusSession>
)
