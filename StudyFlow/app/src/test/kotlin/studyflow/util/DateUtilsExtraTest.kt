package studyflow.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import java.time.LocalDate

class DateUtilsExtraTest {
    @Test
    fun formatShortReturnsNoDeadlineForNull() {
        assertEquals("No deadline", DateUtils.formatShort(null))
    }

    @Test
    fun formatShortFormatsKnownDate() {
        val millis = DateUtils.parseIsoDateToMillis("2026-05-28")!!
        val shortened = DateUtils.formatShort(millis)
        assertTrue(shortened.matches(Regex("\\d{2}\\s+[A-Za-z]{3}")))
    }

    @Test
    fun dateToMillisAndMillisToDateAreInverse() {
        val date = LocalDate.of(2026, 5, 28)
        val millis = DateUtils.dateToMillis(date)
        val back = DateUtils.millisToDate(millis)
        assertEquals(date, back)
    }

    @Test
    fun todayStartMillisReturnsStartOfDay() {
        val start = DateUtils.todayStartMillis()
        val date = DateUtils.millisToDate(start)
        assertEquals(DateUtils.today(), date)
    }

    @Test
    fun isThisWeekTrueForWithin7Days() {
        val tomorrow = DateUtils.daysFromNow(1)
        assertTrue(DateUtils.isThisWeek(tomorrow))
    }

    @Test
    fun isThisWeekFalseForBeyond7Days() {
        val tenDays = DateUtils.daysFromNow(10)
        assertFalse(DateUtils.isThisWeek(tenDays))
    }

    @Test
    fun monthGridContainsAllDaysAndPadsToWeeks() {
        val month = java.time.YearMonth.of(2026, 2)
        val grid = DateUtils.monthGrid(month)

        assertTrue(grid.size % 7 == 0)
        val days = grid.filterNotNull()
        assertEquals(month.lengthOfMonth(), days.size)
    }

    @Test
    fun weekStartReturnsMondayOrEarlier() {
        val start = DateUtils.weekStart()
        assertTrue(start.dayOfWeek.value in 1..7)
    }
}

