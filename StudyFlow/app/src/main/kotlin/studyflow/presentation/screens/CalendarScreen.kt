package studyflow.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import studyflow.data.StudyRepository
import studyflow.domain.model.Exam
import studyflow.domain.model.StudyTask
import studyflow.domain.model.TaskStatus
import studyflow.presentation.components.EmptyState
import studyflow.presentation.components.ExamCard
import studyflow.presentation.components.ScreenScaffold
import studyflow.presentation.components.TaskCard
import studyflow.presentation.dialogs.ConfirmDialog
import studyflow.presentation.dialogs.TaskDialog
import studyflow.util.DateUtils
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarScreen(repository: StudyRepository) {
    var month by remember { mutableStateOf(YearMonth.now()) }
    var selected by remember { mutableStateOf(DateUtils.today()) }
    var editing by remember { mutableStateOf<StudyTask?>(null) }
    var pendingDelete by remember { mutableStateOf<StudyTask?>(null) }
    val selectedTasks = repository.tasks.filter { it.deadlineAt != null && DateUtils.millisToDate(it.deadlineAt) == selected }
    val selectedExams = repository.examsOnDate(DateUtils.dateToMillis(selected))
    ScreenScaffold(
        title = "Calendar",
        subtitle = "Календарь задач и событий сессии. Наведи курсор на событие, чтобы увидеть детали и аудиторию.",
        action = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { month = month.minusMonths(1) }) { Text("<") }
                Button(onClick = { month = YearMonth.now(); selected = DateUtils.today() }) { Text("Today") }
                Button(onClick = { month = month.plusMonths(1) }) { Text(">") }
            }
        }
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxSize()) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827)), shape = RoundedCornerShape(24.dp), modifier = Modifier.weight(1.25f)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(month.toString(), color = Color.White, fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
                            Text(it, color = Color(0xFF9CA3AF), modifier = Modifier.weight(1f))
                        }
                    }
                    DateUtils.monthGrid(month).chunked(7).forEach { week ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            week.forEach { date -> DayCell(date, selected, repository, onSelect = { selected = it }, Modifier.weight(1f)) }
                        }
                    }
                }
            }
            Column(Modifier.weight(0.75f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${selected.dayOfMonth} ${selected.month}", color = Color.White, fontWeight = FontWeight.Bold)
                if (selectedTasks.isEmpty() && selectedExams.isEmpty()) {
                    EmptyState("No deadlines or session events on selected date.")
                } else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(selectedExams) { exam -> ExamCard(exam) }
                    items(selectedTasks) { task -> TaskCard(task, repository.subjectName(task.subjectId), onCycleStatus = { repository.cycleTaskStatus(task.id) }, onEdit = { editing = task }, onDelete = { pendingDelete = task }) }
                }
            }
        }
    }
    editing?.let { task -> TaskDialog(repository.subjects, task, onDismiss = { editing = null }, onSave = { sid, title, desc, status, priority, deadlineAt, est, recurrence -> repository.updateTaskWithDeadline(task, sid, title, desc, status, priority, deadlineAt, est, recurrence) }) }
    pendingDelete?.let { task -> ConfirmDialog(title = "Delete task?", text = "This will delete '${task.title}'.", onConfirm = { repository.deleteTask(task.id) }, onDismiss = { pendingDelete = null }) }
}

@Composable
private fun DayCell(date: LocalDate?, selected: LocalDate, repository: StudyRepository, onSelect: (LocalDate) -> Unit, modifier: Modifier) {
    if (date == null) {
        Box(modifier.height(106.dp))
        return
    }
    val tasks = repository.tasks.filter { it.deadlineAt != null && DateUtils.millisToDate(it.deadlineAt) == date }
    val exams = repository.exams.filter { DateUtils.millisToDate(it.startAt) == date }.sortedBy { it.startAt }
    val overdueCount = tasks.count { DateUtils.isOverdue(it.deadlineAt) && it.status != TaskStatus.Done }
    val isSelected = date == selected
    val isToday = date == DateUtils.today()
    Box(
        modifier = modifier
            .height(106.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFF7C3AED).copy(alpha = 0.35f) else Color(0xFF171E2E))
            .clickable { onSelect(date) }
            .padding(8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(date.dayOfMonth.toString(), color = if (isToday) Color(0xFF22D3EE) else Color.White, fontWeight = FontWeight.Bold)
            exams.take(2).forEach { exam ->
                CalendarExamChip(
                    exam = exam,
                    subjectDescription = exam.subjectId?.let { repository.subjectById(it)?.description }
                )
            }
            if (exams.size > 2) Text("+${exams.size - 2} ещё", color = Color(0xFFFBBF24), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            if (tasks.isNotEmpty()) Text("${tasks.size} tasks", color = if (overdueCount > 0) Color(0xFFF87171) else Color(0xFFB9C0D4), fontSize = 11.sp)
        }
    }
}

@Composable
private fun CalendarExamChip(exam: Exam, subjectDescription: String?) {
    var hovered by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2563EB).copy(alpha = 0.25f))
            .onPointerEvent(PointerEventType.Enter) { hovered = true }
            .onPointerEvent(PointerEventType.Exit) { hovered = false }
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(
            "${DateUtils.formatClock(exam.startAt)} ${exam.subjectName}",
            color = Color(0xFFDCEBFF),
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (hovered) {
            Popup(offset = IntOffset(18, 26)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF020617)),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.widthIn(min = 280.dp, max = 420.dp)
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(exam.subjectName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Дата: ${DateUtils.formatShort(exam.startAt)}", color = Color(0xFFCBD5E1), fontSize = 13.sp)
                        Text("Время: ${DateUtils.formatTimeRange(exam.startAt, exam.endAt)}", color = Color(0xFF22D3EE), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Аудитория: ${exam.location.ifBlank { "не указана" }}", color = Color(0xFFFBBF24), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        if (exam.teachers.isNotBlank()) Text("Преподаватель: ${exam.teachers}", color = Color(0xFFCBD5E1), fontSize = 13.sp)
                        if (!subjectDescription.isNullOrBlank()) Text(subjectDescription, color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
