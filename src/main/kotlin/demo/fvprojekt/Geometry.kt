import com.formdev.flatlaf.FlatDarkLaf
import java.awt.*
import java.awt.event.*
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.*
import kotlin.math.*

// ----------------------------------
// Geometriai alakzatok absztrakt osztálya és implementációi
// ----------------------------------

sealed class GeometryShape(open var color: Color, open var isSelected: Boolean = false) {
    abstract fun draw(g: Graphics2D)
    abstract fun area(): Double
    abstract fun perimeter(): Double
    abstract fun contains(point: Point): Boolean
}

data class Circle(var center: Point, var radius: Double, override var color: Color = Color.ORANGE, override var isSelected: Boolean = false) : GeometryShape(color, isSelected) {
    override fun draw(g: Graphics2D) {
        g.color = color
        val d = (2 * radius).toInt()
        g.fillOval((center.x - radius).toInt(), (center.y - radius).toInt(), d, d)
        // Animált kijelölés: pulzáló piros kör
        if (isSelected) {
            val pulse = (10 * sin(System.currentTimeMillis() / 300.0)).toInt()
            g.color = Color.RED
            g.stroke = BasicStroke(3f)
            g.drawOval((center.x - radius - pulse).toInt(), (center.y - radius - pulse).toInt(), d + 2 * pulse, d + 2 * pulse)
        }
    }
    override fun area(): Double = Math.PI * radius * radius
    override fun perimeter(): Double = 2 * Math.PI * radius
    override fun contains(point: Point): Boolean {
        val dx = point.x - center.x
        val dy = point.y - center.y
        return sqrt((dx * dx + dy * dy).toDouble()) <= radius
    }
}

data class RectangleShape(var topLeft: Point, var width: Double, var height: Double, override var color: Color = Color.GREEN, override var isSelected: Boolean = false) : GeometryShape(color, isSelected) {
    override fun draw(g: Graphics2D) {
        g.color = color
        g.fillRect(topLeft.x, topLeft.y, width.toInt(), height.toInt())
        if (isSelected) {
            val pulse = (5 * sin(System.currentTimeMillis() / 300.0)).toInt()
            g.color = Color.RED
            g.stroke = BasicStroke(3f)
            g.drawRect(topLeft.x - pulse, topLeft.y - pulse, width.toInt() + 2 * pulse, height.toInt() + 2 * pulse)
        }
    }
    override fun area(): Double = width * height
    override fun perimeter(): Double = 2 * (width + height)
    override fun contains(point: Point): Boolean {
        return point.x in topLeft.x..(topLeft.x + width.toInt()) && point.y in topLeft.y..(topLeft.y + height.toInt())
    }
}

data class TriangleShape(var p1: Point, var p2: Point, var p3: Point, override var color: Color = Color.CYAN, override var isSelected: Boolean = false) : GeometryShape(color, isSelected) {
    override fun draw(g: Graphics2D) {
        g.color = color
        val xs = intArrayOf(p1.x, p2.x, p3.x)
        val ys = intArrayOf(p1.y, p2.y, p3.y)
        g.fillPolygon(xs, ys, 3)
        if (isSelected) {
            val pulse = (5 * sin(System.currentTimeMillis() / 300.0)).toInt()
            g.color = Color.RED
            g.stroke = BasicStroke(3f)
            val minX = listOf(p1.x, p2.x, p3.x).minOrNull() ?: 0
            val minY = listOf(p1.y, p2.y, p3.y).minOrNull() ?: 0
            val maxX = listOf(p1.x, p2.x, p3.x).maxOrNull() ?: 0
            val maxY = listOf(p1.y, p2.y, p3.y).maxOrNull() ?: 0
            g.drawRect(minX - pulse, minY - pulse, (maxX - minX) + 2 * pulse, (maxY - minY) + 2 * pulse)
        }
    }
    override fun area(): Double = abs((p1.x * (p2.y - p3.y) + p2.x * (p3.y - p1.y) + p3.x * (p1.y - p2.y)) / 2.0)
    override fun perimeter(): Double {
        fun distance(a: Point, b: Point) = sqrt(((a.x - b.x).toDouble()).pow(2) + ((a.y - b.y).toDouble()).pow(2))
        return distance(p1, p2) + distance(p2, p3) + distance(p3, p1)
    }
    override fun contains(point: Point): Boolean {
        // Barycentrikus koordináták segítségével
        fun sign(p1: Point, p2: Point, p3: Point) = (p1.x - p3.x) * (p2.y - p3.y) - (p2.x - p3.x) * (p1.y - p3.y)
        val d1 = sign(point, p1, p2)
        val d2 = sign(point, p2, p3)
        val d3 = sign(point, p3, p1)
        val hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0)
        val hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0)
        return !(hasNeg && hasPos)
    }
}

data class PolygonShape(var points: MutableList<Point>, override var color: Color = Color.MAGENTA, override var isSelected: Boolean = false) : GeometryShape(color, isSelected) {
    override fun draw(g: Graphics2D) {
        if (points.size < 3) return
        val xs = points.map { it.x }.toIntArray()
        val ys = points.map { it.y }.toIntArray()
        g.color = color
        g.fillPolygon(xs, ys, points.size)
        if (isSelected) {
            val pulse = (5 * sin(System.currentTimeMillis() / 300.0)).toInt()
            g.color = Color.RED
            g.stroke = BasicStroke(3f)
            val minX = points.minByOrNull { it.x }?.x ?: 0
            val minY = points.minByOrNull { it.y }?.y ?: 0
            val maxX = points.maxByOrNull { it.x }?.x ?: 0
            val maxY = points.maxByOrNull { it.y }?.y ?: 0
            g.drawRect(minX - pulse, minY - pulse, (maxX - minX) + 2 * pulse, (maxY - minY) + 2 * pulse)
        }
    }
    override fun area(): Double {
        var sum = 0.0
        for (i in points.indices) {
            val j = (i + 1) % points.size
            sum += points[i].x * points[j].y - points[j].x * points[i].y
        }
        return abs(sum) / 2.0
    }
    override fun perimeter(): Double {
        var sum = 0.0
        for (i in points.indices) {
            val j = (i + 1) % points.size
            val dx = (points[j].x - points[i].x).toDouble()
            val dy = (points[j].y - points[i].y).toDouble()
            sum += sqrt(dx * dx + dy * dy)
        }
        return sum
    }
    override fun contains(point: Point): Boolean {
        var result = false
        var j = points.size - 1
        for (i in points.indices) {
            if ((points[i].y > point.y) != (points[j].y > point.y) &&
                (point.x < (points[j].x - points[i].x) * (point.y - points[i].y) / (points[j].y - points[i].y) + points[i].x)) {
                result = !result
            }
            j = i
        }
        return result
    }
}

// ----------------------------------
// Rajzoló panel, ahol az alakzatok interaktív módon rajzolódnak, mozgatás és kijelölés támogatásával
// ----------------------------------

class ComplexGeometryPanel : JPanel() {
    val shapes = mutableListOf<GeometryShape>()
    var selectedShape: GeometryShape? = null
    var dragOffset = Point(0, 0)

    init {
        background = Color(50, 50, 50)
        // Egérfigyelő: kiválasztás és mozgatás
        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                // Az alakzatok "felülről lefelé" keresése, hogy a legfelső legyen kiválasztva
                selectedShape = shapes.reversed().find { it.contains(e.point) }
                shapes.forEach { it.isSelected = false }
                selectedShape?.isSelected = true
                if (selectedShape != null) {
                    when (selectedShape) {
                        is Circle -> {
                            val circle = selectedShape as Circle
                            dragOffset = Point(e.x - circle.center.x, e.y - circle.center.y)
                        }
                        is RectangleShape -> {
                            val rect = selectedShape as RectangleShape
                            dragOffset = Point(e.x - rect.topLeft.x, e.y - rect.topLeft.y)
                        }
                        is TriangleShape -> {
                            val tri = selectedShape as TriangleShape
                            dragOffset = Point(e.x - tri.p1.x, e.y - tri.p1.y)
                        }
                        is PolygonShape -> {
                            val poly = selectedShape as PolygonShape
                            dragOffset = Point(e.x - poly.points.first().x, e.y - poly.points.first().y)
                        }
                    }
                }
                repaint()
            }
            override fun mouseReleased(e: MouseEvent) {
                dragOffset = Point(0, 0)
            }
        })

        addMouseMotionListener(object : MouseAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                selectedShape?.let { shape ->
                    when (shape) {
                        is Circle -> {
                            shape.center = Point(e.x - dragOffset.x, e.y - dragOffset.y)
                        }
                        is RectangleShape -> {
                            shape.topLeft = Point(e.x - dragOffset.x, e.y - dragOffset.y)
                        }
                        is TriangleShape -> {
                            val dx = e.x - dragOffset.x - shape.p1.x
                            val dy = e.y - dragOffset.y - shape.p1.y
                            shape.p1 = Point(shape.p1.x + dx, shape.p1.y + dy)
                            shape.p2 = Point(shape.p2.x + dx, shape.p2.y + dy)
                            shape.p3 = Point(shape.p3.x + dx, shape.p3.y + dy)
                        }
                        is PolygonShape -> {
                            val dx = e.x - dragOffset.x - shape.points.first().x
                            val dy = e.y - dragOffset.y - shape.points.first().y
                            for (i in shape.points.indices) {
                                shape.points[i] = Point(shape.points[i].x + dx, shape.points[i].y + dy)
                            }
                        }
                    }
                    repaint()
                }
            }
        })

        // Timer a folyamatos animációhoz (pl. pulzáló kijelölés)
        Timer(30) { repaint() }.start()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        for (shape in shapes) {
            shape.draw(g2)
        }
    }
}

// ----------------------------------
// Főablak – modern eszköztár ikonokkal, exportálási lehetőséggel és egy külön panelen megjelenő alakzati adatokkal
// ----------------------------------

class ComplexGeometryFrame : JFrame("Modern Geometriai Ábrázoló") {
    private val geometryPanel = ComplexGeometryPanel()
    private val propertiesText = JTextArea(8, 20).apply {
        isEditable = false
        background = Color(30, 30, 30)
        foreground = Color.WHITE
    }
    private val propertiesScroll = JScrollPane(propertiesText)

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        layout = BorderLayout()
        add(geometryPanel, BorderLayout.CENTER)
        add(propertiesScroll, BorderLayout.EAST)

        // Eszköztár – ikonok betöltése (feltételezzük, hogy a /icons mappa tartalmazza a megfelelő képeket)
        val toolBar = JToolBar()
        toolBar.isFloatable = false

        val btnAddCircle = JButton(ImageIcon(javaClass.getResource("/icons/circle.png")))
        btnAddCircle.toolTipText = "Új kör hozzáadása"
        btnAddCircle.addActionListener {
            val center = Point(geometryPanel.width / 2, geometryPanel.height / 2)
            geometryPanel.shapes.add(Circle(center, 50.0))
            geometryPanel.repaint()
        }

        val btnAddRectangle = JButton(ImageIcon(javaClass.getResource("/icons/rectangle.png")))
        btnAddRectangle.toolTipText = "Új téglalap hozzáadása"
        btnAddRectangle.addActionListener {
            geometryPanel.shapes.add(RectangleShape(Point(100, 100), 120.0, 80.0))
            geometryPanel.repaint()
        }

        val btnAddTriangle = JButton(ImageIcon(javaClass.getResource("/icons/triangle.png")))
        btnAddTriangle.toolTipText = "Új háromszög hozzáadása"
        btnAddTriangle.addActionListener {
            geometryPanel.shapes.add(TriangleShape(Point(200, 200), Point(250, 300), Point(150, 300)))
            geometryPanel.repaint()
        }

        val btnAddPolygon = JButton(ImageIcon(javaClass.getResource("/icons/polygon.png")))
        btnAddPolygon.toolTipText = "Új sokszög hozzáadása"
        btnAddPolygon.addActionListener {
            // Például egy ötszög középponttal a panel közepén
            val center = Point(geometryPanel.width / 2, geometryPanel.height / 2)
            val points = mutableListOf<Point>()
            val sides = 5
            val radius = 60
            for (i in 0 until sides) {
                val angle = Math.toRadians((360.0 / sides * i) - 90)
                val x = center.x + (radius * cos(angle)).toInt()
                val y = center.y + (radius * sin(angle)).toInt()
                points.add(Point(x, y))
            }
            geometryPanel.shapes.add(PolygonShape(points))
            geometryPanel.repaint()
        }

        val btnExport = JButton(ImageIcon(javaClass.getResource("/icons/export.png")))
        btnExport.toolTipText = "Exportálás PNG formátumban"
        btnExport.addActionListener { exportPanel() }

        toolBar.add(btnAddCircle)
        toolBar.add(btnAddRectangle)
        toolBar.add(btnAddTriangle)
        toolBar.add(btnAddPolygon)
        toolBar.addSeparator()
        toolBar.add(btnExport)
        add(toolBar, BorderLayout.NORTH)

        // Egy időzítő frissíti a jobb oldali panelt a kiválasztott alakzat adataival
        Timer(500) {
            val s = geometryPanel.selectedShape
            propertiesText.text = if (s != null) {
                when (s) {
                    is Circle -> "Kör\nKözéppont: (${s.center.x}, ${s.center.y})\nSugár: ${"%.2f".format(s.radius)}\nTerület: ${"%.2f".format(s.area())}\nKerület: ${"%.2f".format(s.perimeter())}"
                    is RectangleShape -> "Téglalap\nBal felső: (${s.topLeft.x}, ${s.topLeft.y})\nSzélesség: ${"%.2f".format(s.width)}\nMagasság: ${"%.2f".format(s.height)}\nTerület: ${"%.2f".format(s.area())}\nKerület: ${"%.2f".format(s.perimeter())}"
                    is TriangleShape -> "Háromszög\nP1: (${s.p1.x}, ${s.p1.y})\nP2: (${s.p2.x}, ${s.p2.y})\nP3: (${s.p3.x}, ${s.p3.y})\nTerület: ${"%.2f".format(s.area())}\nKerület: ${"%.2f".format(s.perimeter())}"
                    is PolygonShape -> "Sokszög\nPontok: ${s.points.joinToString { "(${it.x}, ${it.y})" }}\nTerület: ${"%.2f".format(s.area())}\nKerület: ${"%.2f".format(s.perimeter())}"
                    else -> ""
                }
            } else {
                "Nincs kiválasztott alakzat."
            }
        }.start()

        setSize(1000, 700)
        setLocationRelativeTo(null)
    }

    private fun exportPanel() {
        val fileChooser = JFileChooser()
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            var file = fileChooser.selectedFile
            if (!file.name.endsWith(".png")) {
                file = file.parentFile.resolve(file.name + ".png")
            }
            val image = BufferedImage(geometryPanel.width, geometryPanel.height, BufferedImage.TYPE_INT_ARGB)
            val g = image.createGraphics()
            geometryPanel.paint(g)
            g.dispose()
            try {
                ImageIO.write(image, "png", file)
                JOptionPane.showMessageDialog(this, "Export sikeres!")
            } catch (ex: Exception) {
                JOptionPane.showMessageDialog(this, "Hiba az exportálás során: ${ex.message}")
            }
        }
    }
}

fun main() {
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(FlatDarkLaf())
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        ComplexGeometryFrame().isVisible = true
    }
}
