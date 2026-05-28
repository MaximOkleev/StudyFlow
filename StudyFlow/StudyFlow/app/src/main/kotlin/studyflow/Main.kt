package studyflow

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension
import studyflow.data.WindowPreferences

fun main() = application {
    val windowPreferences = remember { WindowPreferences() }
    val savedPrefs = remember { windowPreferences.load() }
    val windowState = rememberWindowState(width = savedPrefs.width.dp, height = savedPrefs.height.dp)
    Window(
        onCloseRequest = {
            windowPreferences.save(windowState.size.width.value, windowState.size.height.value)
            exitApplication()
        },
        title = "StudyFlow",
        state = windowState
    ) {
        window.minimumSize = Dimension(980, 680)
        StudyFlowApp(compact = windowState.size.width < 1280.dp)
    }
}
