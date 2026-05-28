package studyflow.presentation.components

import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import studyflow.AppScreen

@Composable
fun Sidebar(current: AppScreen, compact: Boolean = false, onSelect: (AppScreen) -> Unit) {
    Column(
        modifier = Modifier
            .width(if (compact) 88.dp else 236.dp)
            .fillMaxHeight()
            .background(Color(0xFF070A12))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StudyFlowLogo(modifier = Modifier.size(44.dp))
            if (!compact) {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text("StudyFlow", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    Text("учебный планер", color = Color(0xFF8B93A7), fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        AppScreen.entries.forEach { item ->
            val selected = item == current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f) else Color.Transparent)
                    .clickable { onSelect(item) }
                    .testTag("sidebar.${item.name.lowercase()}")
                    .padding(horizontal = if (compact) 10.dp else 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = iconFor(item),
                    color = if (selected) Color.White else Color(0xFF8B93A7),
                    modifier = Modifier.width(30.dp)
                )
                if (!compact) {
                    Text(
                        text = item.shortTitle,
                        color = if (selected) Color.White else Color(0xFFB9C0D4),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
        Spacer(Modifier.weight(1f))
        if (!compact) Text("Local-first desktop app", color = Color(0xFF6B7280), fontSize = 12.sp)
    }
}

private fun iconFor(screen: AppScreen): String = when (screen) {
    AppScreen.Dashboard -> "⌂"
    AppScreen.Subjects -> "▦"
    AppScreen.Tasks -> "✓"
    AppScreen.Calendar -> "◷"
    AppScreen.Session -> "⚑"
    AppScreen.Board -> "▤"
    AppScreen.Habits -> "◆"
    AppScreen.Notes -> "✎"
    AppScreen.Timer -> "◉"
    AppScreen.Statistics -> "↗"
    AppScreen.Settings -> "⚙"
}


@Composable
private fun StudyFlowLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF22D3EE), Color(0xFF3B82F6), Color(0xFF7C3AED), Color(0xFF111827)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            ),
            size = Size(w, h),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.28f, h * 0.28f)
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.94f),
            topLeft = Offset(w * 0.22f, h * 0.18f),
            size = Size(w * 0.56f, h * 0.64f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.12f, h * 0.12f)
        )
        drawRoundRect(
            color = Color(0xFF0F172A).copy(alpha = 0.18f),
            topLeft = Offset(w * 0.31f, h * 0.31f),
            size = Size(w * 0.38f, h * 0.07f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.035f, h * 0.035f)
        )
        drawRoundRect(
            color = Color(0xFF0F172A).copy(alpha = 0.12f),
            topLeft = Offset(w * 0.31f, h * 0.45f),
            size = Size(w * 0.30f, h * 0.06f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.03f, h * 0.03f)
        )
        val flow = Path().apply {
            moveTo(w * 0.30f, h * 0.67f)
            cubicTo(w * 0.42f, h * 0.50f, w * 0.53f, h * 0.69f, w * 0.66f, h * 0.52f)
        }
        drawPath(
            path = flow,
            color = Color(0xFF3B82F6),
            style = Stroke(width = w * 0.08f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawCircle(Color(0xFF22D3EE), radius = w * 0.065f, center = Offset(w * 0.30f, h * 0.67f))
        drawCircle(Color(0xFF8B5CF6), radius = w * 0.065f, center = Offset(w * 0.66f, h * 0.52f))
        drawCircle(Color(0xFF0B1220), radius = w * 0.17f, center = Offset(w * 0.70f, h * 0.72f))
        val check = Path().apply {
            moveTo(w * 0.61f, h * 0.72f)
            lineTo(w * 0.68f, h * 0.79f)
            lineTo(w * 0.82f, h * 0.62f)
        }
        drawPath(
            path = check,
            color = Color(0xFF34D399),
            style = Stroke(width = w * 0.07f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
