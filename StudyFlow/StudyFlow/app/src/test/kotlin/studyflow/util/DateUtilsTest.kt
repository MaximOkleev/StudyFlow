package studyflow.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DateUtilsTest {
    @Test
    fun parseIsoDateRejectsInvalidDate() {
        assertNotNull(DateUtils.parseIsoDateToMillis("2026-05-28"))
        assertNull(DateUtils.parseIsoDateToMillis("28.05.2026"))
    }

    @Test
    fun formatIsoRoundtripForParsedDate() {
        val millis = DateUtils.parseIsoDateToMillis("2026-05-28")
        assertEquals("2026-05-28", DateUtils.formatIso(millis))
    }
}
