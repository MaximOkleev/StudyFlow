package studyflow.data

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertFalse

class SeedDataTest {
    @Test
    fun seedDataSubjectsNotEmpty() {
        val subjects = SeedData.subjects()
        assertTrue(subjects.isNotEmpty())
    }

    @Test
    fun seedDataTasksNotEmpty() {
        val tasks = SeedData.tasks()
        assertTrue(tasks.isNotEmpty())
    }

    @Test
    fun seedDataNotesNotEmpty() {
        val notes = SeedData.notes()
        assertTrue(notes.isNotEmpty())
    }

    @Test
    fun seedDataFocusSessionsNotEmpty() {
        val sessions = SeedData.focusSessions()
        assertTrue(sessions.isNotEmpty())
    }

    @Test
    fun seedDataSubjectsHaveValidIds() {
        val subjects = SeedData.subjects()
        val allNonZero = subjects.all { it.id > 0 }
        assertTrue(allNonZero)
    }

    @Test
    fun seedDataSubjectsHaveValidColors() {
        val subjects = SeedData.subjects()
        val allValidColors = subjects.all {
            it.colorHex.startsWith("#") && it.colorHex.length == 7
        }
        assertTrue(allValidColors)
    }

    @Test
    fun seedDataTasksReferencedSubjectsExist() {
        val subjects = SeedData.subjects()
        val subjectIds = subjects.map { it.id }.toSet()
        val tasks = SeedData.tasks()
        val allReferenced = tasks.all { it.subjectId in subjectIds }
        assertTrue(allReferenced)
    }

    @Test
    fun seedDataTasksHavePositiveDuration() {
        val tasks = SeedData.tasks()
        val allNonNegative = tasks.all { it.spentMinutes >= 0 }
        assertTrue(allNonNegative)
    }
}

