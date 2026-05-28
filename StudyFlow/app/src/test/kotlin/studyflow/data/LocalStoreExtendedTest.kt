package studyflow.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertNull
import studyflow.domain.model.FocusSession
import studyflow.domain.model.Note
import studyflow.domain.model.Subject
import studyflow.domain.model.StudyTask
import studyflow.domain.model.TaskPriority
import studyflow.domain.model.TaskStatus
import java.nio.file.Files

class LocalStoreExtendedTest {
    private fun createStore(): LocalStore {
        val dir = Files.createTempDirectory("studyflow-store-extended")
        return LocalStore(dir)
    }

    @Test
    fun storeCanResetData() {
        val store = createStore()
        val subjects = listOf(Subject(1, "Math", "Algebra", "#7C3AED", "M", 1000L))
        store.save(subjects, emptyList(), emptyList(), emptyList())
        store.reset()
        val loaded = store.load()
        assertNull(loaded)
    }

    @Test
    fun storePreservesAllDataTypes() {
        val store = createStore()
        val subjects = listOf(Subject(1, "Physics", "Mechanics", "#FF5733", "P", 1000L))
        val tasks = listOf(StudyTask(1, 1, "Task", "Desc", TaskStatus.InProgress, TaskPriority.Medium, 5000L, 45, 20, 1000L, null))
        val notes = listOf(Note(1, 1, "Note", "Content", listOf("important"), 1000L, 2000L))
        val sessions = listOf(FocusSession(1, 1, 1, 1000L, 30), FocusSession(2, 1, 1, 2000L, 25))

        store.save(subjects, tasks, notes, sessions)
        val snapshot = assertNotNull(store.load())

        assertEquals(1, snapshot.subjects.size)
        assertEquals(1, snapshot.tasks.size)
        assertEquals(1, snapshot.notes.size)
        assertEquals(2, snapshot.sessions.size)
    }

    @Test
    fun storeHandlesEmptyCollections() {
        val store = createStore()
        store.save(emptyList(), emptyList(), emptyList(), emptyList())
        val snapshot = assertNotNull(store.load())
        assertEquals(0, snapshot.subjects.size)
        assertEquals(0, snapshot.tasks.size)
        assertEquals(0, snapshot.notes.size)
        assertEquals(0, snapshot.sessions.size)
    }

    @Test
    fun storeCanExportRawBackup() {
        val store = createStore()
        store.save(
            listOf(Subject(1, "Test", "Desc", "#000000", "T", 1000L)),
            emptyList(),
            emptyList(),
            emptyList()
        )
        val path = store.exportRawBackup()
        assertTrue(Files.exists(path))
    }

    @Test
    fun storeCanImportRawBackup() {
        val store = createStore()
        store.save(
            listOf(Subject(1, "Original", "Description", "#7C3AED", "O", 1000L)),
            emptyList(),
            emptyList(),
            emptyList()
        )
        store.exportRawBackup()


        store.reset()
        assertNull(store.load())

        var imported = false
        var attempts = 0
        while (attempts < 3 && !imported) {
            imported = runCatching { store.importRawBackup() }.getOrDefault(false)
            if (!imported) Thread.sleep(50)
            attempts++
        }

        assertTrue(imported)

        val restored = assertNotNull(store.load())
        assertEquals(1, restored.subjects.size)
        assertEquals("Original", restored.subjects[0].name)
    }

    @Test
    fun storeSavesPropertiesFile() {
        val store = createStore()
        store.save(
            listOf(Subject(1, "Test", "Desc", "#000000", "T", 1000L)),
            emptyList(),
            emptyList(),
            emptyList()
        )
        val propsFile = store.dataDir.resolve("studyflow.properties")
        assertTrue(Files.exists(propsFile))
    }

    @Test
    fun storePreservesLargeDataSets() {
        val store = createStore()
        val subjects = (1..50).map { Subject(it.toLong(), "Subject $it", "Desc", "#7C3AED", "S", 1000L) }
        val tasks = (1..100).map { StudyTask(it.toLong(), (it % 50 + 1).toLong(), "Task $it", "Desc", TaskStatus.Todo, TaskPriority.Low, 5000L, 30, 0, 1000L, null) }
        val notes = (1..75).map { Note(it.toLong(), (it % 50 + 1).toLong(), "Note $it", "Content", listOf("tag"), 1000L, 1000L) }
        val sessions = (1..200).map { FocusSession(it.toLong(), (it % 100 + 1).toLong(), (it % 50 + 1).toLong(), 1000L, 25) }

        store.save(subjects, tasks, notes, sessions)
        val loaded = assertNotNull(store.load())

        assertEquals(50, loaded.subjects.size)
        assertEquals(100, loaded.tasks.size)
        assertEquals(75, loaded.notes.size)
        assertEquals(200, loaded.sessions.size)
    }

    @Test
    fun storeHandlesDuplicateIds() {
        val store = createStore()
        val subjects = listOf(
            Subject(1, "Math", "Algebra", "#7C3AED", "M", 1000L),
            Subject(1, "Physics", "Mechanics", "#FF5733", "P", 1000L)
        )
        store.save(subjects, emptyList(), emptyList(), emptyList())
        val loaded = assertNotNull(store.load())
        assertEquals(2, loaded.subjects.size)
    }
}

