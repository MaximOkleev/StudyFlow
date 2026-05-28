package studyflow.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import java.nio.file.Files
import studyflow.domain.model.FocusSession
import studyflow.domain.model.Note
import studyflow.domain.model.Subject
import studyflow.domain.model.StudyTask
import studyflow.domain.model.TaskPriority
import studyflow.domain.model.TaskStatus

class LocalStoreTest {
    @Test
    fun saveLoadRoundtripKeepsCoreData() {
        val dir = Files.createTempDirectory("studyflow-store-test")
        val store = LocalStore(dir)
        val subjects = listOf(Subject(1, "Math", "Algebra", "#7C3AED", "M", 1000L))
        val tasks = listOf(StudyTask(1, 1, "Homework", "Chapter 1", TaskStatus.Todo, TaskPriority.High, 2000L, 60, 15, 1000L, null))
        val notes = listOf(Note(1, 1, "Formula", "a^2+b^2", listOf("math"), 1000L, 1000L))
        val sessions = listOf(FocusSession(1, 1, 1, 1000L, 25))

        store.save(subjects, tasks, notes, sessions)
        val snapshot = assertNotNull(store.load())

        assertEquals("Math", snapshot.subjects.single().name)
        assertEquals("Homework", snapshot.tasks.single().title)
        assertEquals(TaskPriority.High, snapshot.tasks.single().priority)
        assertEquals("Formula", snapshot.notes.single().title)
        assertEquals(25, snapshot.sessions.single().durationMinutes)
        assertTrue(Files.exists(dir.resolve("studyflow.properties")))
    }
}
