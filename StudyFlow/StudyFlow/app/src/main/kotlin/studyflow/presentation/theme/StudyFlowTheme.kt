package studyflow.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8B5CF6),
    secondary = Color(0xFF22D3EE),
    tertiary = Color(0xFF10B981),
    background = Color(0xFF080B13),
    surface = Color(0xFF101522),
    surfaceVariant = Color(0xFF171E2E),
    onPrimary = Color.White,
    onSecondary = Color(0xFF071018),
    onBackground = Color(0xFFE5E7EB),
    onSurface = Color(0xFFE5E7EB),
    onSurfaceVariant = Color(0xFF9CA3AF)
)

@Composable
fun StudyFlowTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
