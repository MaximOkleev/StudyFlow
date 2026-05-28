package studyflow.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class SubjectTest {
    @Test
    fun subjectCanBeCreatedWithAllFields() {
        val subject = Subject(1L, "Mathematics", "Algebra and Geometry", "#FF5733", "📐", 1000L)
        assertEquals(1L, subject.id)
        assertEquals("Mathematics", subject.name)
        assertEquals("#FF5733", subject.colorHex)
    }

    @Test
    fun subjectCanBeCopied() {
        val original = Subject(1L, "Math", "Algebra", "#7C3AED", "M", 1000L)
        val modified = original.copy(name = "Advanced Math")
        assertEquals("Advanced Math", modified.name)
        assertEquals(1L, modified.id)
    }
}

