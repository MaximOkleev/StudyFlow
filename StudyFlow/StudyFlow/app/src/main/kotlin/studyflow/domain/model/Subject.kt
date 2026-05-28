package studyflow.domain.model

data class Subject(
    val id: Long,
    val name: String,
    val description: String,
    val colorHex: String,
    val icon: String,
    val createdAt: Long
)
