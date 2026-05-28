package studyflow.domain.model

data class Habit(
    val id: Long,
    val name: String,
    val description: String,
    val colorHex: String,
    val createdAt: Long,
    val archived: Boolean = false,
    val completedDates: Set<String> = emptySet()
)
