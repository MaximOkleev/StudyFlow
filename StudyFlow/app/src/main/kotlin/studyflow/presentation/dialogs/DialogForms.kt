@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package studyflow.presentation.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import studyflow.domain.model.Note
import studyflow.domain.model.Subject
import studyflow.domain.model.StudyTask
import studyflow.domain.model.TaskPriority
import studyflow.domain.model.TaskStatus

@Composable
fun SubjectDialog(initial: Subject?, onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var color by remember { mutableStateOf(initial?.colorHex ?: "#7C3AED") }
    var icon by remember { mutableStateOf(initial?.icon ?: "•") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New subject" else "Edit subject") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(description, { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(color, { color = it }, label = { Text("Color #RRGGBB") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(icon, { icon = it }, label = { Text("Icon") }, modifier = Modifier.weight(0.7f))
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(name, description, color, icon); onDismiss() }) { Text("Save") } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun TaskDialog(subjects: List<Subject>, initial: StudyTask?, onDismiss: () -> Unit, onSave: (Long, String, String, TaskStatus, TaskPriority, Int?, Int?) -> Unit) {
    var selectedSubjectId by remember { mutableStateOf(initial?.subjectId ?: subjects.firstOrNull()?.id ?: 0L) }
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var status by remember { mutableStateOf(initial?.status ?: TaskStatus.Todo) }
    var priority by remember { mutableStateOf(initial?.priority ?: TaskPriority.Medium) }
    var deadlineDays by remember { mutableStateOf(if (initial == null) "3" else "") }
    var estimate by remember { mutableStateOf(initial?.estimatedMinutes?.toString() ?: "60") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New task" else "Edit task") },
        text = {
            Column(Modifier.heightIn(max = 560.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Subject")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    subjects.take(4).forEach { s -> FilterChip(selected = selectedSubjectId == s.id, onClick = { selectedSubjectId = s.id }, label = { Text(s.name.take(10)) }) }
                }
                OutlinedTextField(title, { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(description, { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                Text("Status")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { TaskStatus.entries.forEach { FilterChip(selected = status == it, onClick = { status = it }, label = { Text(it.title) }) } }
                Text("Priority")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { TaskPriority.entries.forEach { FilterChip(selected = priority == it, onClick = { priority = it }, label = { Text(it.title) }) } }
                OutlinedTextField(deadlineDays, { deadlineDays = it.filter { ch -> ch == '-' || ch.isDigit() } }, label = { Text("Deadline days from today, empty = none") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(estimate, { estimate = it.filter(Char::isDigit) }, label = { Text("Estimated minutes") }, modifier = Modifier.fillMaxWidth())
                if (initial != null) Text("For existing tasks an empty deadline field removes the deadline.", modifier = Modifier.padding(top = 4.dp))
            }
        },
        confirmButton = { Button(onClick = { onSave(selectedSubjectId, title, description, status, priority, deadlineDays.toIntOrNull(), estimate.toIntOrNull()); onDismiss() }) { Text("Save") } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun NoteDialog(subjects: List<Subject>, initial: Note?, onDismiss: () -> Unit, onSave: (Long?, String, String, String) -> Unit) {
    var selectedSubjectId by remember { mutableStateOf(initial?.subjectId) }
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var content by remember { mutableStateOf(initial?.content ?: "") }
    var tags by remember { mutableStateOf(initial?.tags?.joinToString(", ") ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New note" else "Edit note") },
        text = {
            Column(Modifier.heightIn(max = 560.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Subject")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(selected = selectedSubjectId == null, onClick = { selectedSubjectId = null }, label = { Text("None") })
                    subjects.take(4).forEach { s -> FilterChip(selected = selectedSubjectId == s.id, onClick = { selectedSubjectId = s.id }, label = { Text(s.name.take(10)) }) }
                }
                OutlinedTextField(title, { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(content, { content = it }, label = { Text("Content") }, modifier = Modifier.fillMaxWidth(), minLines = 7)
                OutlinedTextField(tags, { tags = it }, label = { Text("Tags separated by comma") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { Button(onClick = { onSave(selectedSubjectId, title, content, tags); onDismiss() }) { Text("Save") } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
