package studyflow.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import studyflow.data.StudyRepository
import studyflow.domain.model.Subject
import studyflow.presentation.components.ScreenScaffold
import studyflow.presentation.components.SubjectCard
import studyflow.presentation.dialogs.SubjectDialog

@Composable
fun SubjectsScreen(repository: StudyRepository) {
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Subject?>(null) }
    ScreenScaffold(
        title = "Subjects",
        subtitle = "Separate progress by learning area.",
        action = { Button(onClick = { showAdd = true }) { Text("New subject") } }
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 290.dp),
            contentPadding = PaddingValues(bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(repository.subjects) { subject ->
                SubjectCard(
                    subject = subject,
                    progress = repository.subjectProgress(subject.id),
                    taskCount = repository.tasksForSubject(subject.id).size,
                    onEdit = { editing = subject },
                    onDelete = { repository.deleteSubject(subject.id) }
                )
            }
        }
    }
    if (showAdd) SubjectDialog(null, onDismiss = { showAdd = false }, onSave = repository::addSubject)
    editing?.let { subject -> SubjectDialog(subject, onDismiss = { editing = null }, onSave = { n, d, c, i -> repository.updateSubject(subject, n, d, c, i) }) }
}
