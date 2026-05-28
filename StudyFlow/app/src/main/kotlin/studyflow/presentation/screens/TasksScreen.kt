@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

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
import androidx.compose.material3.FilterChip
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
import studyflow.data.StudyRepository
import studyflow.domain.model.StudyTask
import studyflow.domain.model.TaskPriority
import studyflow.domain.model.TaskStatus
import studyflow.presentation.components.EmptyState
import studyflow.presentation.components.ScreenScaffold
import studyflow.presentation.components.TaskCard
import studyflow.presentation.dialogs.ConfirmDialog
import studyflow.presentation.dialogs.TaskDialog

@Composable
fun TasksScreen(repository: StudyRepository) {
    var statusFilter by remember { mutableStateOf<TaskStatus?>(null) }
    var priorityFilter by remember { mutableStateOf<TaskPriority?>(null) }
    var subjectFilter by remember { mutableStateOf<Long?>(null) }
    var search by remember { mutableStateOf("") }
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<StudyTask?>(null) }
    var pendingDelete by remember { mutableStateOf<StudyTask?>(null) }

    val tasks = repository.tasks
        .filter { statusFilter == null || it.status == statusFilter }
        .filter { priorityFilter == null || it.priority == priorityFilter }
        .filter { subjectFilter == null || it.subjectId == subjectFilter }
        .filter { search.isBlank() || it.title.contains(search, ignoreCase = true) || it.description.contains(search, ignoreCase = true) }
        .sortedWith(compareBy<StudyTask> { it.deadlineAt ?: Long.MAX_VALUE }.thenByDescending { it.priority.ordinal })

    ScreenScaffold(
        title = "Tasks",
        subtitle = "Plan work, move statuses and track time.",
        action = { Button(enabled = repository.subjects.isNotEmpty(), onClick = { showAdd = true }, modifier = Modifier.testTag("tasks.new")) { Text("New task") } }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(selected = statusFilter == null, onClick = { statusFilter = null }, label = { Text("All") })
                TaskStatus.entries.forEach { status -> FilterChip(selected = statusFilter == status, onClick = { statusFilter = status }, label = { Text(status.title) }) }
                OutlinedTextField(search, { search = it }, label = { Text("Search") }, singleLine = true, modifier = Modifier.weight(1f).padding(start = 12.dp).testTag("tasks.search"))
            }
            (listOf<Long?>(null) + repository.subjects.map { it.id }).chunked(5).forEach { rowIds ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    rowIds.forEach { id ->
                        val subject = id?.let { sid -> repository.subjectById(sid) }
                        FilterChip(
                            selected = subjectFilter == id,
                            onClick = { subjectFilter = id },
                            label = { Text(subject?.name?.take(12) ?: "Any subject") }
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(selected = priorityFilter == null, onClick = { priorityFilter = null }, label = { Text("Any priority") })
                TaskPriority.entries.forEach { priority -> FilterChip(selected = priorityFilter == priority, onClick = { priorityFilter = priority }, label = { Text(priority.title) }) }
            }
        }
        if (tasks.isEmpty()) EmptyState("No tasks match the current filter.", "Create task") { showAdd = true } else LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            items(tasks) { task ->
                TaskCard(task, repository.subjectName(task.subjectId), onCycleStatus = { repository.cycleTaskStatus(task.id) }, onEdit = { editing = task }, onDelete = { pendingDelete = task })
            }
        }
    }
    if (showAdd) TaskDialog(repository.subjects, null, onDismiss = { showAdd = false }, onSave = { sid, title, desc, _, priority, deadlineAt, est, recurrence -> repository.addTaskWithDeadline(sid, title, desc, priority, deadlineAt, est, recurrence) })
    editing?.let { task -> TaskDialog(repository.subjects, task, onDismiss = { editing = null }, onSave = { sid, title, desc, status, priority, deadlineAt, est, recurrence -> repository.updateTaskWithDeadline(task, sid, title, desc, status, priority, deadlineAt, est, recurrence) }) }
    pendingDelete?.let { task ->
        ConfirmDialog(
            title = "Delete task?",
            text = "This will delete '${task.title}' and detach related focus sessions.",
            onConfirm = { repository.deleteTask(task.id) },
            onDismiss = { pendingDelete = null }
        )
    }
}
