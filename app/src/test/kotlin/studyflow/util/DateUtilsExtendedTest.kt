package studyflow.util

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class DateUtilsExtendedTest {
    @Test
    fun nowMillisReturnsPositiveValue() {
        val now = DateUtils.nowMillis()
        assertTrue(now > 0)
    }

    @Test
    fun daysFromNowReturnsGreaterTimestamp() {
        val now = DateUtils.nowMillis()
        val future = DateUtils.daysFromNow(1)
        assertTrue(future > now)
    }

    @Test
    fun daysFromNowForFiveDaysIsGreaterThanOne() {
        val oneDay = DateUtils.daysFromNow(1)
        val fiveDays = DateUtils.daysFromNow(5)
        assertTrue(fiveDays > oneDay)
    }

    @Test
    fun weekStartReturnsValidDate() {
        val weekStart = DateUtils.weekStart()
        assertNotNull(weekStart)
    }

    @Test
    fun millisToDateFormatsCorrectly() {
        val millis = DateUtils.parseIsoDateToMillis("2026-05-28")
        val result = DateUtils.millisToDate(millis!!)
        assertEquals("2026-05-28", DateUtils.formatIso(millis))
    }

    @Test
    fun formatIsoHandlesNull() {
        val result = DateUtils.formatIso(null)
        assertEquals("", result)
    }

    @Test
    fun isOverdueWorksCorrectly() {
        val pastDate = System.currentTimeMillis() - (10 * 24 * 60 * 60 * 1000)
        assertTrue(DateUtils.isOverdue(pastDate))

        val futureDate = System.currentTimeMillis() + (10 * 24 * 60 * 60 * 1000)
        assertFalse(DateUtils.isOverdue(futureDate))
    }

    @Test
    fun isTodayWorksCorrectly() {
        val today = System.currentTimeMillis()
        assertTrue(DateUtils.isToday(today))

        val pastDate = System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000)
        assertFalse(DateUtils.isToday(pastDate))
    }
}

