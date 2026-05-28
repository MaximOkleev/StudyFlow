package studyflow.domain.model

import java.time.LocalDate

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
    val completedAt: Long?,
    val recurrence: Recurrence = Recurrence.None,
    val sourceRecurringTaskId: Long? = null
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

enum class Recurrence(val title: String) {
    None("No repeat"),
    Daily("Daily"),
    Weekly("Weekly"),
    Monthly("Monthly"),
    Yearly("Yearly");

    fun nextDate(date: LocalDate): LocalDate = when (this) {
        None -> date
        Daily -> date.plusDays(1)
        Weekly -> date.plusWeeks(1)
        Monthly -> date.plusMonths(1)
        Yearly -> date.plusYears(1)
    }
}
