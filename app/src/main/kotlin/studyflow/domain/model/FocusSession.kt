package studyflow.domain.model

data class FocusSession(
    val id: Long,
    val taskId: Long?,
    val subjectId: Long?,
    val startedAt: Long,
    val durationMinutes: Int
)
