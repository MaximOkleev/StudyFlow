@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package studyflow.presentation.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import studyflow.domain.model.Note
import studyflow.domain.model.Subject
import studyflow.domain.model.StudyTask
import studyflow.domain.model.Habit
import studyflow.domain.model.Recurrence
import studyflow.domain.model.TaskPriority
import studyflow.domain.model.TaskStatus
import studyflow.presentation.components.SubjectDropdownField
import studyflow.util.DateUtils

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
fun TaskDialog(subjects: List<Subject>, initial: StudyTask?, onDismiss: () -> Unit, onSave: (Long, String, String, TaskStatus, TaskPriority, Long?, Int?, Recurrence) -> Unit) {
    var selectedSubjectId by remember { mutableStateOf(initial?.subjectId ?: subjects.firstOrNull()?.id ?: 0L) }
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var status by remember { mutableStateOf(initial?.status ?: TaskStatus.Todo) }
    var priority by remember { mutableStateOf(initial?.priority ?: TaskPriority.Medium) }
    var recurrence by remember { mutableStateOf(initial?.recurrence ?: Recurrence.None) }
    var deadlineText by remember { mutableStateOf(initial?.deadlineAt?.let { DateUtils.formatIso(it) } ?: DateUtils.today().plusDays(3).toString()) }
    var estimate by remember { mutableStateOf(initial?.estimatedMinutes?.toString() ?: "60") }
    val deadlineAt = DateUtils.parseIsoDateToMillis(deadlineText)
    val deadlineInvalid = deadlineText.isNotBlank() && deadlineAt == null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New task" else "Edit task") },
        text = {
            Column(Modifier.heightIn(max = 590.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SubjectDropdownField(
                    subjects = subjects,
                    selectedSubjectId = selectedSubjectId,
                    onSelected = { id -> if (id != null) selectedSubjectId = id },
                    allowNone = false,
                    label = "Subject",
                    modifier = Modifier.fillMaxWidth().testTag("task.subject.selector")
                )
                OutlinedTextField(title, { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(description, { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                Text("Status")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { TaskStatus.entries.forEach { FilterChip(selected = status == it, onClick = { status = it }, label = { Text(it.title) }) } }
                Text("Priority")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { TaskPriority.entries.forEach { FilterChip(selected = priority == it, onClick = { priority = it }, label = { Text(it.title) }) } }
                Text("Repeat")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Recurrence.entries.forEach { FilterChip(selected = recurrence == it, onClick = { recurrence = it }, label = { Text(it.title) }) } }
                OutlinedTextField(
                    deadlineText,
                    { deadlineText = it },
                    label = { Text("Deadline date: YYYY-MM-DD, empty = none") },
                    isError = deadlineInvalid,
                    modifier = Modifier.fillMaxWidth()
                )
                if (deadlineInvalid) Text("Date format must be like ${DateUtils.today()}")
                OutlinedTextField(estimate, { estimate = it.filter(Char::isDigit) }, label = { Text("Estimated minutes") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { Button(enabled = !deadlineInvalid && selectedSubjectId != 0L, onClick = { onSave(selectedSubjectId, title, description, status, priority, deadlineAt, estimate.toIntOrNull(), recurrence); onDismiss() }) { Text("Save") } },
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
                SubjectDropdownField(
                    subjects = subjects,
                    selectedSubjectId = selectedSubjectId,
                    onSelected = { selectedSubjectId = it },
                    allowNone = true,
                    noneLabel = "No subject",
                    label = "Subject",
                    modifier = Modifier.fillMaxWidth().testTag("note.subject.selector")
                )
                OutlinedTextField(title, { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(content, { content = it }, label = { Text("Content") }, modifier = Modifier.fillMaxWidth(), minLines = 7)
                OutlinedTextField(tags, { tags = it }, label = { Text("Tags separated by comma") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { Button(onClick = { onSave(selectedSubjectId, title, content, tags); onDismiss() }) { Text("Save") } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}


@Composable
fun HabitDialog(initial: Habit?, onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var color by remember { mutableStateOf(initial?.colorHex ?: "#10B981") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New habit" else "Edit habit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(description, { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(color, { color = it }, label = { Text("Color #RRGGBB") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { Button(onClick = { onSave(name, description, color); onDismiss() }) { Text("Save") } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ConfirmDialog(title: String, text: String, confirmText: String = "Delete", onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { Button(onClick = { onConfirm(); onDismiss() }, modifier = Modifier.testTag("dialog.confirm")) { Text(confirmText) } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
