package studyflow.domain.model

data class StudyTask(
    val id: Long,
    val subjectId: Long,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val priority: TaskPriority,
    val deadlineAt: Long?,
    val estimatedMinutes: Int?,
    val spentMinutes: Int,
    val createdAt: Long,
    val completedAt: Long?
)

enum class TaskStatus(val title: String) {
    Todo("To do"),
    InProgress("In progress"),
    Done("Done");

    fun next(): TaskStatus = when (this) {
        Todo -> InProgress
        InProgress -> Done
        Done -> Todo
    }
}

enum class TaskPriority(val title: String) {
    Low("Low"),
    Medium("Medium"),
    High("High")
}
