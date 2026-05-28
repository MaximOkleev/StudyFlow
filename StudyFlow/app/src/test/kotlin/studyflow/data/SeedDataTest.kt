package studyflow.data

import kotlin.test.Test
import kotlin.test.assertTrue

class SeedDataTest {
    @Test
    fun starterDataContainsOnlySubjectsByDefault() {
        assertTrue(SeedData.subjects().isNotEmpty())
        assertTrue(SeedData.tasks().isEmpty())
        assertTrue(SeedData.notes().isEmpty())
        assertTrue(SeedData.focusSessions().isEmpty())
        assertTrue(SeedData.habits().isEmpty())
    }
}
