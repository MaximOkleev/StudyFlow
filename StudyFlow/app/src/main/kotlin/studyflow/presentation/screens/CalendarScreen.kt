package studyflow.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import studyflow.data.StudyRepository
import studyflow.presentation.components.EmptyState
import studyflow.presentation.components.ScreenScaffold
import studyflow.presentation.components.TaskCard
import studyflow.util.DateUtils
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarScreen(repository: StudyRepository) {
    var month by remember { mutableStateOf(YearMonth.now()) }
    var selected by remember { mutableStateOf(DateUtils.today()) }
    val selectedTasks = repository.tasks.filter { it.deadlineAt != null && DateUtils.millisToDate(it.deadlineAt) == selected }
    ScreenScaffold(
        title = "Calendar",
        subtitle = "Month view for task deadlines.",
        action = { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = { month = month.minusMonths(1) }) { Text("<") }; Button(onClick = { month = YearMonth.now(); selected = DateUtils.today() }) { Text("Today") }; Button(onClick = { month = month.plusMonths(1) }) { Text(">") } } }
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxSize()) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827)), shape = RoundedCornerShape(24.dp), modifier = Modifier.weight(1.2f)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(month.toString(), color = Color.White, fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { Text(it, color = Color(0xFF9CA3AF), modifier = Modifier.weight(1f)) } }
                    DateUtils.monthGrid(month).chunked(7).forEach { week ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            week.forEach { date -> DayCell(date, selected, repository, onSelect = { selected = it }, Modifier.weight(1f)) }
                        }
                    }
                }
            }
            Column(Modifier.weight(0.8f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${selected.dayOfMonth} ${selected.month}", color = Color.White, fontWeight = FontWeight.Bold)
                if (selectedTasks.isEmpty()) EmptyState("No deadlines on selected date.") else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(selectedTasks) { task -> TaskCard(task, repository.subjectName(task.subjectId), onCycleStatus = { repository.cycleTaskStatus(task.id) }, onEdit = {}, onDelete = { repository.deleteTask(task.id) }) }
                }
            }
        }
    }
}

@Composable
private fun DayCell(date: LocalDate?, selected: LocalDate, repository: StudyRepository, onSelect: (LocalDate) -> Unit, modifier: Modifier) {
    if (date == null) {
        Box(modifier.height(72.dp))
        return
    }
    val count = repository.tasks.count { it.deadlineAt != null && DateUtils.millisToDate(it.deadlineAt) == date }
    val isSelected = date == selected
    val isToday = date == DateUtils.today()
    Box(
        modifier = modifier.height(72.dp).clip(RoundedCornerShape(16.dp)).background(if (isSelected) Color(0xFF7C3AED).copy(alpha = 0.35f) else Color(0xFF171E2E)).clickable { onSelect(date) }.padding(8.dp)
    ) {
        Column {
            Text(date.dayOfMonth.toString(), color = if (isToday) Color(0xFF22D3EE) else Color.White, fontWeight = FontWeight.Bold)
            if (count > 0) Text("$count tasks", color = Color(0xFFB9C0D4))
        }
    }
}
