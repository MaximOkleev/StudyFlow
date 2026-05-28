package studyflow.data

import kotlin.test.Test
import kotlin.test.assertTrue

class SeedDataTest {
    @Test
    fun starterDataContainsSubjectsAndBasicTasks() {
        assertTrue(SeedData.subjects().isNotEmpty())
        assertTrue(SeedData.tasks().isNotEmpty())
        assertTrue(SeedData.notes().isEmpty())
        assertTrue(SeedData.focusSessions().isEmpty())
        assertTrue(SeedData.habits().isEmpty())
    }
}
