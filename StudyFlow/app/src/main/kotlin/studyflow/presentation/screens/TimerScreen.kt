@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package studyflow.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import studyflow.data.StudyRepository
import studyflow.presentation.components.ScreenScaffold

@Composable
fun TimerScreen(repository: StudyRepository) {
    var presetMinutes by remember { mutableIntStateOf(25) }
    var remainingSeconds by remember { mutableIntStateOf(presetMinutes * 60) }
    var running by remember { mutableStateOf(false) }
    var selectedSubjectId by remember { mutableStateOf(repository.subjects.firstOrNull()?.id) }
    var selectedTaskId by remember { mutableStateOf(repository.tasks.firstOrNull()?.id) }

    LaunchedEffect(running, remainingSeconds) {
        if (running && remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        } else if (running && remainingSeconds <= 0) {
            running = false
            repository.logFocusSession(selectedTaskId, selectedSubjectId, presetMinutes)
            remainingSeconds = presetMinutes * 60
        }
    }

    ScreenScaffold(title = "Focus Timer", subtitle = "Log study sessions and connect them to subjects or tasks.") {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(15, 25, 50, 90).forEach { minutes -> FilterChip(selected = presetMinutes == minutes, onClick = { presetMinutes = minutes; remainingSeconds = minutes * 60; running = false }, label = { Text("${minutes}m") }) } }
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827)), modifier = Modifier.fillMaxWidth(0.7f)) {
                Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    val mm = remainingSeconds / 60
                    val ss = remainingSeconds % 60
                    Text("%02d:%02d".format(mm, ss), color = Color.White, fontSize = 72.sp, fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(progress = { 1f - remainingSeconds.toFloat() / (presetMinutes * 60).coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth().height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { running = !running }) { Text(if (running) "Pause" else "Start") }
                        OutlinedButton(onClick = { running = false; remainingSeconds = presetMinutes * 60 }) { Text("Reset") }
                        OutlinedButton(onClick = { repository.logFocusSession(selectedTaskId, selectedSubjectId, ((presetMinutes * 60 - remainingSeconds) / 60).coerceAtLeast(1)); running = false; remainingSeconds = presetMinutes * 60 }) { Text("Log now") }
                    }
                }
            }
            Text("Subject", color = Color.White)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) { repository.subjects.take(6).forEach { s -> FilterChip(selected = selectedSubjectId == s.id, onClick = { selectedSubjectId = s.id }, label = { Text(s.name) }) } }
            Text("Task", color = Color.White)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) { repository.tasks.take(5).forEach { t -> FilterChip(selected = selectedTaskId == t.id, onClick = { selectedTaskId = t.id; selectedSubjectId = t.subjectId }, label = { Text(t.title.take(18)) }) } }
            Spacer(Modifier.height(10.dp))
            Text("Logged total: ${repository.totalFocusMinutes()} minutes", color = Color(0xFF9CA3AF))
        }
    }
}
