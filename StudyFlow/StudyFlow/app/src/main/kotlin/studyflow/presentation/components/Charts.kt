package studyflow.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SimpleBarChart(values: List<Int>, modifier: Modifier = Modifier, color: Color = Color(0xFF8B5CF6)) {
    Canvas(modifier = modifier.fillMaxWidth().height(180.dp)) {
        if (values.isEmpty()) return@Canvas
        val max = values.maxOrNull()?.coerceAtLeast(1) ?: 1
        val gap = 10f
        val barWidth = (size.width - gap * (values.size - 1)) / values.size
        values.forEachIndexed { index, value ->
            val normalized = value.toFloat() / max
            val barHeight = (size.height * normalized).coerceAtLeast(8f)
            val x = index * (barWidth + gap)
            drawRoundRect(
                color = color.copy(alpha = 0.9f),
                topLeft = Offset(x, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(14f, 14f)
            )
        }
    }
}

@Composable
fun HorizontalBars(items: List<Pair<String, Int>>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().height((items.size.coerceAtLeast(1) * 34).dp)) {
        val max = items.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1
        items.forEachIndexed { index, (_, value) ->
            val y = index * 34f
            val width = size.width * (value.toFloat() / max)
            drawRoundRect(
                color = Color(0xFF22D3EE).copy(alpha = 0.8f),
                topLeft = Offset(0f, y),
                size = Size(width.coerceAtLeast(8f), 20f),
                cornerRadius = CornerRadius(12f, 12f)
            )
        }
    }
}
