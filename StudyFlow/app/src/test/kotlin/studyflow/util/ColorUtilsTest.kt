package studyflow.util

import kotlin.test.Test
import kotlin.test.assertEquals
import androidx.compose.ui.graphics.Color

class ColorUtilsTest {
    @Test
    fun colorFromHexParses6DigitColor() {
        val color = colorFromHex("#FF0000")
        assertEquals(1f, color.red, 0.01f)
        assertEquals(0f, color.green, 0.01f)
        assertEquals(0f, color.blue, 0.01f)
        assertEquals(1f, color.alpha, 0.01f)
    }

    @Test
    fun colorFromHexHandlesMissingHash() {
        val color1 = colorFromHex("7C3AED")
        val color2 = colorFromHex("#7C3AED")
        assertEquals(color1.red, color2.red, 0.01f)
        assertEquals(color1.green, color2.green, 0.01f)
        assertEquals(color1.blue, color2.blue, 0.01f)
    }

    @Test
    fun colorFromHexDefaultsToDefaultColorForInvalid() {
        val color = colorFromHex("GGGGGG")
        val default = colorFromHex("#7C3AED")
        assertEquals(color.red, default.red, 0.01f)
        assertEquals(color.green, default.green, 0.01f)
        assertEquals(color.blue, default.blue, 0.01f)
    }

    @Test
    fun colorFromHexPadsPaddleValue() {
        val color = colorFromHex("F00")
        assertEquals(0.94f, color.red, 0.01f)
        assertEquals(0f, color.green, 0.01f)
        assertEquals(0f, color.blue, 0.01f)
    }

    @Test
    fun safeColorHexValidatesCorrectly() {
        assertEquals("#FF0000", safeColorHex("FF0000"))
        assertEquals("#7C3AED", safeColorHex("GGGGGG"))
        assertEquals("#7C3AED", safeColorHex(""))
        assertEquals("#0000FF", safeColorHex("  0000FF  "))
    }

    @Test
    fun safeColorHexHandlesHashPrefix() {
        assertEquals("#FF0000", safeColorHex("#FF0000"))
        val result = safeColorHex("##FF0000")
        assertEquals("#7C3AED", result)
    }

    @Test
    fun safeColorHexRequires6Digits() {
        assertEquals("#7C3AED", safeColorHex("FFF"))
        assertEquals("#7C3AED", safeColorHex("FF00"))
        assertEquals("#FF0000", safeColorHex("FF0000"))
    }
}

