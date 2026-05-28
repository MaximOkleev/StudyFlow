package studyflow.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import studyflow.data.StudyRepository
import studyflow.presentation.components.EmptyState
import studyflow.presentation.components.ExamCard
import studyflow.presentation.components.ScreenScaffold
import studyflow.presentation.components.StatCard
import studyflow.util.DateUtils
import java.time.temporal.ChronoUnit

@Composable
fun ExamSessionScreen(repository: StudyRepository) {
    val exams = repository.exams.sortedBy { it.startAt }
    val next = repository.nextExam()
    ScreenScaffold(
        title = "Session",
        subtitle = "Сессия: предмет, дата, время и преподаватель без аудиторий и типа контроля.",
        action = { Button(onClick = { repository.loadExamSchedule() }) { Text("Load session schedule") } }
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Events", exams.size.toString(), "Loaded session events", Modifier.weight(1f))
                StatCard("Next event", next?.let { DateUtils.formatShort(it.startAt) } ?: "None", next?.let { "${it.subjectName} • ${DateUtils.formatTimeRange(it.startAt, it.endAt)}" } ?: "No upcoming events", Modifier.weight(2f))
                StatCard("Days left", next?.let { daysLeftText(it.startAt) } ?: "—", "Until nearest event", Modifier.weight(1f))
            }
            if (exams.isEmpty()) {
                EmptyState("Session schedule is empty.", "Load session schedule") { repository.loadExamSchedule() }
            } else {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827)), modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Text("Session timeline", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                    }
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                    items(exams) { exam -> ExamCard(exam) }
                }
            }
        }
    }
}

private fun daysLeftText(startAt: Long): String {
    val today = DateUtils.today()
    val examDate = DateUtils.millisToDate(startAt)
    return ChronoUnit.DAYS.between(today, examDate).coerceAtLeast(0).toString()
}
