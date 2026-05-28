package studyflow.domain.model

data class Note(
    val id: Long,
    val subjectId: Long?,
    val title: String,
    val content: String,
    val tags: List<String>,
    val createdAt: Long,
    val updatedAt: Long
)
