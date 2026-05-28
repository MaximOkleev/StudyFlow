@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package studyflow.presentation.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import studyflow.data.StudyRepository
import studyflow.domain.model.StudyTask
import studyflow.domain.model.TaskStatus
import studyflow.presentation.components.EmptyState
import studyflow.presentation.components.ScreenScaffold
import studyflow.presentation.components.TaskCard
import studyflow.presentation.dialogs.TaskDialog

@Composable
fun TasksScreen(repository: StudyRepository) {
    var filter by remember { mutableStateOf<TaskStatus?>(null) }
    var search by remember { mutableStateOf("") }
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<StudyTask?>(null) }
    val tasks = repository.tasks
        .filter { filter == null || it.status == filter }
        .filter { search.isBlank() || it.title.contains(search, ignoreCase = true) || it.description.contains(search, ignoreCase = true) }
        .sortedWith(compareBy<StudyTask> { it.deadlineAt ?: Long.MAX_VALUE }.thenByDescending { it.priority.ordinal })

    ScreenScaffold(
        title = "Tasks",
        subtitle = "Plan work, move statuses and track time.",
        action = { Button(enabled = repository.subjects.isNotEmpty(), onClick = { showAdd = true }) { Text("New task") } }
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text("All") })
            TaskStatus.entries.forEach { status -> FilterChip(selected = filter == status, onClick = { filter = status }, label = { Text(status.title) }) }
            OutlinedTextField(search, { search = it }, label = { Text("Search") }, singleLine = true, modifier = Modifier.weight(1f).padding(start = 12.dp))
        }
        if (tasks.isEmpty()) EmptyState("No tasks match the current filter.", "Create task") { showAdd = true } else LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            items(tasks) { task ->
                TaskCard(task, repository.subjectName(task.subjectId), onCycleStatus = { repository.cycleTaskStatus(task.id) }, onEdit = { editing = task }, onDelete = { repository.deleteTask(task.id) })
            }
        }
    }
    if (showAdd) TaskDialog(repository.subjects, null, onDismiss = { showAdd = false }, onSave = { sid, title, desc, _, priority, days, est -> repository.addTask(sid, title, desc, priority, days, est) })
    editing?.let { task -> TaskDialog(repository.subjects, task, onDismiss = { editing = null }, onSave = { sid, title, desc, status, priority, days, est -> repository.updateTask(task, sid, title, desc, status, priority, days, est) }) }
}
