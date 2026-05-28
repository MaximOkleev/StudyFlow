package studyflow.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.nio.file.Files

class WindowPreferencesTest {
    @Test
    fun windowPreferencesDefaultsToStandardSize() {
        val dir = Files.createTempDirectory("studyflow-prefs-test")
        val prefs = WindowPreferences(dir)
        val loaded = prefs.load()
        assertEquals(1360f, loaded.width)
        assertEquals(900f, loaded.height)
    }

    @Test
    fun windowPreferencesSavesAndLoadsCorrectly() {
        val dir = Files.createTempDirectory("studyflow-prefs-test")
        val prefs = WindowPreferences(dir)
        prefs.save(1440f, 1080f)
        val loaded = prefs.load()
        assertEquals(1440f, loaded.width)
        assertEquals(1080f, loaded.height)
    }

    @Test
    fun windowPreferencesEnforcesMinimumWidth() {
        val dir = Files.createTempDirectory("studyflow-prefs-test")
        val prefs = WindowPreferences(dir)
        prefs.save(500f, 900f)
        val loaded = prefs.load()
        assertTrue(loaded.width >= 980f)
    }

    @Test
    fun windowPreferencesEnforcesMinimumHeight() {
        val dir = Files.createTempDirectory("studyflow-prefs-test")
        val prefs = WindowPreferences(dir)
        prefs.save(1360f, 400f)
        val loaded = prefs.load()
        assertTrue(loaded.height >= 680f)
    }

    @Test
    fun windowPreferencesHandlesMissingFile() {
        val dir = Files.createTempDirectory("studyflow-prefs-empty-test")
        val prefs = WindowPreferences(dir)
        val loaded = prefs.load()
        assertEquals(1360f, loaded.width)
        assertEquals(900f, loaded.height)
    }
}

