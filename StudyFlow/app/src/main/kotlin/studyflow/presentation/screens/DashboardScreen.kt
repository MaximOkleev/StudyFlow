package studyflow.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import studyflow.data.StudyRepository
import studyflow.presentation.components.EmptyState
import studyflow.presentation.components.ScreenScaffold
import studyflow.presentation.components.SimpleBarChart
import studyflow.presentation.components.StatCard
import studyflow.presentation.components.TaskCard

@Composable
fun DashboardScreen(repository: StudyRepository, onOpenTasks: () -> Unit, onOpenTimer: () -> Unit) {
    ScreenScaffold(
        title = "Dashboard",
        subtitle = "Today, deadlines, focus and weak spots.",
        action = { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Button(onClick = onOpenTasks) { Text("Open tasks") }; Button(onClick = onOpenTimer) { Text("Start focus") } } }
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Active tasks", repository.activeTasks().toString(), "Not completed", Modifier.weight(1f))
                StatCard("Today", repository.todayTasks().size.toString(), "Due today", Modifier.weight(1f))
                StatCard("Overdue", repository.overdueTasks().toString(), "Needs attention", Modifier.weight(1f))
                StatCard("Focus", "${repository.totalFocusMinutes()}m", "Total logged", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxSize()) {
                Column(Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Upcoming work", color = Color.White)
                    val upcoming = repository.upcomingTasks()
                    if (upcoming.isEmpty()) EmptyState(if (repository.subjects.isEmpty()) "Clean workspace. Add your first subject and task to start." else "No upcoming tasks. Good state, but do not relax too early.") else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(upcoming) { task ->
                            TaskCard(task, repository.subjectName(task.subjectId), onCycleStatus = { repository.cycleTaskStatus(task.id) }, onEdit = onOpenTasks, onDelete = { repository.deleteTask(task.id) })
                        }
                    }
                }
                Column(Modifier.weight(0.8f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827))) {
                        Column(Modifier.padding(18.dp)) {
                            Text("Done tasks this week", color = Color.White)
                            SimpleBarChart(repository.weeklyDoneCounts())
                        }
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827))) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Quick diagnosis", color = Color.White)
                            val worst = repository.subjects.minByOrNull { repository.subjectProgress(it.id) }
                            Text("Weakest subject: ${worst?.name ?: "none"}", color = Color(0xFFB9C0D4))
                            repository.nextExam()?.let { exam ->
                                Text("Next session event: ${exam.subjectName}", color = Color(0xFFFBBF24))
                                Text("${studyflow.util.DateUtils.formatTimeRange(exam.startAt, exam.endAt)} • ${exam.teachers}", color = Color(0xFF9CA3AF))
                            }
                            Text("Rule: close overdue tasks first, then high-priority tasks, then everything else.", color = Color(0xFF9CA3AF))
                        }
                    }
                }
            }
        }
    }
}
