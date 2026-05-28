package studyflow.domain.model

data class Exam(
    val id: Long,
    val subjectId: Long?,
    val subjectName: String,
    val type: String,
    val teachers: String,
    val startAt: Long,
    val endAt: Long,
    val location: String,
    val createdAt: Long
)
