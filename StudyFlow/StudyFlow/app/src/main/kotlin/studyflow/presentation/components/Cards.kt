package studyflow.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studyflow.domain.model.Note
import studyflow.domain.model.Subject
import studyflow.domain.model.StudyTask
import studyflow.domain.model.TaskPriority
import studyflow.domain.model.TaskStatus
import studyflow.util.DateUtils
import studyflow.util.colorFromHex

@Composable
fun ScreenScaffold(title: String, subtitle: String? = null, action: (@Composable () -> Unit)? = null, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { SectionTitle(title, subtitle) }
            if (action != null) action()
        }
        content()
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String? = null) {
    Column {
        Text(title, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
        if (subtitle != null) Text(subtitle, fontSize = 14.sp, color = Color(0xFF9CA3AF), modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun StatCard(title: String, value: String, hint: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color(0xFF101827)), shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(18.dp)) {
            Text(title, color = Color(0xFF9CA3AF), fontSize = 13.sp)
            Text(value, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text(hint, color = Color(0xFF6B7280), fontSize = 12.sp)
        }
    }
}

@Composable
fun SubjectCard(subject: Subject, progress: Float, taskCount: Int, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827)), shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(colorFromHex(subject.colorHex)),
                    contentAlignment = Alignment.Center
                ) { Text(subject.icon, color = Color.White, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(subject.name, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text("$taskCount tasks", color = Color(0xFF9CA3AF), fontSize = 13.sp)
                }
            }
            Text(subject.description, color = Color(0xFFB9C0D4), maxLines = 2, overflow = TextOverflow.Ellipsis)
            LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(99.dp)))
            Text("Progress ${(progress * 100).toInt()}%", color = Color(0xFF9CA3AF), fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit) { Text("Edit") }
                OutlinedButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

@Composable
fun TaskCard(task: StudyTask, subjectName: String, onCycleStatus: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val priorityColor = when (task.priority) {
        TaskPriority.High -> Color(0xFFF97316)
        TaskPriority.Medium -> Color(0xFF22D3EE)
        TaskPriority.Low -> Color(0xFF10B981)
    }
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827)), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(task.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(subjectName, color = Color(0xFF9CA3AF), fontSize = 12.sp)
                }
                Badge(task.status.title, statusColor(task.status))
                Spacer(Modifier.width(8.dp))
                Badge(task.priority.title, priorityColor)
            }
            if (task.description.isNotBlank()) Text(task.description, color = Color(0xFFB9C0D4), maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Deadline: ${DateUtils.formatShort(task.deadlineAt)}", color = if (DateUtils.isOverdue(task.deadlineAt) && task.status != TaskStatus.Done) Color(0xFFF87171) else Color(0xFF9CA3AF), modifier = Modifier.weight(1f))
                Text("${task.spentMinutes}/${task.estimatedMinutes ?: "?"} min", color = Color(0xFF9CA3AF))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onCycleStatus) { Text("Move status") }
                OutlinedButton(onClick = onEdit) { Text("Edit") }
                OutlinedButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

@Composable
fun NoteCard(note: Note, subjectName: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827)), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(note.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(subjectName, color = Color(0xFF9CA3AF), fontSize = 12.sp)
                }
                Text(DateUtils.formatShort(note.updatedAt), color = Color(0xFF6B7280), fontSize = 12.sp)
            }
            Text(note.content, color = Color(0xFFB9C0D4), maxLines = 4, overflow = TextOverflow.Ellipsis)
            if (note.tags.isNotEmpty()) Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { note.tags.take(4).forEach { Badge("#$it", Color(0xFF8B5CF6)) } }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit) { Text("Edit") }
                OutlinedButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

@Composable
fun Badge(text: String, color: Color) {
    Box(Modifier.clip(RoundedCornerShape(99.dp)).background(color.copy(alpha = 0.18f)).padding(horizontal = 10.dp, vertical = 5.dp)) {
        Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EmptyState(text: String, actionText: String? = null, onAction: (() -> Unit)? = null) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827)), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text, color = Color(0xFF9CA3AF))
            if (actionText != null && onAction != null) Button(onClick = onAction) { Text(actionText) }
        }
    }
}

@Composable
private fun statusColor(status: TaskStatus): Color = when (status) {
    TaskStatus.Todo -> Color(0xFF9CA3AF)
    TaskStatus.InProgress -> MaterialTheme.colorScheme.primary
    TaskStatus.Done -> Color(0xFF10B981)
}
