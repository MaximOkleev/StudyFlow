@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package studyflow.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studyflow.data.StudyRepository
import studyflow.domain.model.StudyTask
import studyflow.domain.model.TaskStatus
import studyflow.presentation.components.Badge
import studyflow.presentation.components.EmptyState
import studyflow.presentation.components.ScreenScaffold
import studyflow.util.DateUtils
import kotlin.math.roundToInt

@Composable
fun BoardScreen(repository: StudyRepository) {
    ScreenScaffold(title = "Kanban Board", subtitle = "Drag cards horizontally between To do, In progress and Done.") {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxSize()) {
            TaskStatus.entries.forEach { status ->
                BoardColumn(
                    status = status,
                    tasks = repository.tasks.filter { it.status == status }.sortedBy { it.deadlineAt ?: Long.MAX_VALUE },
                    repository = repository,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun BoardColumn(status: TaskStatus, tasks: List<StudyTask>, repository: StudyRepository, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0B1220))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Text(status.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Badge(tasks.size.toString(), Color(0xFF334155))
        }
        if (tasks.isEmpty()) {
            EmptyState("No cards here.", "Add in Tasks") {}
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(tasks, key = { it.id }) { task ->
                    DraggableTaskCard(task, repository)
                }
            }
        }
    }
}

@Composable
private fun DraggableTaskCard(task: StudyTask, repository: StudyRepository) {
    var dragX by remember(task.id) { mutableFloatStateOf(0f) }
    var dragging by remember(task.id) { mutableStateOf(false) }
    Card(
        colors = CardDefaults.cardColors(containerColor = if (dragging) Color(0xFF1E293B) else Color(0xFF101827)),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(dragX.roundToInt(), 0) }
            .pointerInput(task.id) {
                detectDragGestures(
                    onDragStart = { dragging = true },
                    onDragCancel = { dragging = false; dragX = 0f },
                    onDragEnd = {
                        val target = when {
                            dragX > 90f -> nextStatus(task.status)
                            dragX < -90f -> previousStatus(task.status)
                            else -> task.status
                        }
                        if (target != task.status) repository.updateTaskStatus(task.id, target)
                        dragging = false
                        dragX = 0f
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        dragX += amount.x
                    }
                )
            }
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(task.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(repository.subjectName(task.subjectId), color = Color(0xFF9CA3AF), fontSize = 12.sp)
            if (task.description.isNotBlank()) Text(task.description, color = Color(0xFFCBD5E1), maxLines = 2, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Badge(task.priority.title, Color(0xFF334155))
                Badge(DateUtils.formatShort(task.deadlineAt), Color(0xFF1F2937))
                if (task.recurrence.name != "None") Badge(task.recurrence.title, Color(0xFF4C1D95))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(enabled = task.status != TaskStatus.Todo, onClick = { repository.updateTaskStatus(task.id, previousStatus(task.status)) }) { Text("←") }
                Button(enabled = task.status != TaskStatus.Done, onClick = { repository.updateTaskStatus(task.id, nextStatus(task.status)) }) { Text("→") }
            }
        }
    }
}

private fun nextStatus(status: TaskStatus): TaskStatus = when (status) {
    TaskStatus.Todo -> TaskStatus.InProgress
    TaskStatus.InProgress -> TaskStatus.Done
    TaskStatus.Done -> TaskStatus.Done
}

private fun previousStatus(status: TaskStatus): TaskStatus = when (status) {
    TaskStatus.Todo -> TaskStatus.Todo
    TaskStatus.InProgress -> TaskStatus.Todo
    TaskStatus.Done -> TaskStatus.InProgress
}
