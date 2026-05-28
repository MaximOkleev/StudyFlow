package studyflow.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
    var confirmClear by remember { mutableStateOf(false) }
    var confirmRestore by remember { mutableStateOf(false) }
    var csvImportPath by remember { mutableStateOf("") }
    var mdImportPath by remember { mutableStateOf("") }
    var subjectImportPath by remember { mutableStateOf("") }

    ScreenScaffold(title = "Settings", subtitle = "Local storage, export and project maintenance.") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Storage", color = Color.White)
                    Text(repository.lastMessage, color = Color(0xFF9CA3AF))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { repository.saveNow() }) { Text("Save now") }
                        Button(onClick = { repository.loadSemesterSubjects() }, modifier = Modifier.testTag("settings.load.semester.subjects")) { Text("Load semester subjects") }
                        Button(onClick = { repository.loadExamSchedule() }, modifier = Modifier.testTag("settings.load.exam.schedule")) { Text("Load session schedule") }
                        OutlinedButton(onClick = { confirmClear = true }, modifier = Modifier.testTag("settings.clear.all")) { Text("Clear all data") }
                    }
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Export", color = Color.White)
                    Text("Files are written to the local StudyFlow data folder.", color = Color(0xFF9CA3AF))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { repository.exportSubjectsCsv() }) { Text("Subjects CSV") }
                        Button(onClick = { repository.exportExamsCsv() }) { Text("Events CSV") }
                        Button(onClick = { repository.exportTasksCsv() }) { Text("Tasks CSV") }
                        Button(onClick = { repository.exportNotesMarkdown() }) { Text("Notes Markdown") }
                        Button(onClick = { repository.exportHabitsCsv() }) { Text("Habits CSV") }
                        Button(onClick = { repository.exportBackup() }) { Text("Readable backup") }
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Import", color = Color.White)
                    Text("Paste an absolute path to a CSV/Markdown file, then import it into the SQLite database.", color = Color(0xFF9CA3AF))
                    OutlinedTextField(subjectImportPath, { subjectImportPath = it }, label = { Text("Subjects CSV path") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { repository.importSubjectsCsv(subjectImportPath) }) { Text("Import subjects CSV") }
                    OutlinedTextField(csvImportPath, { csvImportPath = it }, label = { Text("Tasks CSV path") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { repository.importTasksCsv(csvImportPath) }) { Text("Import tasks CSV") }
                    OutlinedTextField(mdImportPath, { mdImportPath = it }, label = { Text("Notes Markdown path") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { repository.importNotesMarkdown(mdImportPath) }) { Text("Import notes Markdown") }
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
            Text("Storage is SQLite-first now. Raw backup creates a copy of studyflow.sqlite.", color = Color(0xFF6B7280))
        }
    }

    if (confirmClear) {
        ConfirmDialog(
            title = "Clear all data?",
            text = "This will delete all local subjects, tasks, notes, habits, session schedule and focus sessions. The app will stay empty so you can add everything manually.",
            confirmText = "Clear",
            onConfirm = { repository.clearAllData() },
            onDismiss = { confirmClear = false }
        )
    }

    if (confirmRestore) {
        ConfirmDialog(
            title = "Restore raw backup?",
            text = "This will replace current local data with studyflow_raw_backup.sqlite if it exists.",
            confirmText = "Restore",
            onConfirm = { repository.restoreRawBackup() },
            onDismiss = { confirmRestore = false }
        )
    }
}
