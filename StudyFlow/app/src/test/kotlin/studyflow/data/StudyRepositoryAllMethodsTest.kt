package studyflow.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import studyflow.domain.model.TaskPriority
import studyflow.domain.model.TaskStatus
import java.nio.file.Files

class StudyRepositoryAllMethodsTest {
    private fun createRepository(): StudyRepository {
        val dir = Files.createTempDirectory("studyflow-all-methods-test")
        val store = LocalStore(dir)
        return StudyRepository(store)
    }

    @Test
    fun exerciseAllPublicMethods() {
        val repo = createRepository()

        val initialSubjects = repo.subjects.size
        repo.addSubject("Biology", "Study cells", "#00FF00", "B")
        assertEquals(initialSubjects + 1, repo.subjects.size)

        repo.addSubject("   ", "x", "#000000", "X")
        assertEquals(initialSubjects + 1, repo.subjects.size)

        val subject = repo.subjects.last()

        val oldName = subject.name
        repo.updateSubject(subject, "   ", "newdesc", "bad", "")
        assertEquals(oldName, repo.subjectById(subject.id)?.name)

        val beforeTasks = repo.tasks.size
        repo.addTask(subject.id, "Read chapter", "Chp 1", TaskPriority.Medium, 1, 45)
        assertEquals(beforeTasks + 1, repo.tasks.size)

        val task = repo.tasks.last()

        repo.updateTask(task, subject.id, "Read chapter updated", "Chp 1.1", TaskStatus.Done, TaskPriority.High, 2, 60)
        val updated = repo.taskById(task.id)
        assertNotNull(updated)
        assertEquals(TaskStatus.Done, updated.status)
        assertNotNull(updated.completedAt)

        val prevStatus = updated.status
        repo.cycleTaskStatus(updated.id)
        val cycled = repo.taskById(updated.id)
        assertNotNull(cycled)
        assertEquals(prevStatus.next(), cycled.status)

        repo.addSpentMinutes(updated.id, 30)
        val withSpent = repo.taskById(updated.id)
        assertNotNull(withSpent)
        assertTrue(withSpent.spentMinutes >= 0)

        val beforeNotes = repo.notes.size
        repo.addNote(null, "General note", "content", "a,b")
        assertEquals(beforeNotes + 1, repo.notes.size)
        val note = repo.notes.last()
        repo.updateNote(note, subject.id, "Updated note", "new content", "tag1")
        val updatedNote = repo.notes.find { it.id == note.id }
        assertNotNull(updatedNote)
        assertEquals("Updated note", updatedNote.title)

        repo.deleteNote(note.id)
        assertTrue(repo.notes.none { it.id == note.id })

        val beforeSessions = repo.focusSessions.size
        repo.logFocusSession(updated.id, subject.id, 20)
        assertEquals(beforeSessions + 1, repo.focusSessions.size)

        repo.logFocusSession(null, null, 0)
        repo.logFocusSession(null, null, -5)

        val total = repo.totalFocusMinutes()
        assertTrue(total >= 0)
        val done = repo.doneTasks()
        assertTrue(done >= 0)
        val active = repo.activeTasks()
        assertTrue(active >= 0)

        val overdue = repo.overdueTasks()
        assertTrue(overdue >= 0)
        val today = repo.todayTasks()
        assertNotNull(today)
        val upcoming = repo.upcomingTasks()
        assertTrue(upcoming.size <= 6)
        val dateMillis = System.currentTimeMillis()
        val tasksOn = repo.tasksOnDate(dateMillis)
        assertNotNull(tasksOn)

        val weekly = repo.weeklyDoneCounts()
        assertEquals(7, weekly.size)

        val focusBySubject = repo.focusMinutesBySubject()
        assertTrue(focusBySubject.isNotEmpty())

        val progress = repo.subjectProgress(subject.id)
        assertTrue(progress >= 0f && progress <= 1f)

        val csv = repo.exportTasksCsv()
        assertTrue(Files.exists(csv))
        val md = repo.exportNotesMarkdown()
        assertTrue(Files.exists(md))
        val backup = repo.exportBackup()
        assertTrue(Files.exists(backup))

        val raw = repo.exportRawBackup()
        assertTrue(Files.exists(raw))

        val restored = repo.restoreRawBackup()
        assertTrue(restored)

        assertNotNull(repo.subjectById(subject.id))
        assertNotNull(repo.taskById(task.id))

        repo.resetDemoData()
        assertTrue(repo.subjects.isNotEmpty())

        repo.saveNow()


        repo.deleteTask(task.id)
        assertTrue(repo.tasks.none { it.id == task.id })
        repo.deleteSubject(subject.id)
        assertTrue(repo.subjects.none { it.id == subject.id })
    }
}
