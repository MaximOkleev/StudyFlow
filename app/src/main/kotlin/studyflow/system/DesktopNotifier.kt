package studyflow.system

import java.awt.Color
import java.awt.Graphics2D
import java.awt.Image
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.awt.image.BufferedImage

object DesktopNotifier {
    fun notify(title: String, message: String) {
        val delivered = runCatching {
            if (!SystemTray.isSupported()) return@runCatching false
            val tray = SystemTray.getSystemTray()
            val icon = TrayIcon(createIcon(), "StudyFlow")
            icon.isImageAutoSize = true
            tray.add(icon)
            icon.displayMessage(title, message, TrayIcon.MessageType.INFO)
            Thread {
                Thread.sleep(5000)
                runCatching { tray.remove(icon) }
            }.start()
            true
        }.getOrDefault(false)
        if (!delivered) runCatching { Toolkit.getDefaultToolkit().beep() }
    }

    private fun createIcon(): Image {
        val image = BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics() as Graphics2D
        g.color = Color(0x0F172A)
        g.fillRoundRect(0, 0, 32, 32, 8, 8)
        g.color = Color(0x7C3AED)
        g.fillOval(7, 7, 18, 18)
        g.color = Color.WHITE
        g.drawString("S", 12, 21)
        g.dispose()
        return image
    }
}
