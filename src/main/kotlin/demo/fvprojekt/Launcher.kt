@file:Suppress("unused")
package demo.fvprojekt.demo.fvprojekt

import com.formdev.flatlaf.FlatDarkLaf
import javax.swing.*
import java.awt.*
import java.awt.geom.RoundRectangle2D

fun main() {
    try {
        UIManager.setLookAndFeel(FlatDarkLaf())
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
    SwingUtilities.invokeLater {
        val launcherFrame = LauncherFrame()
        launcherFrame.isVisible = true
    }
}

class LauncherFrame : JFrame("f(xit) Launcher") {
    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        layout = BorderLayout()
        setSize(600, 300)
        setLocationRelativeTo(null)

        iconImage = loadIcon("/icons/app-icon.png", 32, 32).image

        val header = JPanel(BorderLayout()).apply {
            border = BorderFactory.createEmptyBorder(20, 20, 10, 20)
            background = UIManager.getColor("Panel.background")
        }

        val titleLabel = JLabel("Welcome to f(xit)").apply {
            font = Font("SansSerif", Font.BOLD, 24)
            horizontalAlignment = SwingConstants.CENTER
        }

        val logoLabel = JLabel(loadIcon("/icons/app-icon.png", 48, 48))
        header.add(logoLabel, BorderLayout.WEST)
        header.add(titleLabel, BorderLayout.CENTER)

        val gridPanel = JPanel(GridLayout(2, 2, 15, 15)).apply {
            border = BorderFactory.createEmptyBorder(10, 20, 20, 20)
            background = UIManager.getColor("Panel.background")
        }

        val launchButton = RoundedButton("Launch Application").apply {
            preferredSize = Dimension(140, 50)
            font = Font("SansSerif", Font.BOLD, 16)
            addActionListener {
                SwingUtilities.invokeLater {
                    val mainFrame = MainFrame()
                    mainFrame.isVisible = true
                }
                dispose()
            }
        }

        val settingsButton = RoundedButton("Settings").apply {
            preferredSize = Dimension(140, 50)
            font = Font("SansSerif", Font.BOLD, 16)
            addActionListener {
                JOptionPane.showMessageDialog(
                    this@LauncherFrame,
                    "Settings functionality not implemented yet.",
                    "Settings",
                    JOptionPane.INFORMATION_MESSAGE
                )
            }
        }

        val aboutButton = RoundedButton("About").apply {
            preferredSize = Dimension(140, 50)
            font = Font("SansSerif", Font.BOLD, 16)
            addActionListener {
                JOptionPane.showMessageDialog(
                    this@LauncherFrame,
                    "f(xit) Application\nVersion 1.0\nDeveloped by [Your Name]",
                    "About",
                    JOptionPane.INFORMATION_MESSAGE
                )
            }
        }

        val functionGrapherButton = RoundedButton("Függvény Rajzoló").apply {
            preferredSize = Dimension(140, 50)
            font = Font("SansSerif", Font.BOLD, 16)
            addActionListener {
                JOptionPane.showMessageDialog(
                    this@LauncherFrame,
                    "Függvény Rajzoló indítva!",
                    "Függvény Rajzoló",
                    JOptionPane.INFORMATION_MESSAGE
                )
            }
        }

        gridPanel.add(launchButton)
        gridPanel.add(settingsButton)
        gridPanel.add(aboutButton)
        gridPanel.add(functionGrapherButton)

        add(header, BorderLayout.NORTH)
        add(gridPanel, BorderLayout.CENTER)
    }
}


class RoundedButton(text: String) : JButton(text) {
    init {
        isContentAreaFilled = false
        isFocusPainted = false
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        val arc = 20
        g2.color = if (model.isArmed) background.darker() else background
        g2.fillRoundRect(0, 0, width, height, arc, arc)
        super.paintComponent(g)
    }

    override fun paintBorder(g: Graphics) {
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.color = foreground
        g2.drawRoundRect(0, 0, width - 1, height - 1, 20, 20)
    }

    override fun contains(x: Int, y: Int): Boolean {
        val shape = RoundRectangle2D.Float(0f, 0f, width.toFloat(), height.toFloat(), 20f, 20f)
        return shape.contains(x.toDouble(), y.toDouble())
    }
}
