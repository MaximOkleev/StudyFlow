package studyflow

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import studyflow.data.StudyRepository
import studyflow.presentation.components.Sidebar
import studyflow.presentation.screens.CalendarScreen
import studyflow.presentation.screens.DashboardScreen
import studyflow.presentation.screens.NotesScreen
import studyflow.presentation.screens.SettingsScreen
import studyflow.presentation.screens.StatisticsScreen
import studyflow.presentation.screens.SubjectsScreen
import studyflow.presentation.screens.TasksScreen
import studyflow.presentation.screens.TimerScreen
import studyflow.presentation.theme.StudyFlowTheme

enum class AppScreen(val title: String, val shortTitle: String) {
    Dashboard("Dashboard", "Home"),
    Subjects("Subjects", "Subjects"),
    Tasks("Tasks", "Tasks"),
    Calendar("Calendar", "Calendar"),
    Notes("Notes", "Notes"),
    Timer("Focus Timer", "Timer"),
    Statistics("Statistics", "Stats"),
    Settings("Settings", "Settings")
}

@Composable
fun StudyFlowApp(compact: Boolean = false) {
    val repository = remember { StudyRepository() }
    var screen by remember { mutableStateOf(AppScreen.Dashboard) }

    StudyFlowTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Sidebar(current = screen, compact = compact, onSelect = { screen = it })
                when (screen) {
                    AppScreen.Dashboard -> DashboardScreen(repository, onOpenTasks = { screen = AppScreen.Tasks }, onOpenTimer = { screen = AppScreen.Timer })
                    AppScreen.Subjects -> SubjectsScreen(repository)
                    AppScreen.Tasks -> TasksScreen(repository)
                    AppScreen.Calendar -> CalendarScreen(repository)
                    AppScreen.Notes -> NotesScreen(repository)
                    AppScreen.Timer -> TimerScreen(repository)
                    AppScreen.Statistics -> StatisticsScreen(repository)
                    AppScreen.Settings -> SettingsScreen(repository)
                }
            }
        }
    }
}
