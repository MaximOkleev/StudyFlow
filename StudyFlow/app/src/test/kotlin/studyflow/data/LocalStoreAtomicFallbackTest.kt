package studyflow.data

import kotlin.test.Test
import kotlin.test.assertTrue
import java.nio.file.Files
import studyflow.domain.model.Subject

class LocalStoreAtomicFallbackTest {
    @Test
    fun storeAtomicallyFallsBackWhenAtomicMoveFails() {
        val dir = Files.createTempDirectory("store-atomic-fallback")
        val store = LocalStore(dir, forceAtomicMoveFailure = true)
        val subjects = listOf(Subject(1, "T", "D", "#000000", "T", 1000L))
        store.save(subjects, emptyList(), emptyList(), emptyList())
        val mainFile = dir.resolve("studyflow.properties")
        assertTrue(Files.exists(mainFile))
        val tmp = dir.resolve("studyflow.properties.tmp")
        assertTrue(!Files.exists(tmp))
    }
}

