package studyflow.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import java.nio.file.Files
import java.util.Properties

class LocalStoreEdgeCasesTest {
    @Test
    fun existsIsFalseWhenNoFile() {
        val dir = Files.createTempDirectory("store-edge-1")
        val store = LocalStore(dir)
        assertFalse(store.exists())
    }

    @Test
    fun exportRawBackupThrowsWhenNoMainFile() {
        val dir = Files.createTempDirectory("store-edge-2")
        val store = LocalStore(dir)
        assertFailsWith<IllegalStateException> { store.exportRawBackup() }
    }

    @Test
    fun loadFromSkipsInvalidTaskEntries() {
        val dir = Files.createTempDirectory("store-edge-3")
        val props = Properties()
        props["subjects.count"] = "0"
        props["tasks.count"] = "2"
        props["task.0.title"] = "Bad Task"







        props["task.1.id"] = "5"
        props["task.1.id"] = "5"
        props["task.1.subjectId"] = "1"
        props["task.1.title"] = "Good Task"
        props["task.1.description"] = "d"
        props["task.1.status"] = "Todo"
        props["task.1.priority"] = "Low"
        props["task.1.deadlineAt"] = ""
        props["task.1.estimatedMinutes"] = ""
        props["task.1.spentMinutes"] = "0"
        props["task.1.createdAt"] = "1000"
        props["notes.count"] = "0"
        props["sessions.count"] = "0"

        val file = dir.resolve("studyflow.properties")
        Files.newOutputStream(file).use { props.store(it, "test") }

        val store = LocalStore(dir)
        val snapshot = assertNotNull(store.load())
        assertEquals(1, snapshot.tasks.size)
        assertEquals(5L, snapshot.tasks[0].id)
    }
}
