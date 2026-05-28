package studyflow.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class TaskStatusTest {
    @Test
    fun taskStatusNextTransitionFromTodo() {
        assertEquals(TaskStatus.InProgress, TaskStatus.Todo.next())
    }

    @Test
    fun taskStatusNextTransitionFromInProgress() {
        assertEquals(TaskStatus.Done, TaskStatus.InProgress.next())
    }

    @Test
    fun taskStatusNextTransitionFromDone() {
        assertEquals(TaskStatus.Todo, TaskStatus.Done.next())
    }

    @Test
    fun taskStatusHasCorrectTitle() {
        assertEquals("To do", TaskStatus.Todo.title)
        assertEquals("In progress", TaskStatus.InProgress.title)
        assertEquals("Done", TaskStatus.Done.title)
    }
}

class TaskPriorityTest {
    @Test
    fun taskPriorityHasCorrectTitles() {
        assertEquals("Low", TaskPriority.Low.title)
        assertEquals("Medium", TaskPriority.Medium.title)
        assertEquals("High", TaskPriority.High.title)
    }
}

class StudyTaskTest {
    @Test
    fun studyTaskCanBeCreated() {
        val task = StudyTask(
            id = 1L,
            subjectId = 5L,
            title = "Homework",
            description = "Chapter 5",
            status = TaskStatus.Todo,
            priority = TaskPriority.High,
            deadlineAt = 5000L,
            estimatedMinutes = 60,
            spentMinutes = 0,
            createdAt = 1000L,
            completedAt = null
        )
        assertEquals(1L, task.id)
        assertEquals(TaskStatus.Todo, task.status)
        assertEquals(TaskPriority.High, task.priority)
    }

    @Test
    fun studyTaskCanBeCopied() {
        val original = StudyTask(1L, 5L, "Task", "Desc", TaskStatus.Todo, TaskPriority.Medium, 5000L, 30, 10, 1000L, null)
        val done = original.copy(status = TaskStatus.Done, spentMinutes = 30)
        assertEquals(TaskStatus.Done, done.status)
        assertEquals(30, done.spentMinutes)
    }
}

