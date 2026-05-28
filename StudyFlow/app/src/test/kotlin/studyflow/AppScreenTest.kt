package studyflow

import kotlin.test.Test
import kotlin.test.assertEquals

class AppScreenTest {
    @Test
    fun appScreenDashboardHasCorrectTitles() {
        assertEquals("Dashboard", AppScreen.Dashboard.title)
        assertEquals("Home", AppScreen.Dashboard.shortTitle)
    }

    @Test
    fun appScreenSubjectsHasCorrectTitles() {
        assertEquals("Subjects", AppScreen.Subjects.title)
        assertEquals("Subjects", AppScreen.Subjects.shortTitle)
    }

    @Test
    fun appScreenTasksHasCorrectTitles() {
        assertEquals("Tasks", AppScreen.Tasks.title)
        assertEquals("Tasks", AppScreen.Tasks.shortTitle)
    }

    @Test
    fun appScreenCalendarHasCorrectTitles() {
        assertEquals("Calendar", AppScreen.Calendar.title)
        assertEquals("Calendar", AppScreen.Calendar.shortTitle)
    }

    @Test
    fun appScreenNotesHasCorrectTitles() {
        assertEquals("Notes", AppScreen.Notes.title)
        assertEquals("Notes", AppScreen.Notes.shortTitle)
    }

    @Test
    fun appScreenTimerHasCorrectTitles() {
        assertEquals("Focus Timer", AppScreen.Timer.title)
        assertEquals("Timer", AppScreen.Timer.shortTitle)
    }

    @Test
    fun appScreenStatisticsHasCorrectTitles() {
        assertEquals("Statistics", AppScreen.Statistics.title)
        assertEquals("Stats", AppScreen.Statistics.shortTitle)
    }

    @Test
    fun appScreenSettingsHasCorrectTitles() {
        assertEquals("Settings", AppScreen.Settings.title)
        assertEquals("Settings", AppScreen.Settings.shortTitle)
    }

    @Test
    fun allAppScreensHaveUniqueIds() {
        val screens = AppScreen.entries
        val ids = screens.map { it.ordinal }
        assertEquals(ids.distinct().size, ids.size)
    }

    @Test
    fun appScreenValuesContainsTenScreens() {
        assertEquals(10, AppScreen.entries.size)
    }

    @Test
    fun appScreenValueOfWorks() {
        assertEquals(AppScreen.Dashboard, AppScreen.valueOf("Dashboard"))
        assertEquals(AppScreen.Timer, AppScreen.valueOf("Timer"))
    }
}

