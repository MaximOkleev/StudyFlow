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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import studyflow.data.StudyRepository
import studyflow.presentation.components.ScreenScaffold
import studyflow.presentation.dialogs.ConfirmDialog

@Composable
fun SettingsScreen(repository: StudyRepository) {
    var confirmReset by remember { mutableStateOf(false) }
    var confirmRestore by remember { mutableStateOf(false) }

    ScreenScaffold(title = "Settings", subtitle = "Local storage, export and project maintenance.") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Storage", color = Color.White)
                    Text(repository.lastMessage, color = Color(0xFF9CA3AF))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { repository.saveNow() }) { Text("Save now") }
                        OutlinedButton(onClick = { confirmReset = true }) { Text("Reset demo data") }
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
                        Button(onClick = { repository.exportBackup() }) { Text("Readable backup") }
                    }
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Restorable backup", color = Color.White)
                    Text("Creates a real backup of the local data file and can restore it later.", color = Color(0xFF9CA3AF))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { repository.exportRawBackup() }, modifier = Modifier.testTag("settings.export.raw")) { Text("Export raw backup") }
                        OutlinedButton(onClick = { confirmRestore = true }, modifier = Modifier.testTag("settings.restore.raw")) { Text("Restore raw backup") }
                    }
                }
            }
            Text("Good next serious upgrade: SQLite/SQLDelight persistence after the UI is stable.", color = Color(0xFF6B7280))
        }
    }

    if (confirmReset) {
        ConfirmDialog(
            title = "Reset demo data?",
            text = "This will replace current local subjects, tasks, notes and focus sessions with demo data.",
            confirmText = "Reset",
            onConfirm = { repository.resetDemoData() },
            onDismiss = { confirmReset = false }
        )
    }

    if (confirmRestore) {
        ConfirmDialog(
            title = "Restore raw backup?",
            text = "This will replace current local data with studyflow_raw_backup.properties if it exists.",
            confirmText = "Restore",
            onConfirm = { repository.restoreRawBackup() },
            onDismiss = { confirmRestore = false }
        )
    }
}
