package studyflow.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class FocusSessionTest {
    @Test
    fun focusSessionCanBeCreatedWithAllFields() {
        val session = FocusSession(
            id = 1L,
            taskId = 10L,
            subjectId = 5L,
            startedAt = 1000L,
            durationMinutes = 25
        )
        assertEquals(1L, session.id)
        assertEquals(10L, session.taskId)
        assertEquals(5L, session.subjectId)
        assertEquals(1000L, session.startedAt)
        assertEquals(25, session.durationMinutes)
    }

    @Test
    fun focusSessionCanHaveNullTaskAndSubjectIds() {
        val session = FocusSession(
            id = 2L,
            taskId = null,
            subjectId = null,
            startedAt = 2000L,
            durationMinutes = 30
        )
        assertEquals(null, session.taskId)
        assertEquals(null, session.subjectId)
    }

    @Test
    fun focusSessionCanBeCopiedWithModifiedFields() {
        val original = FocusSession(1L, 10L, 5L, 1000L, 25)
        val modified = original.copy(durationMinutes = 35, taskId = 20L)

        assertEquals(1L, modified.id)
        assertEquals(20L, modified.taskId)
        assertEquals(5L, modified.subjectId)
        assertEquals(1000L, modified.startedAt)
        assertEquals(35, modified.durationMinutes)
    }

    @Test
    fun focusSessionsShouldBeEqualByValue() {
        val session1 = FocusSession(1L, 10L, 5L, 1000L, 25)
        val session2 = FocusSession(1L, 10L, 5L, 1000L, 25)
        assertEquals(session1, session2)
    }
}

