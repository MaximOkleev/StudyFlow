package studyflow.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import studyflow.domain.model.Subject
import studyflow.domain.model.StudyTask
import studyflow.domain.model.TaskPriority
import studyflow.domain.model.TaskStatus
import java.nio.file.Files

class StudyRepositoryTest {
    private fun createRepository(): StudyRepository {
        val dir = Files.createTempDirectory("studyflow-repo-test")
        val store = LocalStore(dir)
        return StudyRepository(store, seedOnFirstRun = false)
    }

    private fun createRepositoryWithSubject(): StudyRepository {
        val repo = createRepository()
        repo.addSubject("Physics", "Force and Motion", "#FF6B6B", "⚛")
        return repo
    }

    private fun createRepositoryWithTask(): StudyRepository {
        val repo = createRepositoryWithSubject()
        val subject = repo.subjects.first()
        repo.addTask(subject.id, "Study Task", "Learn basics", TaskPriority.High, null, 45)
        return repo
    }

    @Test
    fun repositoryInitializesEmpty() {
        val repo = createRepository()
        assertTrue(repo.subjects.isEmpty())
        assertTrue(repo.tasks.isEmpty())
        assertTrue(repo.notes.isEmpty())
    }

    @Test
    fun canAddNewSubject() {
        val repo = createRepository()
        val initialCount = repo.subjects.size
        repo.addSubject("Physics", "Force and Motion", "#FF6B6B", "⚛")
        assertEquals(initialCount + 1, repo.subjects.size)
        val added = repo.subjects.last()
        assertEquals("Physics", added.name)
        assertEquals("#FF6B6B", added.colorHex)
    }

    @Test
    fun cannotAddSubjectWithBlankName() {
        val repo = createRepository()
        val initialCount = repo.subjects.size
        repo.addSubject("", "Description", "#FF0000", "X")
        assertEquals(initialCount, repo.subjects.size)
    }

    @Test
    fun canFindSubjectById() {
        val repo = createRepositoryWithSubject()
        val subject = repo.subjects.first()
        val found = repo.subjectById(subject.id)
        assertEquals(subject, found)
    }

    @Test
    fun canUpdateSubject() {
        val repo = createRepositoryWithSubject()
        val subject = repo.subjects.first()
        repo.updateSubject(subject, "Updated Name", "New Desc", "#00FF00", "U")
        val updated = repo.subjectById(subject.id)
        assertNotNull(updated)
        assertEquals("Updated Name", updated.name)
        assertEquals("#00FF00", updated.colorHex)
    }

    @Test
    fun canDeleteSubject() {
        val repo = createRepositoryWithSubject()
        val subject = repo.subjects.first()
        val initialCount = repo.subjects.size
        repo.deleteSubject(subject.id)
        assertEquals(initialCount - 1, repo.subjects.size)
        assertNull(repo.subjectById(subject.id))
    }

    @Test
    fun canAddTaskToSubject() {
        val repo = createRepositoryWithSubject()
        val subject = repo.subjects.first()
        val initialCount = repo.tasks.size
        repo.addTask(subject.id, "Study Task", "Learn basics", TaskPriority.High, null, 45)
        assertEquals(initialCount + 1, repo.tasks.size)
    }

    @Test
    fun cannotAddTaskWithBlankTitle() {
        val repo = createRepository()
        val initialCount = repo.tasks.size
        repo.addTask(1L, "", "Description", TaskPriority.Medium, null, 30)
        assertEquals(initialCount, repo.tasks.size)
    }

    @Test
    fun canCycleTaskStatus() {
        val repo = createRepositoryWithTask()
        val task = repo.tasks.first()
        val originalStatus = task.status
        repo.cycleTaskStatus(task.id)
        val updated = repo.taskById(task.id)
        assertNotNull(updated)
        assertEquals(originalStatus.next(), updated.status)
    }

    @Test
    fun canCountDoneTasks() {
        val repo = createRepository()
        val doneCount = repo.doneTasks()
        assertTrue(doneCount >= 0)
    }

    @Test
    fun canCalculateProgress() {
        val repo = createRepositoryWithSubject()
        val subject = repo.subjects.first()
        val progress = repo.subjectProgress(subject.id)
        assertTrue(progress >= 0f && progress <= 1f)
    }

    @Test
    fun canAddSpentMinutes() {
        val repo = createRepositoryWithTask()
        val task = repo.tasks.first()
        val initialSpent = task.spentMinutes
        repo.addSpentMinutes(task.id, 15)
        val updated = repo.taskById(task.id)
        assertNotNull(updated)
        assertEquals(initialSpent + 15, updated.spentMinutes)
    }

    @Test
    fun canLogFocusSession() {
        val repo = createRepositoryWithSubject()
        val subject = repo.subjects.first()
        val initialCount = repo.focusSessions.size
        repo.logFocusSession(null, subject.id, 25)
        assertEquals(initialCount + 1, repo.focusSessions.size)
    }

    @Test
    fun canIgnoreZeroOrNegativeMinutesForSession() {
        val repo = createRepository()
        val initialCount = repo.focusSessions.size
        repo.logFocusSession(null, null, 0)
        repo.logFocusSession(null, null, -10)
        assertEquals(initialCount, repo.focusSessions.size)
    }

    @Test
    fun canGetSubjectName() {
        val repo = createRepositoryWithSubject()
        val subject = repo.subjects.first()
        val name = repo.subjectName(subject.id)
        assertEquals(subject.name, name)
    }

    @Test
    fun noSubjectReturnsDefault() {
        val repo = createRepository()
        val name = repo.subjectName(null)
        assertEquals("No subject", name)
    }
}

