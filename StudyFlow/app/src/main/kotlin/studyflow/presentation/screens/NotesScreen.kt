package studyflow.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import studyflow.domain.model.Note
import studyflow.presentation.components.EmptyState
import studyflow.presentation.components.NoteCard
import studyflow.presentation.components.ScreenScaffold
import studyflow.presentation.dialogs.NoteDialog

@Composable
fun NotesScreen(repository: StudyRepository) {
    var search by remember { mutableStateOf("") }
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Note?>(null) }
    val notes = repository.notes.filter { note ->
        search.isBlank() || note.title.contains(search, true) || note.content.contains(search, true) || note.tags.any { it.contains(search, true) }
    }.sortedByDescending { it.updatedAt }

    ScreenScaffold(
        title = "Notes",
        subtitle = "Keep formulas, explanations and project ideas near tasks.",
        action = { Button(onClick = { showAdd = true }) { Text("New note") } }
    ) {
        OutlinedTextField(search, { search = it }, label = { Text("Search notes or tags") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        if (notes.isEmpty()) EmptyState("No notes found.", "Create note") { showAdd = true } else LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            items(notes) { note -> NoteCard(note, repository.subjectName(note.subjectId), onEdit = { editing = note }, onDelete = { repository.deleteNote(note.id) }) }
        }
    }
    if (showAdd) NoteDialog(repository.subjects, null, onDismiss = { showAdd = false }, onSave = repository::addNote)
    editing?.let { note -> NoteDialog(repository.subjects, note, onDismiss = { editing = null }, onSave = { sid, title, content, tags -> repository.updateNote(note, sid, title, content, tags) }) }
}
