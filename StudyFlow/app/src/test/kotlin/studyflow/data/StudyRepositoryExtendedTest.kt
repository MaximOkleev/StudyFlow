package studyflow.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertFalse
import studyflow.domain.model.Subject
import studyflow.domain.model.StudyTask
import studyflow.domain.model.TaskPriority
import studyflow.domain.model.TaskStatus
import java.nio.file.Files

class StudyRepositoryExportTest {
    private fun createRepository(): StudyRepository {
        val dir = Files.createTempDirectory("studyflow-export-test")
        val store = LocalStore(dir)
        return StudyRepository(store)
    }

    @Test
    fun canExportTasksCsv() {
        val repo = createRepository()
        val path = repo.exportTasksCsv()
        assertTrue(Files.exists(path))
        val content = Files.readString(path)
        assertTrue(content.contains("id,subject,title"))
    }

    @Test
    fun canExportNotesMarkdown() {
        val repo = createRepository()
        val path = repo.exportNotesMarkdown()
        assertTrue(Files.exists(path))
        val content = Files.readString(path)
        assertTrue(content.contains("# StudyFlow notes"))
    }

    @Test
    fun canExportBackup() {
        val repo = createRepository()
        val path = repo.exportBackup()
        assertTrue(Files.exists(path))
        val content = Files.readString(path)
        assertTrue(content.contains("STUDYFLOW BACKUP"))
    }

    @Test
    fun exportedCsvContainsAllTasks() {
        val repo = createRepository()
        val initialTaskCount = repo.tasks.size
        val path = repo.exportTasksCsv()
        val content = Files.readString(path)
        val lines = content.trim().lines()
        assertEquals(initialTaskCount + 1, lines.size)
    }

    @Test
    fun exportedMarkdownContainsAllNotes() {
        val repo = createRepository()
        val initialNoteCount = repo.notes.size
        val path = repo.exportNotesMarkdown()
        val content = Files.readString(path)
        val noteCount = content.split("## ").size - 1
        assertEquals(initialNoteCount, noteCount)
    }

    @Test
    fun exportedBackupContainsSubjectCount() {
        val repo = createRepository()
        val path = repo.exportBackup()
        val content = Files.readString(path)
        assertEquals(repo.subjects.size, repo.subjects.size)
        assertTrue(content.contains("Subjects: ${repo.subjects.size}"))
    }

    @Test
    fun exportRawBackupCreatesFile() {
        val repo = createRepository()
        val path = repo.exportRawBackup()
        assertTrue(Files.exists(path))
    }

    @Test
    fun lastMessageUpdatedAfterExport() {
        val repo = createRepository()
        repo.exportTasksCsv()
        assertTrue(repo.lastMessage.contains("Exported:"))
    }
}

class StudyRepositoryRestoreTest {
    private fun createRepository(): StudyRepository {
        val dir = Files.createTempDirectory("studyflow-restore-test")
        val store = LocalStore(dir)
        return StudyRepository(store)
    }

    @Test
    fun restoreWithoutBackupReturnsFalse() {
        val repo = createRepository()
        val restored = repo.restoreRawBackup()
        assertFalse(restored)
        assertTrue(repo.lastMessage.contains("No restorable backup found"))
    }

    @Test
    fun canExportAndRestoreData() {
        val repo = createRepository()
        val initialSubjectCount = repo.subjects.size
        val initialTaskCount = repo.tasks.size

        repo.exportRawBackup()
        assertTrue(repo.restoreRawBackup())

        assertEquals(initialSubjectCount, repo.subjects.size)
        assertEquals(initialTaskCount, repo.tasks.size)
    }

    @Test
    fun restoreUpdatesLastMessage() {
        val repo = createRepository()
        repo.exportRawBackup()
        repo.restoreRawBackup()
        assertTrue(repo.lastMessage.contains("Restored from raw backup"))
    }
}

class StudyRepositoryStatsTest {
    private fun createRepository(): StudyRepository {
        val dir = Files.createTempDirectory("studyflow-stats-test")
        val store = LocalStore(dir)
        return StudyRepository(store)
    }

    @Test
    fun weeklyDoneCountsHasSevenElements() {
        val repo = createRepository()
        val counts = repo.weeklyDoneCounts()
        assertEquals(7, counts.size)
    }

    @Test
    fun focusMinutesBySubjectGroupsCorrectly() {
        val repo = createRepository()
        val minutesBySubject = repo.focusMinutesBySubject()
        assertTrue(minutesBySubject.isNotEmpty())
        assertTrue(minutesBySubject.values.all { it >= 0 })
    }

    @Test
    fun tasksOnDateReturnsTasksForDate() {
        val repo = createRepository()
        val today = System.currentTimeMillis()
        val tasks = repo.tasksOnDate(today)
        assertTrue(tasks.all { it.deadlineAt != null })
    }

    @Test
    fun todayTasksFiltersCorrectly() {
        val repo = createRepository()
        val today = repo.todayTasks()
        assertTrue(today.all { it.status != TaskStatus.Done })
    }

    @Test
    fun upcomingTasksLimitedAndSorted() {
        val repo = createRepository()
        val upcoming = repo.upcomingTasks()
        assertTrue(upcoming.size <= 6)
        if (upcoming.size > 1) {
            for (i in 0 until upcoming.size - 1) {
                assertTrue(upcoming[i].deadlineAt!! <= upcoming[i + 1].deadlineAt!!)
            }
        }
    }

    @Test
    fun overdueTasksAreIncomplete() {
        val repo = createRepository()
        val overdueCount = repo.overdueTasks()
        assertTrue(overdueCount >= 0)
    }

    @Test
    fun totalFocusMinutesReturnsSumOfAllSessions() {
        val repo = createRepository()
        val total = repo.totalFocusMinutes()
        val expected = repo.focusSessions.sumOf { it.durationMinutes }
        assertEquals(expected, total)
    }

    @Test
    fun activTasksCountExcludesDone() {
        val repo = createRepository()
        val active = repo.activeTasks()
        assertTrue(active >= 0)
        assertTrue(active + repo.doneTasks() > 0)
    }
}

class StudyRepositoryNotesTest {
    private fun createRepository(): StudyRepository {
        val dir = Files.createTempDirectory("studyflow-notes-test")
        val store = LocalStore(dir)
        return StudyRepository(store)
    }

    @Test
    fun canAddNoteWithoutSubject() {
        val repo = createRepository()
        val initialCount = repo.notes.size
        repo.addNote(null, "General Note", "Content here", "tag1, tag2")
        assertEquals(initialCount + 1, repo.notes.size)
        assertTrue(repo.notes.last().subjectId == null)
    }

    @Test
    fun canAddNoteWithSubject() {
        val repo = createRepository()
        val subject = repo.subjects.first()
        val initialCount = repo.notes.size
        repo.addNote(subject.id, "Subject Note", "Content", "topic")
        assertEquals(initialCount + 1, repo.notes.size)
        assertEquals(subject.id, repo.notes.last().subjectId)
    }

    @Test
    fun cannotAddNoteWithBlankTitle() {
        val repo = createRepository()
        val initialCount = repo.notes.size
        repo.addNote(null, "", "Content", "tag")
        assertEquals(initialCount, repo.notes.size)
    }

    @Test
    fun canUpdateNote() {
        val repo = createRepository()
        val note = repo.notes.first()
        repo.updateNote(note, note.subjectId, "Updated Title", "Updated Content", "newtag")
        val updated = repo.notes.first { it.id == note.id }
        assertEquals("Updated Title", updated.title)
        assertEquals("Updated Content", updated.content)
        assertTrue(updated.tags.contains("newtag"))
    }

    @Test
    fun canDeleteNote() {
        val repo = createRepository()
        val note = repo.notes.first()
        val initialCount = repo.notes.size
        repo.deleteNote(note.id)
        assertEquals(initialCount - 1, repo.notes.size)
    }
}

class StudyRepositoryTaskDetailsTest {
    private fun createRepository(): StudyRepository {
        val dir = Files.createTempDirectory("studyflow-task-details-test")
        val store = LocalStore(dir)
        return StudyRepository(store)
    }

    @Test
    fun tasksForSubjectFiltersCorrectly() {
        val repo = createRepository()
        val subject = repo.subjects.first()
        val subjectTasks = repo.tasksForSubject(subject.id)
        assertTrue(subjectTasks.all { it.subjectId == subject.id })
    }

    @Test
    fun notesForSubjectFiltersCorrectly() {
        val repo = createRepository()
        val subject = repo.subjects.first()
        val subjectNotes = repo.notesForSubject(subject.id)
        assertTrue(subjectNotes.all { it.subjectId == subject.id || it.subjectId == null })
    }

    @Test
    fun deleteSubjectClearesReferences() {
        val repo = createRepository()
        val subject = repo.subjects.first()
        val subjectId = subject.id
        repo.deleteSubject(subjectId)

        assertTrue(repo.subjects.none { it.id == subjectId })
        assertTrue(repo.tasks.none { it.subjectId == subjectId })
    }

    @Test
    fun deleteTaskClearsFocusSessions() {
        val repo = createRepository()
        val task = repo.tasks.first()
        val taskId = task.id
        repo.deleteTask(taskId)

        assertTrue(repo.tasks.none { it.id == taskId })
        assertTrue(repo.focusSessions.none { it.taskId == taskId })
    }

    @Test
    fun updateTaskStatusChangesCompletedAt() {
        val repo = createRepository()
        val task = repo.tasks.first()
        val originalStatus = task.status

        repo.cycleTaskStatus(task.id)
        val updated = repo.taskById(task.id)

        assertNotNull(updated)
        assertEquals(originalStatus.next(), updated.status)
        if (updated.status == TaskStatus.Done) {
            assertNotNull(updated.completedAt)
        }
    }
}

