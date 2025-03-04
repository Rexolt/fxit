import com.formdev.flatlaf.FlatDarkLaf
import java.awt.*
import java.awt.event.*
import java.awt.image.BufferedImage
import java.io.File
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
}

data class SegmentShape(
    var p1: Point,
    var p2: Point,
    override var color: Color = Color.LIGHT_GRAY,
    override var isSelected: Boolean = false
) : GeometryShape(color, isSelected) {

    override fun draw(g: Graphics2D) {
        g.color = color
        g.stroke = BasicStroke(2f)
        g.drawLine(p1.x, p1.y, p2.x, p2.y)
        if (isSelected) {
            g.color = Color.RED
            g.fillOval(p1.x - 3, p1.y - 3, 6, 6)
            g.fillOval(p2.x - 3, p2.y - 3, 6, 6)
        }
    }

    override fun area() = 0.0
    override fun perimeter() = p1.distance(p2)

    override fun contains(point: Point): Boolean {

        val dist = distanceToSegment(point, p1, p2)
        return dist < 5.0
    }

    private fun distanceToSegment(p: Point, v: Point, w: Point): Double {
        val l2 = (v.distanceSq(w))
        if (l2 == 0.0) return p.distance(v)
        var t = ((p.x - v.x) * (w.x - v.x) + (p.y - v.y) * (w.y - v.y)) / l2
        t = t.coerceIn(0.0, 1.0)
        val projX = v.x + t * (w.x - v.x)
        val projY = v.y + t * (w.y - v.y)
        return sqrt((p.x - projX).pow(2) + (p.y - projY).pow(2))
    }
}


data class RayShape(
    var start: Point,
    var direction: Point,
    override var color: Color = Color.MAGENTA,
    override var isSelected: Boolean = false
) : GeometryShape(color, isSelected) {

    override fun draw(g: Graphics2D) {
        g.color = color
        g.stroke = BasicStroke(2f)
        val length = 1000
        val dx = direction.x - start.x
        val dy = direction.y - start.y
        val mag = sqrt((dx * dx + dy * dy).toDouble())
        if (mag == 0.0) return
        val ex = (start.x + (dx / mag * length)).toInt()
        val ey = (start.y + (dy / mag * length)).toInt()
        g.drawLine(start.x, start.y, ex, ey)
        if (isSelected) {
            g.color = Color.RED
            g.fillOval(start.x - 3, start.y - 3, 6, 6)
            g.fillOval(direction.x - 3, direction.y - 3, 6, 6)
        }
    }

    override fun area() = 0.0
    override fun perimeter() = -1.0
    override fun contains(point: Point): Boolean {
        val dx = direction.x - start.x
        val dy = direction.y - start.y
        val mag = sqrt((dx * dx + dy * dy).toDouble())
        if (mag == 0.0) return false
        val t = ((point.x - start.x) * dx + (point.y - start.y) * dy) / (mag * mag)
        if (t < 0) return false
        val projX = start.x + t * dx
        val projY = start.y + t * dy
        val distance = sqrt((point.x - projX).pow(2) + (point.y - projY).pow(2))
        return distance < 5.0
    }
}

data class LineShape(
    var p1: Point,
    var p2: Point,
    override var color: Color = Color.GRAY,
    override var isSelected: Boolean = false
) : GeometryShape(color, isSelected) {

    override fun draw(g: Graphics2D) {
        g.color = color
        g.stroke = BasicStroke(2f)
        val length = 1000
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        val mag = sqrt((dx * dx + dy * dy).toDouble())
        if (mag == 0.0) return
        val normX = dx / mag
        val normY = dy / mag
        val startX = (p1.x - normX * length).toInt()
        val startY = (p1.y - normY * length).toInt()
        val endX = (p1.x + normX * length).toInt()
        val endY = (p1.y + normY * length).toInt()
        g.drawLine(startX, startY, endX, endY)
        if (isSelected) {
            g.color = Color.RED
            g.fillOval(p1.x - 3, p1.y - 3, 6, 6)
            g.fillOval(p2.x - 3, p2.y - 3, 6, 6)
        }
    }

    override fun area() = 0.0
    override fun perimeter() = -1.0
    override fun contains(point: Point): Boolean {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        val mag = sqrt((dx * dx + dy * dy).toDouble())
        if (mag == 0.0) return false
        val distance = abs(dy * point.x - dx * point.y + p2.x * p1.y - p2.y * p1.x) / mag
        return distance < 5.0
    }
}


enum class Tool {
    SELECT,
    ADD_CIRCLE,
    ADD_RECTANGLE,
    ADD_TRIANGLE,
    ADD_POLYGON,
    ADD_SEGMENT,
    ADD_RAY,
    ADD_LINE
}

// A rajzoló panel, ahol a geometriai elemek kezelése történik
class ComplexGeometryPanel : JPanel() {
    val shapes = mutableListOf<GeometryShape>()
    var selectedShape: GeometryShape? = null

    var currentTool: Tool = Tool.SELECT

    val tempPoints = mutableListOf<Point>()
    var currentMousePos: Point? = null

    private var dragOffset = Point(0, 0)

    init {
        background = Color(50, 50, 50)

        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                if (currentTool == Tool.SELECT) {
                    selectedShape = shapes.reversed().find { it.contains(e.point) }
                    shapes.forEach { it.isSelected = false }
                    selectedShape?.isSelected = true
                    selectedShape?.let { shape ->
                        when (shape) {
                            is Circle -> {
                                dragOffset = Point(e.x - shape.center.x, e.y - shape.center.y)
                            }
                            is RectangleShape -> {
                                dragOffset = Point(e.x - shape.topLeft.x, e.y - shape.topLeft.y)
                            }
                            is TriangleShape -> {
                                dragOffset = Point(e.x - shape.p1.x, e.y - shape.p1.y)
                            }
                            is PolygonShape -> {
                                dragOffset = Point(e.x - shape.points.first().x, e.y - shape.points.first().y)
                            }
                            is SegmentShape -> {
                                dragOffset = Point(e.x - shape.p1.x, e.y - shape.p1.y)
                            }
                            is RayShape -> {
                                dragOffset = Point(e.x - shape.start.x, e.y - shape.start.y)
                            }
                            is LineShape -> {
                                dragOffset = Point(e.x - shape.p1.x, e.y - shape.p1.y)
                            }
                        }
                    }
                    repaint()
                } else {
                    // DRAW mód: pontok rögzítése az aktuális eszközhöz
                    tempPoints.add(e.point)
                    // Ha a szükséges pontszám teljes, létrehozzuk az alakzatot
                    when (currentTool) {
                        Tool.ADD_CIRCLE -> if (tempPoints.size == 2) finishDrawing()
                        Tool.ADD_RECTANGLE -> if (tempPoints.size == 2) finishDrawing()
                        Tool.ADD_TRIANGLE -> if (tempPoints.size == 3) finishDrawing()
                        Tool.ADD_SEGMENT -> if (tempPoints.size == 2) finishDrawing()
                        Tool.ADD_RAY -> if (tempPoints.size == 2) finishDrawing()
                        Tool.ADD_LINE -> if (tempPoints.size == 2) finishDrawing()
                        Tool.ADD_POLYGON -> {
                            // Polygon: dupla kattintásra zárjuk le az alakzatot
                            if (e.clickCount == 2 && tempPoints.size > 2) {
                                finishDrawing()
                            }
                        }
                        else -> {}
                    }
                }
            }

            override fun mouseReleased(e: MouseEvent) {
                dragOffset = Point(0, 0)
            }
        })

        addMouseMotionListener(object : MouseAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                if (currentTool == Tool.SELECT) {
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
                            is SegmentShape -> {
                                val dx = e.x - dragOffset.x - shape.p1.x
                                val dy = e.y - dragOffset.y - shape.p1.y
                                shape.p1 = Point(shape.p1.x + dx, shape.p1.y + dy)
                                shape.p2 = Point(shape.p2.x + dx, shape.p2.y + dy)
                            }
                            is RayShape -> {
                                val dx = e.x - dragOffset.x - shape.start.x
                                val dy = e.y - dragOffset.y - shape.start.y
                                shape.start = Point(shape.start.x + dx, shape.start.y + dy)
                                shape.direction = Point(shape.direction.x + dx, shape.direction.y + dy)
                            }
                            is LineShape -> {
                                val dx = e.x - dragOffset.x - shape.p1.x
                                val dy = e.y - dragOffset.y - shape.p1.y
                                shape.p1 = Point(shape.p1.x + dx, shape.p1.y + dy)
                                shape.p2 = Point(shape.p2.x + dx, shape.p2.y + dy)
                            }
                        }
                    }
                    repaint()
                } else {
                    // DRAW mód: követjük az egér aktuális pozícióját az előnézethez
                    currentMousePos = e.point
                    repaint()
                }
            }

            override fun mouseMoved(e: MouseEvent) {
                if (currentTool != Tool.SELECT) {
                    currentMousePos = e.point
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
        shapes.forEach { it.draw(g2) }
        drawTempShape(g2)
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

    private fun drawTempShape(g2: Graphics2D) {
        if (tempPoints.isEmpty()) return
        g2.color = Color.YELLOW
        tempPoints.forEach { point ->
            g2.fillOval(point.x - 3, point.y - 3, 6, 6)
        }
        val previewPoints = mutableListOf<Point>().apply {
            addAll(tempPoints)
            currentMousePos?.let { add(it) }
        }
        when (currentTool) {
            Tool.ADD_CIRCLE, Tool.ADD_RECTANGLE, Tool.ADD_SEGMENT, Tool.ADD_RAY, Tool.ADD_LINE -> {
                if (previewPoints.size >= 2) {
                    g2.drawLine(previewPoints.first().x, previewPoints.first().y,
                        previewPoints.last().x, previewPoints.last().y)
                }
            }
            Tool.ADD_TRIANGLE -> {
                if (previewPoints.size >= 2) {
                    for (i in 0 until previewPoints.size - 1) {
                        g2.drawLine(previewPoints[i].x, previewPoints[i].y,
                            previewPoints[i + 1].x, previewPoints[i + 1].y)
                    }
                }
            }
            Tool.ADD_POLYGON -> {
                if (previewPoints.size >= 2) {
                    for (i in 0 until previewPoints.size - 1) {
                        g2.drawLine(previewPoints[i].x, previewPoints[i].y,
                            previewPoints[i + 1].x, previewPoints[i + 1].y)
                    }
                }
            }

            else -> {}
        }
    }


    private fun finishDrawing() {
        when (currentTool) {
            Tool.ADD_CIRCLE -> {
                val center = tempPoints[0]
                val edge = tempPoints[1]
                val radius = center.distance(edge)
                shapes.add(Circle(center, radius))
            }
            Tool.ADD_RECTANGLE -> {
                val p1 = tempPoints[0]
                val p2 = tempPoints[1]
                val topLeft = Point(min(p1.x, p2.x), min(p1.y, p2.y))
                val width = abs(p2.x - p1.x).toDouble()
                val height = abs(p2.y - p1.y).toDouble()
                shapes.add(RectangleShape(topLeft, width, height))
            }
            Tool.ADD_TRIANGLE -> {
                shapes.add(TriangleShape(tempPoints[0], tempPoints[1], tempPoints[2]))
            }
            Tool.ADD_POLYGON -> {
                shapes.add(PolygonShape(tempPoints.toMutableList()))
            }
            Tool.ADD_SEGMENT -> {
                shapes.add(SegmentShape(tempPoints[0], tempPoints[1]))
            }
            Tool.ADD_RAY -> {
                shapes.add(RayShape(tempPoints[0], tempPoints[1]))
            }
            Tool.ADD_LINE -> {
                shapes.add(LineShape(tempPoints[0], tempPoints[1]))
            }
            else -> {}
        }
        tempPoints.clear()
        currentMousePos = null
        repaint()
    }
}

private fun IntRange.dropLast(i: Int) {}

class ComplexGeometryFrame : JFrame("F(xit) Geo – Komplex Eszköztár") {
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

        val btnSelect = JButton("Kiválasztás").apply {
            toolTipText = "Alakzatok kiválasztása és mozgatása"
            addActionListener {
                geometryPanel.currentTool = Tool.SELECT
                geometryPanel.tempPoints.clear()
            }
        }
        val btnCircle = JButton(ImageIcon(javaClass.getResource("/icons/circle.png"))).apply {
            toolTipText = "Kör rajzolása (Körző)"
            addActionListener {
                geometryPanel.currentTool = Tool.ADD_CIRCLE
                geometryPanel.tempPoints.clear()
            }
        }
        val btnRectangle = JButton(ImageIcon(javaClass.getResource("/icons/rectangle.png"))).apply {
            toolTipText = "Téglalap rajzolása"
            addActionListener {
                geometryPanel.currentTool = Tool.ADD_RECTANGLE
                geometryPanel.tempPoints.clear()
            }
        }

        val btnTriangle = JButton(ImageIcon(javaClass.getResource("/icons/triangle.png"))).apply {
            toolTipText = "Háromszög rajzolása"
            addActionListener {
                geometryPanel.currentTool = Tool.ADD_TRIANGLE
                geometryPanel.tempPoints.clear()
            }
        }

        val btnPolygon = JButton(ImageIcon(javaClass.getResource("/icons/polygon.png"))).apply {
            toolTipText = "Sokszög rajzolása (dupla kattintás a lezáráshoz)"
            addActionListener {
                geometryPanel.currentTool = Tool.ADD_POLYGON
                geometryPanel.tempPoints.clear()
            }
        }

        val btnSegment = JButton("Szakasz").apply {
            toolTipText = "Szakasz rajzolása"
            addActionListener {
                geometryPanel.currentTool = Tool.ADD_SEGMENT
                geometryPanel.tempPoints.clear()
            }
        }
        val btnRay = JButton("Félegyenes").apply {
            toolTipText = "Félegyenes rajzolása"
            addActionListener {
                geometryPanel.currentTool = Tool.ADD_RAY
                geometryPanel.tempPoints.clear()
            }
        }
        val btnLine = JButton("Vonal").apply {
            toolTipText = "Vonal rajzolása (végtelen egyenes)"
            addActionListener {
                geometryPanel.currentTool = Tool.ADD_LINE
                geometryPanel.tempPoints.clear()
            }
        }

        toolBar.add(btnSelect)
        toolBar.add(btnCircle)
        toolBar.add(btnRectangle)
        toolBar.add(btnTriangle)
        toolBar.add(btnPolygon)
        toolBar.add(btnSegment)
        toolBar.add(btnRay)
        toolBar.add(btnLine)

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
                        "Háromszög\n" +
                                "P1: (${s.p1.x}, ${s.p1.y})\n" +
                                "P2: (${s.p2.x}, ${s.p2.y})\n" +
                                "P3: (${s.p3.x}, ${s.p3.y})\n" +
                                "Terület: ${"%.2f".format(s.area())}\n" +
                                "Kerület: ${"%.2f".format(s.perimeter())}"
                    }
                    is PolygonShape -> {
                        "Sokszög\n" +
                                "Pontok: ${s.points.joinToString { "(${it.x}, ${it.y})" }}\n" +
                                "Terület: ${"%.2f".format(s.area())}\n" +
                                "Kerület: ${"%.2f".format(s.perimeter())}"
                    }
                    is SegmentShape -> {
                        "Szakasz\n" +
                                "P1: (${s.p1.x}, ${s.p1.y})\n" +
                                "P2: (${s.p2.x}, ${s.p2.y})\n" +
                                "Hossz: ${"%.2f".format(s.perimeter())}"
                    }
                    is RayShape -> {
                        "Félegyenes\n" +
                                "Kezdőpont: (${s.start.x}, ${s.start.y})\n" +
                                "Irány: (${s.direction.x - s.start.x}, ${s.direction.y - s.start.y})"
                    }
                    is LineShape -> {
                        "Vonal\n" +
                                "P1: (${s.p1.x}, ${s.p1.y})\n" +
                                "P2: (${s.p2.x}, ${s.p2.y})"
                    }
                    else -> "Ismeretlen alakzat"
                }
            } else {
                "Nincs kiválasztott alakzat."
            }
        }.start()

        setSize(1200, 800)
        setLocationRelativeTo(null)
    }

    private fun exportPanel() {
        val fileChooser = JFileChooser()
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            var file = fileChooser.selectedFile
            if (!file.name.endsWith(".png")) {
                file = File(file.parentFile, file.name + ".png")
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
