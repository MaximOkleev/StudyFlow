package studyflow.data

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Properties
import kotlin.io.path.exists

class WindowPreferences(rootDir: Path = Paths.get(System.getProperty("user.home"), ".studyflow")) {
    private val file = rootDir.resolve("window.properties")

    init {
        Files.createDirectories(rootDir)
    }

    fun load(): WindowPrefs {
        if (!file.exists()) return WindowPrefs()
        val p = Properties()
        return runCatching {
            Files.newInputStream(file).use { p.load(it) }
            WindowPrefs(
                width = p.getProperty("width", "1360").toFloatOrNull()?.coerceAtLeast(980f) ?: 1360f,
                height = p.getProperty("height", "900").toFloatOrNull()?.coerceAtLeast(680f) ?: 900f
            )
        }.getOrDefault(WindowPrefs())
    }

    fun save(width: Float, height: Float) {
        val p = Properties()
        p["width"] = width.coerceAtLeast(980f).toString()
        p["height"] = height.coerceAtLeast(680f).toString()
        Files.newOutputStream(file).use { p.store(it, "StudyFlow window preferences") }
    }
}

data class WindowPrefs(
    val width: Float = 1360f,
    val height: Float = 900f
)
