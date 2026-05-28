package studyflow.presentation.components

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
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            }
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
