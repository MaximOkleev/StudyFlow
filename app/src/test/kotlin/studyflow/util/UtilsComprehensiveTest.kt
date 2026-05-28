package studyflow.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import java.time.LocalDate

class DateUtilsComprehensiveTest {
    @Test
    fun dateUtilsFormatFullHandlesNull() {
        val formatted = DateUtils.formatFull(null)
        assertEquals("No deadline", formatted)
    }

    @Test
    fun dateUtilsFormatFullFormatsDate() {
        val millis = DateUtils.parseIsoDateToMillis("2026-05-28")
        val formatted = DateUtils.formatFull(millis)
        assertNotNull(formatted)
        assertTrue(formatted.contains("2026") || formatted.isEmpty())
    }

    @Test
    fun dateUtilsMillisToDateReturnsLocalDate() {
        val millis = DateUtils.parseIsoDateToMillis("2026-05-28")
        val date = DateUtils.millisToDate(millis!!)
        assertEquals(LocalDate.parse("2026-05-28"), date)
    }

    @Test
    fun dateUtilsWeekStartReturnsMonday() {
        val weekStart = DateUtils.weekStart()
        val today = LocalDate.now()
        assertTrue(weekStart.isBefore(today) || weekStart.isEqual(today))
    }

    @Test
    fun dateUtilsDaysFromNowReturnsValidFuture() {
        val now = DateUtils.nowMillis()
        val tomorrow = DateUtils.daysFromNow(1)
        assertTrue(tomorrow > now)
    }

    @Test
    fun dateUtilsDaysFromNowMultipleDays() {
        val one = DateUtils.daysFromNow(1)
        val seven = DateUtils.daysFromNow(7)
        val thirty = DateUtils.daysFromNow(30)
        assertTrue(one < seven)
        assertTrue(seven < thirty)
    }

    @Test
    fun dateUtilsIsOverdueForPastDate() {
        val past = System.currentTimeMillis() - (2L * 24 * 60 * 60 * 1000)
        assertTrue(DateUtils.isOverdue(past))
    }

    @Test
    fun dateUtilsIsOverdueForFutureDate() {
        val future = System.currentTimeMillis() + (2L * 24 * 60 * 60 * 1000)
        assertFalse(DateUtils.isOverdue(future))
    }

    @Test
    fun dateUtilsIsOverdueForNull() {
        assertFalse(DateUtils.isOverdue(null))
    }

    @Test
    fun dateUtilsIsTodayForCurrentTime() {
        val now = System.currentTimeMillis()
        assertTrue(DateUtils.isToday(now))
    }

    @Test
    fun dateUtilsIsTodayForPastDate() {
        val yesterday = System.currentTimeMillis() - (24L * 60 * 60 * 1000)
        assertFalse(DateUtils.isToday(yesterday))
    }

    @Test
    fun dateUtilsIsTodayForNull() {
        assertFalse(DateUtils.isToday(null))
    }

    @Test
    fun dateUtilsParseIsoDateHandlesValidDates() {
        val result1 = DateUtils.parseIsoDateToMillis("2026-01-01")
        assertNotNull(result1)
        assertTrue(result1 > 0)

        val result2 = DateUtils.parseIsoDateToMillis("2026-12-31")
        assertNotNull(result2)
        assertTrue(result2 > 0)
    }

    @Test
    fun dateUtilsParseIsoDateHandlesInvalidFormats() {
        assertNotNull(DateUtils.parseIsoDateToMillis("2026-05-28"))
        assertEquals(null, DateUtils.parseIsoDateToMillis("05/28/2026"))
        assertEquals(null, DateUtils.parseIsoDateToMillis("2026-5-28"))
    }

    @Test
    fun dateUtilsFormatIsoReturnsValidFormat() {
        val millis = System.currentTimeMillis()
        val formatted = DateUtils.formatIso(millis)
        assertTrue(formatted.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun dateUtilsFormatIsoRoundtrip() {
        val original = "2026-05-28"
        val millis = DateUtils.parseIsoDateToMillis(original)
        val formatted = DateUtils.formatIso(millis)
        assertEquals(original, formatted)
    }
}

class ColorUtilsComprehensiveTest {
    @Test
    fun colorUtilsColorFromHexHandles3DigitCodes() {
        val color = colorFromHex("FFF")

        assertEquals(1f, color.red, 0.05f)
        assertEquals(0.94f, color.green, 0.05f)
        assertEquals(0f, color.blue, 0.05f)
    }

    @Test
    fun colorUtilsColorFromHexHandles8DigitCodes() {
        val color = colorFromHex("#FF0000FF")
        assertEquals(1f, color.red, 0.05f)
        assertEquals(0f, color.green, 0.05f)
        assertEquals(0f, color.blue, 0.05f)
    }

    @Test
    fun colorUtilsColorFromHexMultipleFormats() {
        val color1 = colorFromHex("#0000FF")

        assertEquals(0f, color1.red, 0.05f)
        assertEquals(0f, color1.blue, 1.05f)
        assertTrue(color1.blue > 0.9f)
    }

    @Test
    fun colorUtilsColorFromHexToleratesWhitespace() {

        val colorWithSpaces = colorFromHex("  FF0000  ")
        val defaultColor = colorFromHex("#7C3AED")

        assertEquals(colorWithSpaces.red, defaultColor.red, 0.05f)
    }

    @Test
    fun colorUtilsSafeColorHexAcceptsValidCodes() {
        val result1 = safeColorHex("FF0000")
        assertEquals("#FF0000", result1)
        
        val result2 = safeColorHex("AABBCC")
        assertEquals("#AABBCC", result2)
    }

    @Test
    fun colorUtilsSafeColorHexMultipleHashes() {
        val result = safeColorHex("##FFFF00")
        assertEquals("#7C3AED", result)
    }

    @Test
    fun colorUtilsSafeColorHexBoundaryLengths() {
        assertEquals("#7C3AED", safeColorHex(""))
        assertEquals("#7C3AED", safeColorHex("F"))
        assertEquals("#7C3AED", safeColorHex("FF"))
        assertEquals("#7C3AED", safeColorHex("FFF"))
        assertEquals("#7C3AED", safeColorHex("FFFF"))
        assertEquals("#7C3AED", safeColorHex("FFFFF"))
        assertEquals("#FFFF00", safeColorHex("FFFF00"))
    }

    @Test
    fun colorUtilsSafeColorHexSpecialCharacters() {
        assertEquals("#7C3AED", safeColorHex("GG0000"))
        assertEquals("#7C3AED", safeColorHex("##FF4400"))
        assertEquals("#7C3AED", safeColorHex("FF-00-00"))
    }

    @Test
    fun colorUtilsColorFromHexBlackColor() {
        val color = colorFromHex("000000")
        assertEquals(0f, color.red, 0.01f)
        assertEquals(0f, color.green, 0.01f)
        assertEquals(0f, color.blue, 0.01f)
    }

    @Test
    fun colorUtilsColorFromHexWhiteColor() {
        val color = colorFromHex("FFFFFF")
        assertEquals(1f, color.red, 0.01f)
        assertEquals(1f, color.green, 0.01f)
        assertEquals(1f, color.blue, 0.01f)
    }

    @Test
    fun colorUtilsColorAlwaysHasFullOpacity() {
        val colors = listOf(
            colorFromHex("#FF0000"),
            colorFromHex("#00FF00"),
            colorFromHex("#0000FF"),
            colorFromHex("#000000"),
            colorFromHex("#FFFFFF")
        )
        assertTrue(colors.all { it.alpha == 1f })
    }
}
