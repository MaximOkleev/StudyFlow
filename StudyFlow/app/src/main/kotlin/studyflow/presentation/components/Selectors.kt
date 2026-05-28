package studyflow.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import studyflow.domain.model.StudyTask
import studyflow.domain.model.Subject

@Composable
fun SubjectDropdownField(
    subjects: List<Subject>,
    selectedSubjectId: Long?,
    onSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Subject",
    allowNone: Boolean = false,
    noneLabel: String = "Any subject",
    placeholder: String = "Choose subject"
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = selectedSubjectId?.let { id -> subjects.firstOrNull { it.id == id } }
    val buttonText = selected?.name ?: if (allowNone) noneLabel else placeholder

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (label.isNotBlank()) Text(label, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = buttonText,
                    maxLines = 4,
                    softWrap = true,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, modifier = Modifier.padding(start = 8.dp))
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.widthIn(min = 520.dp).heightIn(max = 460.dp)
            ) {
                if (allowNone) {
                    DropdownMenuItem(
                        text = { Text(noneLabel, fontWeight = if (selectedSubjectId == null) FontWeight.Bold else FontWeight.Normal) },
                        onClick = {
                            onSelected(null)
                            expanded = false
                        }
                    )
                }
                subjects.forEach { subject ->
                    DropdownMenuItem(
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(subject.icon, modifier = Modifier.padding(end = 8.dp))
                                    Text(
                                        subject.name,
                                        maxLines = 3,
                                        softWrap = true,
                                        overflow = TextOverflow.Clip,
                                        fontWeight = if (selectedSubjectId == subject.id) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (subject.description.isNotBlank()) {
                                    Text(
                                        subject.description,
                                        maxLines = 2,
                                        softWrap = true,
                                        overflow = TextOverflow.Clip,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        onClick = {
                            onSelected(subject.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskDropdownField(
    tasks: List<StudyTask>,
    selectedTaskId: Long?,
    onSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Task",
    allowNone: Boolean = true,
    noneLabel: String = "No task",
    placeholder: String = "Choose task"
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = selectedTaskId?.let { id -> tasks.firstOrNull { it.id == id } }
    val buttonText = selected?.title ?: if (allowNone) noneLabel else placeholder

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (label.isNotBlank()) Text(label, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(buttonText, maxLines = 3, softWrap = true, overflow = TextOverflow.Clip, modifier = Modifier.weight(1f))
                Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, modifier = Modifier.padding(start = 8.dp))
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.widthIn(min = 520.dp).heightIn(max = 420.dp)
            ) {
                if (allowNone) {
                    DropdownMenuItem(
                        text = { Text(noneLabel, fontWeight = if (selectedTaskId == null) FontWeight.Bold else FontWeight.Normal) },
                        onClick = {
                            onSelected(null)
                            expanded = false
                        }
                    )
                }
                tasks.forEach { task ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                task.title,
                                maxLines = 3,
                                softWrap = true,
                                overflow = TextOverflow.Clip,
                                fontWeight = if (selectedTaskId == task.id) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onSelected(task.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
