package studyflow

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.Dimension

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "StudyFlow"
    ) {
        window.minimumSize = Dimension(1160, 760)
        StudyFlowApp()
    }
}
