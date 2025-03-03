import com.formdev.flatlaf.FlatDarkLaf
import java.awt.*
import java.awt.event.*
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.*
import kotlin.math.*

sealed class GeometryShape(open var color: Color, open var isSelected: Boolean = false) {
    abstract fun draw(g: Graphics2D)
    abstract fun area(): Double
    abstract fun perimeter(): Double
    abstract fun contains(point: Point): Boolean
}

data class Circle(
    var center: Point,
    var radius: Double,
    override var color: Color = Color.ORANGE,
    override var isSelected: Boolean = false
) : GeometryShape(color, isSelected) {

    override fun draw(g: Graphics2D) {
        g.color = color
        val d = (2 * radius).toInt()
        g.fillOval((center.x - radius).toInt(), (center.y - radius).toInt(), d, d)
        if (isSelected) {
            val pulse = (10 * sin(System.currentTimeMillis() / 300.0)).toInt()
            g.color = Color.RED
            g.stroke = BasicStroke(3f)
            g.drawOval(
                (center.x - radius - pulse).toInt(),
                (center.y - radius - pulse).toInt(),
                d + 2 * pulse,
                d + 2 * pulse
            )
        }
    }

    override fun area() = Math.PI * radius * radius
    override fun perimeter() = 2 * Math.PI * radius

    override fun contains(point: Point): Boolean {
        val dx = point.x - center.x
        val dy = point.y - center.y
        return sqrt((dx * dx + dy * dy).toDouble()) <= radius
    }
}

// Téglalap
data class RectangleShape(
    var topLeft: Point,
    var width: Double,
    var height: Double,
    override var color: Color = Color.GREEN,
    override var isSelected: Boolean = false
) : GeometryShape(color, isSelected) {

    override fun draw(g: Graphics2D) {
        g.color = color
        g.fillRect(topLeft.x, topLeft.y, width.toInt(), height.toInt())
        if (isSelected) {
            val pulse = (5 * sin(System.currentTimeMillis() / 300.0)).toInt()
            g.color = Color.RED
            g.stroke = BasicStroke(3f)
            g.drawRect(
                topLeft.x - pulse,
                topLeft.y - pulse,
                width.toInt() + 2 * pulse,
                height.toInt() + 2 * pulse
            )
        }
    }

    override fun area() = width * height
    override fun perimeter() = 2 * (width + height)

    override fun contains(point: Point): Boolean {
        return point.x in topLeft.x..(topLeft.x + width.toInt()) &&
                point.y in topLeft.y..(topLeft.y + height.toInt())
    }
}

// Háromszög
data class TriangleShape(
    var p1: Point,
    var p2: Point,
    var p3: Point,
    override var color: Color = Color.CYAN,
    override var isSelected: Boolean = false
) : GeometryShape(color, isSelected) {

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
            g.drawRect(
                minX - pulse, minY - pulse,
                (maxX - minX) + 2 * pulse,
                (maxY - minY) + 2 * pulse
            )
        }
    }

    override fun area(): Double {
        return abs((p1.x * (p2.y - p3.y) +
                p2.x * (p3.y - p1.y) +
                p3.x * (p1.y - p2.y)) / 2.0)
    }

    override fun perimeter(): Double {
        fun distance(a: Point, b: Point) =
            sqrt(((a.x - b.x).toDouble()).pow(2) + ((a.y - b.y).toDouble()).pow(2))
        return distance(p1, p2) + distance(p2, p3) + distance(p3, p1)
    }

    override fun contains(point: Point): Boolean {
        fun sign(p1: Point, p2: Point, p3: Point) =
            (p1.x - p3.x) * (p2.y - p3.y) - (p2.x - p3.x) * (p1.y - p3.y)
        val d1 = sign(point, p1, p2)
        val d2 = sign(point, p2, p3)
        val d3 = sign(point, p3, p1)
        val hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0)
        val hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0)
        return !(hasNeg && hasPos)
    }

    fun angles(): Triple<Double, Double, Double> {
        fun distance(a: Point, b: Point) =
            sqrt(((a.x - b.x).toDouble()).pow(2) + ((a.y - b.y).toDouble()).pow(2))
        val a = distance(p2, p3)
        val b = distance(p1, p3)
        val c = distance(p1, p2)
        val angleA = acos(((b*b + c*c - a*a) / (2*b*c)).coerceIn(-1.0, 1.0)) * 180 / Math.PI
        val angleB = acos(((a*a + c*c - b*b) / (2*a*c)).coerceIn(-1.0, 1.0)) * 180 / Math.PI
        val angleC = 180 - angleA - angleB
        return Triple(angleA, angleB, angleC)
    }
}

// Sokszög
data class PolygonShape(
    var points: MutableList<Point>,
    override var color: Color = Color.MAGENTA,
    override var isSelected: Boolean = false
) : GeometryShape(color, isSelected) {

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
            g.drawRect(
                minX - pulse, minY - pulse,
                (maxX - minX) + 2 * pulse,
                (maxY - minY) + 2 * pulse
            )
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
                (point.x < (points[j].x - points[i].x) * (point.y - points[i].y) /
                        (points[j].y - points[i].y) + points[i].x)) {
                result = !result
            }
            j = i
        }
        return result
    }

    fun angles(): List<Double> {
        val angles = mutableListOf<Double>()
        for (i in points.indices) {
            val prev = points[(i - 1 + points.size) % points.size]
            val current = points[i]
            val next = points[(i + 1) % points.size]
            angles.add(angleBetween(prev, current, next))
        }
        return angles
    }

    private fun angleBetween(a: Point, b: Point, c: Point): Double {
        val ab = Point(b.x - a.x, b.y - a.y)
        val cb = Point(b.x - c.x, b.y - c.y)
        val dot = ab.x * cb.x + ab.y * cb.y
        val magAB = sqrt(ab.x.toDouble().pow(2) + ab.y.toDouble().pow(2))
        val magCB = sqrt(cb.x.toDouble().pow(2) + cb.y.toDouble().pow(2))
        return acos((dot / (magAB * magCB)).coerceIn(-1.0, 1.0)) * 180 / Math.PI
    }
}

class ComplexGeometryPanel : JPanel() {
    val shapes = mutableListOf<GeometryShape>()
    var selectedShape: GeometryShape? = null
    private var dragOffset = Point(0, 0)

    init {
        background = Color(50, 50, 50)

        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
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
                        }else -> "Ismeretlen alakzat"
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
                        } else ->{
                        "Nincs kiválasztott alakzat."
                    }
                    }
                    repaint()
                }
            }
        })

        Timer(30) { repaint() }.start()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        drawGrid(g2)
        for (shape in shapes) {
            shape.draw(g2)
        }
    }

    private fun drawGrid(g2: Graphics2D) {
        val gridSize = 20
        g2.color = Color(80, 80, 80)
        for (x in 0 until width step gridSize) {
            g2.drawLine(x, 0, x, height)
        }
        for (y in 0 until height step gridSize) {
            g2.drawLine(0, y, width, y)
        }
    }
}

class ComplexGeometryFrame : JFrame("F(xit) Geo") {
    private val geometryPanel = ComplexGeometryPanel()
    private val propertiesText = JTextArea(10, 20).apply {
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

        val toolBar = JToolBar().apply { isFloatable = false }

        val btnAddCircle = JButton(ImageIcon(javaClass.getResource("/icons/circle.png"))).apply {
            toolTipText = "Kör hozzáadása"
            addActionListener {
                geometryPanel.shapes.add(
                    Circle(Point(geometryPanel.width / 2, geometryPanel.height / 2), 50.0)
                )
                geometryPanel.repaint()
            }
        }
        val btnAddRectangle = JButton(ImageIcon(javaClass.getResource("/icons/rectangle.png"))).apply {
            toolTipText = "Téglalap hozzáadása"
            addActionListener {
                geometryPanel.shapes.add(
                    RectangleShape(Point(100, 100), 120.0, 80.0)
                )
                geometryPanel.repaint()
            }
        }
        val btnAddTriangle = JButton(ImageIcon(javaClass.getResource("/icons/triangle.png"))).apply {
            toolTipText = "Háromszög hozzáadása"
            addActionListener {
                geometryPanel.shapes.add(
                    TriangleShape(Point(200, 200), Point(250, 300), Point(150, 300))
                )
                geometryPanel.repaint()
            }
        }
        val btnAddPolygon = JButton(ImageIcon(javaClass.getResource("/icons/polygon.png"))).apply {
            toolTipText = "Sokszög hozzáadása"
            addActionListener {
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
        }
        val btnEdit = JButton("Szerkesztés").apply {
            toolTipText = "Kiválasztott alakzat szerkesztése"
            addActionListener {
                val s = geometryPanel.selectedShape
                if (s is Circle) {
                    val centerX = JOptionPane.showInputDialog(this, "Középpont X:", s.center.x)?.toIntOrNull() ?: s.center.x
                    val centerY = JOptionPane.showInputDialog(this, "Középpont Y:", s.center.y)?.toIntOrNull() ?: s.center.y
                    val radius = JOptionPane.showInputDialog(this, "Sugár:", s.radius)?.toDoubleOrNull() ?: s.radius
                    s.center = Point(centerX, centerY)
                    s.radius = radius
                } else if (s is RectangleShape) {
                    val x = JOptionPane.showInputDialog(this, "Bal felső X:", s.topLeft.x)?.toIntOrNull() ?: s.topLeft.x
                    val y = JOptionPane.showInputDialog(this, "Bal felső Y:", s.topLeft.y)?.toIntOrNull() ?: s.topLeft.y
                    val width = JOptionPane.showInputDialog(this, "Szélesség:", s.width)?.toDoubleOrNull() ?: s.width
                    val height = JOptionPane.showInputDialog(this, "Magasság:", s.height)?.toDoubleOrNull() ?: s.height
                    s.topLeft = Point(x, y)
                    s.width = width
                    s.height = height
                } else if (s is TriangleShape) {
                    val p1x = JOptionPane.showInputDialog(this, "P1 X:", s.p1.x)?.toIntOrNull() ?: s.p1.x
                    val p1y = JOptionPane.showInputDialog(this, "P1 Y:", s.p1.y)?.toIntOrNull() ?: s.p1.y
                    val p2x = JOptionPane.showInputDialog(this, "P2 X:", s.p2.x)?.toIntOrNull() ?: s.p2.x
                    val p2y = JOptionPane.showInputDialog(this, "P2 Y:", s.p2.y)?.toIntOrNull() ?: s.p2.y
                    val p3x = JOptionPane.showInputDialog(this, "P3 X:", s.p3.x)?.toIntOrNull() ?: s.p3.x
                    val p3y = JOptionPane.showInputDialog(this, "P3 Y:", s.p3.y)?.toIntOrNull() ?: s.p3.y
                    s.p1 = Point(p1x, p1y)
                    s.p2 = Point(p2x, p2y)
                    s.p3 = Point(p3x, p3y)
                } else {
                    JOptionPane.showMessageDialog(this, "Ez a forma nem szerkeszthető vagy nem támogatott.")
                }
                geometryPanel.repaint()
            }
        }
        val btnAngles = JButton("Szögek").apply {
            toolTipText = "Kiválasztott háromszög vagy sokszög szögeinek számítása"
            addActionListener {
                val s = geometryPanel.selectedShape
                if (s is TriangleShape) {
                    val (a, b, c) = s.angles()
                    JOptionPane.showMessageDialog(
                        this,
                        "Szögek: ${"%.2f".format(a)}°, ${"%.2f".format(b)}°, ${"%.2f".format(c)}°"
                    )
                } else if (s is PolygonShape) {
                    val angles = s.angles()
                    JOptionPane.showMessageDialog(
                        this,
                        "Szögek: ${angles.joinToString(", ") { "%.2f".format(it) + "°" }}"
                    )
                } else {
                    JOptionPane.showMessageDialog(this, "A kiválasztott alakzathoz nem számolható szög.")
                }
            }
        }
        val btnExport = JButton(ImageIcon(javaClass.getResource("/icons/export.png"))).apply {
            toolTipText = "Exportálás PNG formátumban"
            addActionListener { exportPanel() }
        }

        toolBar.add(btnAddCircle)
        toolBar.add(btnAddRectangle)
        toolBar.add(btnAddTriangle)
        toolBar.add(btnAddPolygon)
        toolBar.add(btnEdit)
        toolBar.add(btnAngles)
        toolBar.add(btnExport)

        add(toolBar, BorderLayout.NORTH)

        Timer(500) {
            val s = geometryPanel.selectedShape
            propertiesText.text = if (s != null) {
                when (s) {
                    is Circle -> {
                        "Kör\n" +
                                "Középpont: (${s.center.x}, ${s.center.y})\n" +
                                "Sugár: ${"%.2f".format(s.radius)}\n" +
                                "Terület: ${"%.2f".format(s.area())}\n" +
                                "Kerület: ${"%.2f".format(s.perimeter())}"
                    }
                    is RectangleShape -> {
                        "Téglalap\n" +
                                "Bal felső: (${s.topLeft.x}, ${s.topLeft.y})\n" +
                                "Szélesség: ${"%.2f".format(s.width)}\n" +
                                "Magasság: ${"%.2f".format(s.height)}\n" +
                                "Terület: ${"%.2f".format(s.area())}\n" +
                                "Kerület: ${"%.2f".format(s.perimeter())}"
                    }
                    is TriangleShape -> {
                        val (a, b, c) = s.angles()
                        "Háromszög\n" +
                                "P1: (${s.p1.x}, ${s.p1.y})\n" +
                                "P2: (${s.p2.x}, ${s.p2.y})\n" +
                                "P3: (${s.p3.x}, ${s.p3.y})\n" +
                                "Szögek: ${"%.2f".format(a)}°, ${"%.2f".format(b)}°, ${"%.2f".format(c)}°\n" +
                                "Terület: ${"%.2f".format(s.area())}\n" +
                                "Kerület: ${"%.2f".format(s.perimeter())}"
                    }
                    is PolygonShape -> {
                        "Sokszög\n" +
                                "Pontok: ${s.points.joinToString { "(${it.x}, ${it.y})" }}\n" +
                                "Szögek: ${s.angles().joinToString(", ") { "%.2f".format(it) + "°" }}\n" +
                                "Terület: ${"%.2f".format(s.area())}\n" +
                                "Kerület: ${"%.2f".format(s.perimeter())}"
                    }
                    else -> "Ismeretlen alakzat"
                }
            } else {
                "Nincs kiválasztott alakzat."
            }
        }.start()

        setSize(1100, 700)
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
                JOptionPane.showMessageDialog(this, "Hiba: ${ex.message}")
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
