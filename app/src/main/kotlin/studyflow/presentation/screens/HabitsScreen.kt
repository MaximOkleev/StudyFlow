package studyflow.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studyflow.data.StudyRepository
import studyflow.domain.model.Habit
import studyflow.presentation.components.EmptyState
import studyflow.presentation.components.ScreenScaffold
import studyflow.presentation.dialogs.ConfirmDialog
import studyflow.presentation.dialogs.HabitDialog
import studyflow.util.colorFromHex

@Composable
fun HabitsScreen(repository: StudyRepository) {
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Habit?>(null) }
    var deleting by remember { mutableStateOf<Habit?>(null) }

    ScreenScaffold(
        title = "Habits",
        subtitle = "Daily habit tracking with streaks.",
        action = { Button(onClick = { showAdd = true }) { Text("New habit") } }
    ) {
        if (repository.habits.isEmpty()) {
            EmptyState("No habits yet.", "Create habit") { showAdd = true }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                items(repository.habits, key = { it.id }) { habit ->
                    HabitCard(
                        habit = habit,
                        completedToday = repository.habitCompletedToday(habit),
                        streak = repository.habitStreak(habit),
                        onToggle = { repository.toggleHabitToday(habit.id) },
                        onEdit = { editing = habit },
                        onDelete = { deleting = habit }
                    )
                }
            }
        }
    }

    if (showAdd) HabitDialog(null, onDismiss = { showAdd = false }, onSave = { name, desc, color -> repository.addHabit(name, desc, color) })
    editing?.let { habit -> HabitDialog(habit, onDismiss = { editing = null }, onSave = { name, desc, color -> repository.updateHabit(habit, name, desc, color) }) }
    deleting?.let { habit ->
        ConfirmDialog(
            title = "Delete habit?",
            text = "This will delete '${habit.name}' and all daily marks.",
            onConfirm = { repository.deleteHabit(habit.id) },
            onDismiss = { deleting = null }
        )
    }
}

@Composable
private fun HabitCard(habit: Habit, completedToday: Boolean, streak: Int, onToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827)), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(colorFromHex(habit.colorHex)), contentAlignment = Alignment.Center) {
                Text(if (completedToday) "✓" else "○", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(habit.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                if (habit.description.isNotBlank()) Text(habit.description, color = Color(0xFFCBD5E1))
                Text("Streak: $streak days · Logged days: ${habit.completedDates.size}", color = Color(0xFF9CA3AF), fontSize = 12.sp)
            }
            Button(onClick = onToggle) { Text(if (completedToday) "Unmark" else "Done today") }
            OutlinedButton(onClick = onEdit) { Text("Edit") }
            OutlinedButton(onClick = onDelete) { Text("Delete") }
            Spacer(Modifier.size(2.dp))
        }
    }
}
