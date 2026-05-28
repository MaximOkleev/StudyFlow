package studyflow.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import studyflow.data.StudyRepository
import studyflow.domain.model.TaskStatus
import studyflow.presentation.components.HorizontalBars
import studyflow.presentation.components.ScreenScaffold
import studyflow.presentation.components.SimpleBarChart
import studyflow.presentation.components.StatCard

@Composable
fun StatisticsScreen(repository: StudyRepository) {
    ScreenScaffold(title = "Statistics", subtitle = "Simple analytics from tasks and focus sessions.") {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Subjects", repository.subjects.size.toString(), "Active learning areas", Modifier.weight(1f))
                StatCard("Tasks", repository.tasks.size.toString(), "Total planned work", Modifier.weight(1f))
                StatCard("Done", repository.doneTasks().toString(), "Completed tasks", Modifier.weight(1f))
                StatCard("Focus", "${repository.totalFocusMinutes()}m", "Tracked minutes", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxSize()) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827))) { Column(Modifier.padding(18.dp)) { Text("Done tasks this week", color = Color.White); SimpleBarChart(repository.weeklyDoneCounts()) } }
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827))) { Column(Modifier.padding(18.dp)) { Text("Focus minutes by subject", color = Color.White); val items = repository.focusMinutesBySubject().map { repository.subjectName(it.key) to it.value }.sortedByDescending { it.second }; HorizontalBars(items); items.forEach { Text("${it.first}: ${it.second}m", color = Color(0xFF9CA3AF)) } } }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Subject progress", color = Color.White, fontWeight = FontWeight.Bold)
                    repository.subjects.forEach { subject ->
                        val progress = repository.subjectProgress(subject.id)
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827)), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp)) {
                                Text(subject.name, color = Color.White)
                                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                                val done = repository.tasksForSubject(subject.id).count { it.status == TaskStatus.Done }
                                Text("$done/${repository.tasksForSubject(subject.id).size} tasks done", color = Color(0xFF9CA3AF))
                            }
                        }
                    }
                }
            }
        }
    }
}
