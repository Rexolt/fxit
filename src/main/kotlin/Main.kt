import javax.swing.*
import java.awt.*
import java.awt.event.ActionListener

data class LinearisFuggveny(val m: Double, val b: Double) {
    fun f(x: Double): Double = m * x + b
}
fun parseLinearisFuggveny(bemenet: String): LinearisFuggveny {
    var s = bemenet.replace(" ", "").lowercase()
    if (s.startsWith("f(x)=")) {
        s = s.substring(5)
    }
    val xIndex = s.indexOf('x')
    if (xIndex == -1) {
        throw IllegalArgumentException("A kifejezés nem tartalmaz 'x' betűt!")
    }
    val mResz = s.substring(0, xIndex)
    val m: Double = when {
        mResz.isEmpty() || mResz == "+" -> 1.0
        mResz == "-" -> -1.0
        mResz.endsWith("*") -> mResz.substring(0, mResz.length - 1).toDouble()
        else -> mResz.toDouble()
    }

    val bResz = s.substring(xIndex + 1)
    val b: Double = if (bResz.isEmpty()) 0.0 else bResz.toDouble()
    return LinearisFuggveny(m, b)
}

class RajzPanel : JPanel() {
    var fuggveny1: LinearisFuggveny? = null
    var fuggveny2: LinearisFuggveny? = null
    var metszesPont: Pair<Double, Double>? = null
    var zerushely1: Double? = null
    var zerushely2: Double? = null

    val mertek = 25.0

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)


        g2.color = Color(30, 30, 30)
        g2.fillRect(0, 0, width, height)
        g2.color = Color(50, 50, 50)
        val centerX = width / 2
        val centerY = height / 2
        val lepes = mertek.toInt()
        var x = centerX
        while (x < width) {
            g2.drawLine(x, 0, x, height)
            x += lepes
        }
        x = centerX
        while (x >= 0) {
            g2.drawLine(x, 0, x, height)
            x -= lepes
        }
        var y = centerY
        while (y < height) {
            g2.drawLine(0, y, width, y)
            y += lepes
        }
        y = centerY
        while (y >= 0) {
            g2.drawLine(0, y, width, y)
            y -= lepes
        }

        g2.color = Color.WHITE
        g2.drawLine(0, centerY, width, centerY)
        g2.drawLine(centerX, 0, centerX, height)


        fuggveny1?.let { f1 ->
            g2.color = Color.ORANGE
            g2.stroke = BasicStroke(2f)
            val p1 = getPanelKoordinataFuggvenyhez(f1, -centerX / mertek)
            val p2 = getPanelKoordinataFuggvenyhez(f1, (width - centerX) / mertek)
            g2.drawLine(p1.first, p1.second, p2.first, p2.second)
        }

        fuggveny2?.let { f2 ->
            g2.color = Color.CYAN
            g2.stroke = BasicStroke(2f)
            val p1 = getPanelKoordinataFuggvenyhez(f2, -centerX / mertek)
            val p2 = getPanelKoordinataFuggvenyhez(f2, (width - centerX) / mertek)
            g2.drawLine(p1.first, p1.second, p2.first, p2.second)
        }


        g2.color = Color.MAGENTA
        zerushely1?.let { z1 ->
            val p = toPanelKoordinata(z1, fuggveny1!!.f(z1), centerX, centerY)
            g2.fillOval(p.first - 4, p.second - 4, 8, 8)
        }
        zerushely2?.let { z2 ->
            val p = toPanelKoordinata(z2, fuggveny2!!.f(z2), centerX, centerY)
            g2.fillOval(p.first - 4, p.second - 4, 8, 8)
        }

        metszesPont?.let { (mx, my) ->
            g2.color = Color.YELLOW
            val p = toPanelKoordinata(mx, my, centerX, centerY)
            g2.fillOval(p.first - 5, p.second - 5, 10, 10)
        }
    }
    private fun getPanelKoordinataFuggvenyhez(f: LinearisFuggveny, x: Double): Pair<Int, Int> {
        val centerX = width / 2
        val centerY = height / 2
        val y = f.f(x)
        return toPanelKoordinata(x, y, centerX, centerY)
    }

    private fun toPanelKoordinata(x: Double, y: Double, centerX: Int, centerY: Int): Pair<Int, Int> {
        val px = centerX + (x * mertek).toInt()
        val py = centerY - (y * mertek).toInt() // a képernyőn lefelé növekszik az y
        return Pair(px, py)
    }
}


fun hozzaLetreGUI() {
    val frame = JFrame("Lineáris függvények megoldó")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.layout = BorderLayout()
    val rajzPanel = RajzPanel()
    rajzPanel.preferredSize = Dimension(600, 600)
    val vezeroPanel = JPanel()
    vezeroPanel.layout = GridBagLayout()
    vezeroPanel.background = Color(30, 30, 30)
    val gbc = GridBagConstraints().apply {
        insets = Insets(5, 5, 5, 5)
        fill = GridBagConstraints.HORIZONTAL
    }
    val labelF1 = JLabel("Első függvény (f1(x)= m*x + b):")
    labelF1.foreground = Color.WHITE
    gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1
    vezeroPanel.add(labelF1, gbc)
    val textF1 = JTextField(20)
    textF1.background = Color(50, 50, 50)
    textF1.foreground = Color.WHITE
    gbc.gridx = 1; gbc.gridy = 0
    vezeroPanel.add(textF1, gbc)
    val labelF2 = JLabel("Második függvény (f2(x)= m*x + b):")
    labelF2.foreground = Color.WHITE
    gbc.gridx = 0; gbc.gridy = 1
    vezeroPanel.add(labelF2, gbc)
    val textF2 = JTextField(20)
    textF2.background = Color(50, 50, 50)
    textF2.foreground = Color.WHITE
    gbc.gridx = 1; gbc.gridy = 1
    vezeroPanel.add(textF2, gbc)
    val szamitGomb = JButton("Számol és Ábrázol")
    szamitGomb.background = Color(70, 70, 70)
    szamitGomb.foreground = Color.WHITE
    gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2
    vezeroPanel.add(szamitGomb, gbc)
    val eredmenyLabel = JLabel("<html>Eredmények ide...</html>")
    eredmenyLabel.foreground = Color.WHITE
    gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2
    vezeroPanel.add(eredmenyLabel, gbc)
    val akcioValtozo = ActionListener {
        try {
            val f1 = parseLinearisFuggveny(textF1.text)
            val f2 = parseLinearisFuggveny(textF2.text)
            val zerushely1 = if (f1.m != 0.0) -f1.b / f1.m else null
            val zerushely2 = if (f2.m != 0.0) -f2.b / f2.m else null
            val metszesPont: Pair<Double, Double>? = if (f1.m != f2.m) {
                val x = (f2.b - f1.b) / (f1.m - f2.m)
                val y = f1.f(x)
                Pair(x, y)
            } else {
                null
            }
            rajzPanel.fuggveny1 = f1
            rajzPanel.fuggveny2 = f2
            rajzPanel.zerushely1 = zerushely1
            rajzPanel.zerushely2 = zerushely2
            rajzPanel.metszesPont = metszesPont
            rajzPanel.repaint()
            val sb = StringBuilder("<html>")
            sb.append("<b>Első függvény:</b> f1(x) = ${f1.m} * x + ${f1.b}<br>")
            if (f1.m != 0.0) {
                sb.append("Zérushely: m*x + b = 0 &rarr; x = -b/m = ${-f1.b} / ${f1.m} = ${zerushely1}<br>")
            } else {
                sb.append("Első függvény: m = 0, ezért nincs egyértelmű zérushely.<br>")
            }
            sb.append("<br><b>Második függvény:</b> f2(x) = ${f2.m} * x + ${f2.b}<br>")
            if (f2.m != 0.0) {
                sb.append("Zérushely: m*x + b = 0 &rarr; x = -b/m = ${-f2.b} / ${f2.m} = ${zerushely2}<br>")
            } else {
                sb.append("Második függvény: m = 0, ezért nincs egyértelmű zérushely.<br>")
            }
            sb.append("<br><b>Metszéspont:</b><br>")
            if (f1.m != f2.m) {
                val x = (f2.b - f1.b) / (f1.m - f2.m)
                sb.append("Egyenletek egyenlővé tétele: ${f1.m} * x + ${f1.b} = ${f2.m} * x + ${f2.b}<br>")
                sb.append("Átrendezve: (${f1.m} - ${f2.m}) * x = ${f2.b} - ${f1.b}<br>")
                sb.append("Megoldva: x = (${f2.b} - ${f1.b}) / (${f1.m} - ${f2.m}) = ${metszesPont?.first}<br>")
                sb.append("Ekkor y = f1(x) = ${f1.f(x)}<br>")
            } else {
                sb.append("A két függvény párhuzamos, így nincs egyetlen metszéspont.<br>")
            }
            sb.append("</html>")
            eredmenyLabel.text = sb.toString()

        } catch (e: Exception) {
            JOptionPane.showMessageDialog(frame, "Hiba a függvény kifejezésben:\n${e.message}", "Hiba", JOptionPane.ERROR_MESSAGE)
        }
    }
    szamitGomb.addActionListener(akcioValtozo)
    textF1.addActionListener(akcioValtozo)
    textF2.addActionListener(akcioValtozo)

    frame.add(rajzPanel, BorderLayout.CENTER)
    frame.add(vezeroPanel, BorderLayout.SOUTH)
    frame.pack()
    frame.setLocationRelativeTo(null)
    frame.isVisible = true
}

fun main() {
    SwingUtilities.invokeLater {
        hozzaLetreGUI()
    }
}
