package studyflow.data

import studyflow.domain.model.FocusSession
import studyflow.domain.model.Habit
import studyflow.domain.model.Note
import studyflow.domain.model.Subject
import studyflow.domain.model.StudyTask
import studyflow.domain.model.TaskPriority
import studyflow.domain.model.TaskStatus
import studyflow.util.DateUtils

object SeedData {
    fun subjects() = listOf(
        Subject(1, "Mathematics", "Algebra, functions, logarithms and exam practice.", "#7C3AED", "∑", DateUtils.nowMillis()),
        Subject(2, "Physics", "Mechanics, electricity, formula sheets and variants.", "#0EA5E9", "⚛", DateUtils.nowMillis()),
        Subject(3, "Programming", "Kotlin, C++, algorithms and summer projects.", "#10B981", "</>", DateUtils.nowMillis()),
        Subject(4, "Russian", "Essays, grammar, arguments and exam tasks.", "#F97316", "А", DateUtils.nowMillis()),
        Subject(5, "English", "Vocabulary, listening and weekly speaking practice.", "#EC4899", "EN", DateUtils.nowMillis())
    )

    fun tasks() = listOf(
        StudyTask(1, 3, "Build StudyFlow UI", "Finish dashboard, tasks, notes, timer and statistics screens.", TaskStatus.InProgress, TaskPriority.High, DateUtils.daysFromNow(1), 180, 70, DateUtils.nowMillis(), null),
        StudyTask(2, 2, "Solve physics variant", "Write formulas first, solve tasks, then mark weak topics.", TaskStatus.Todo, TaskPriority.High, DateUtils.daysFromNow(2), 100, 0, DateUtils.nowMillis(), null),
        StudyTask(3, 1, "Repeat logarithm properties", "Make one compact formula sheet and solve 20 examples.", TaskStatus.Done, TaskPriority.Medium, DateUtils.daysFromNow(-1), 60, 75, DateUtils.nowMillis(), DateUtils.nowMillis()),
        StudyTask(4, 4, "Write essay draft", "Use five paragraph structure and check examples.", TaskStatus.Todo, TaskPriority.Medium, DateUtils.daysFromNow(4), 80, 0, DateUtils.nowMillis(), null),
        StudyTask(5, 3, "Start PixelMatter C++ project", "Set up raylib, grid simulation and first materials.", TaskStatus.InProgress, TaskPriority.High, DateUtils.daysFromNow(5), 240, 90, DateUtils.nowMillis(), null),
        StudyTask(6, 5, "Learn 40 new words", "Add examples and repeat after one day.", TaskStatus.Todo, TaskPriority.Low, DateUtils.daysFromNow(0), 35, 10, DateUtils.nowMillis(), null)
    )

    fun notes() = listOf(
        Note(1, 1, "Logarithm properties", "log(ab)=log(a)+log(b)\nlog(a/b)=log(a)-log(b)\nlog(a^n)=n log(a)", listOf("math", "formula"), DateUtils.nowMillis(), DateUtils.nowMillis()),
        Note(2, 3, "Summer project rule", "Do not chase 20 features. First make a stable MVP, then make it beautiful.", listOf("project", "planning"), DateUtils.nowMillis(), DateUtils.nowMillis()),
        Note(3, 2, "Physics solving order", "1. Write given values.\n2. Convert units.\n3. Draw scheme.\n4. Choose formula.\n5. Substitute numbers.", listOf("physics"), DateUtils.nowMillis(), DateUtils.nowMillis())
    )

    fun focusSessions() = listOf(
        FocusSession(1, 1, 3, DateUtils.daysFromNow(-3), 25),
        FocusSession(2, 3, 1, DateUtils.daysFromNow(-2), 50),
        FocusSession(3, 2, 2, DateUtils.daysFromNow(-1), 35),
        FocusSession(4, 5, 3, DateUtils.nowMillis(), 45)
    )

    fun habits() = listOf(
        Habit(1, "Solve one variant", "Do at least one exam-style task set every day.", "#7C3AED", DateUtils.nowMillis(), completedDates = setOf(DateUtils.today().minusDays(2).toString(), DateUtils.today().minusDays(1).toString())),
        Habit(2, "Read notes", "Repeat formulas or summaries for 10 minutes.", "#0EA5E9", DateUtils.nowMillis(), completedDates = setOf(DateUtils.today().minusDays(1).toString())),
        Habit(3, "Focus block", "Complete one timer session without distractions.", "#10B981", DateUtils.nowMillis())
    )

}
