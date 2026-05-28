package studyflow.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import studyflow.data.StudyRepository
import studyflow.presentation.components.ScreenScaffold

@Composable
fun SettingsScreen(repository: StudyRepository) {
    ScreenScaffold(title = "Settings", subtitle = "Local storage, export and project maintenance.") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Storage", color = Color.White)
                    Text(repository.lastMessage, color = Color(0xFF9CA3AF))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { repository.saveNow() }) { Text("Save now") }
                        OutlinedButton(onClick = { repository.resetDemoData() }) { Text("Reset demo data") }
                    }
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Export", color = Color.White)
                    Text("Files are written to the local StudyFlow data folder.", color = Color(0xFF9CA3AF))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { repository.exportTasksCsv() }) { Text("Tasks CSV") }
                        Button(onClick = { repository.exportNotesMarkdown() }) { Text("Notes Markdown") }
                        Button(onClick = { repository.exportBackup() }) { Text("Full backup") }
                    }
                }
            }
            Text("Next serious upgrade: replace the current simple local store with SQLite/SQLDelight after the UI is stable.", color = Color(0xFF6B7280))
        }
    }
}
