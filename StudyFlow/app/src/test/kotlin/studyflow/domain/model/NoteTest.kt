package studyflow.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NoteTest {
    @Test
    fun noteCreation() {
        val note = Note(1L, 5L, "Title", "Content", listOf("tag1", "tag2"), 1000L, 1500L)
        assertEquals(1L, note.id)
        assertEquals(5L, note.subjectId)
        assertEquals("Title", note.title)
        assertEquals("Content", note.content)
        assertEquals(2, note.tags.size)
    }

    @Test
    fun noteWithNullSubjectId() {
        val note = Note(2L, null, "General", "Text", emptyList(), 1000L, 1000L)
        assertEquals(null, note.subjectId)
    }

    @Test
    fun noteWithEmptyTags() {
        val note = Note(3L, 10L, "No tags", "Content", emptyList(), 1000L, 1000L)
        assertTrue(note.tags.isEmpty())
    }

    @Test
    fun noteCopying() {
        val original = Note(1L, 5L, "Title", "Content", listOf("tag1"), 1000L, 1000L)
        val modified = original.copy(title = "New title", updatedAt = 2000L)
        assertEquals("New title", modified.title)
        assertEquals(2000L, modified.updatedAt)
        assertEquals(1L, modified.id)
    }

    @Test
    fun noteEquality() {
        val note1 = Note(1L, 5L, "Title", "Content", listOf("tag1"), 1000L, 1500L)
        val note2 = Note(1L, 5L, "Title", "Content", listOf("tag1"), 1000L, 1500L)
        assertEquals(note1, note2)
    }
}

