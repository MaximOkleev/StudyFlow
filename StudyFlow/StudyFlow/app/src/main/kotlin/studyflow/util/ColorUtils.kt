package studyflow.util

import androidx.compose.ui.graphics.Color

fun colorFromHex(hex: String): Color {
    val clean = hex.removePrefix("#").take(6).padEnd(6, '0')
    val value = clean.toLongOrNull(16) ?: 0x7C3AED
    return Color(
        red = ((value shr 16) and 0xFF) / 255f,
        green = ((value shr 8) and 0xFF) / 255f,
        blue = (value and 0xFF) / 255f,
        alpha = 1f
    )
}

fun safeColorHex(value: String): String {
    val clean = value.trim().removePrefix("#")
    return if (clean.length == 6 && clean.all { it in '0'..'9' || it.lowercaseChar() in 'a'..'f' }) "#$clean" else "#7C3AED"
}
