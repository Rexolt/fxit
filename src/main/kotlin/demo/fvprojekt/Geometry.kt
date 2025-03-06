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

data class Circle(var center: Point, var radius: Double, override var color: Color = Color.ORANGE, override var isSelected: Boolean = false) : GeometryShape(color, isSelected) {
    override fun draw(g: Graphics2D) {
        g.color = color
        val d = (2 * radius).toInt()
        g.fillOval((center.x - radius).toInt(), (center.y - radius).toInt(), d, d)
        if (isSelected) {
            val pulse = (10 * sin(System.currentTimeMillis() / 300.0)).toInt()
            g.color = Color.RED
            g.stroke = BasicStroke(3f)
            g.drawOval((center.x - radius - pulse).toInt(), (center.y - radius - pulse).toInt(), d + 2 * pulse, d + 2 * pulse)
        }
    }
    override fun area() = Math.PI * radius * radius
    override fun perimeter() = 2 * Math.PI * radius
    override fun contains(point: Point) = center.distance(point) <= radius
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
    override fun area() = width * height
    override fun perimeter() = 2 * (width + height)
    override fun contains(point: Point) = point.x in topLeft.x..(topLeft.x + width.toInt()) && point.y in topLeft.y..(topLeft.y + height.toInt())
    fun center() = Point((topLeft.x + width / 2).toInt(), (topLeft.y + height / 2).toInt())
    fun scale(scale: Double) {
        val c = center()
        width *= scale
        height *= scale
        topLeft = Point((c.x - width / 2).toInt(), (c.y - height / 2).toInt())
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
    override fun area() = abs((p1.x * (p2.y - p3.y) + p2.x * (p3.y - p1.y) + p3.x * (p1.y - p2.y)) / 2.0)
    override fun perimeter() = p1.distance(p2) + p2.distance(p3) + p3.distance(p1)
    override fun contains(point: Point): Boolean {
        fun sign(a: Point, b: Point, c: Point) = (a.x - c.x) * (b.y - c.y) - (b.x - c.x) * (a.y - c.y)
        val d1 = sign(point, p1, p2)
        val d2 = sign(point, p2, p3)
        val d3 = sign(point, p3, p1)
        val hasNeg = d1 < 0 || d2 < 0 || d3 < 0
        val hasPos = d1 > 0 || d2 > 0 || d3 > 0
        return !(hasNeg && hasPos)
    }
    fun centroid() = Point(((p1.x + p2.x + p3.x) / 3), ((p1.y + p2.y + p3.y) / 3))
    fun scale(scale: Double) {
        val c = centroid()
        p1 = scalePoint(p1, c, scale)
        p2 = scalePoint(p2, c, scale)
        p3 = scalePoint(p3, c, scale)
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
            sum += points[i].distance(points[j])
        }
        return sum
    }
    override fun contains(point: Point): Boolean {
        var result = false
        var j = points.size - 1
        for (i in points.indices) {
            if ((points[i].y > point.y) != (points[j].y > point.y) &&
                (point.x < (points[j].x - points[i].x) * (point.y - points[i].y) / (points[j].y - points[i].y) + points[i].x)
            ) result = !result
            j = i
        }
        return result
    }
    fun centroid() = Point(points.map { it.x }.average().toInt(), points.map { it.y }.average().toInt())
    fun scale(scale: Double) {
        val c = centroid()
        points = points.map { scalePoint(it, c, scale) }.toMutableList()
    }
}

data class SegmentShape(var p1: Point, var p2: Point, override var color: Color = Color.LIGHT_GRAY, override var isSelected: Boolean = false) : GeometryShape(color, isSelected) {
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
        val l2 = v.distanceSq(w)
        if (l2 == 0.0) return p.distance(v)
        var t = ((p.x - v.x) * (w.x - v.x) + (p.y - v.y) * (w.y - v.y)) / l2
        t = t.coerceIn(0.0, 1.0)
        val projX = v.x + t * (w.x - v.x)
        val projY = v.y + t * (w.y - v.y)
        return sqrt((p.x - projX).pow(2) + (p.y - projY).pow(2))
    }
    fun scale(scale: Double) {
        val c = Point(((p1.x + p2.x) / 2), ((p1.y + p2.y) / 2))
        p1 = scalePoint(p1, c, scale)
        p2 = scalePoint(p2, c, scale)
    }
}

data class RayShape(var start: Point, var direction: Point, override var color: Color = Color.MAGENTA, override var isSelected: Boolean = false) : GeometryShape(color, isSelected) {
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

data class LineShape(var p1: Point, var p2: Point, override var color: Color = Color.GRAY, override var isSelected: Boolean = false) : GeometryShape(color, isSelected) {
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
    fun scale(scale: Double) {
        val c = Point(((p1.x + p2.x) / 2), ((p1.y + p2.y) / 2))
        p1 = scalePoint(p1, c, scale)
        p2 = scalePoint(p2, c, scale)
    }
}


data class AngleShape(val vertex: Point, val ray1: Point, val ray2: Point, val arcRadius: Double, override var color: Color = Color.YELLOW, override var isSelected: Boolean = false) : GeometryShape(color, isSelected) {
    override fun draw(g: Graphics2D) {
        g.color = color

        g.drawLine(vertex.x, vertex.y, ray1.x, ray1.y)
        g.drawLine(vertex.x, vertex.y, ray2.x, ray2.y)

        val startAngle = Math.toDegrees(atan2((ray1.y - vertex.y).toDouble(), (ray1.x - vertex.x).toDouble()))
        val endAngle = Math.toDegrees(atan2((ray2.y - vertex.y).toDouble(), (ray2.x - vertex.x).toDouble()))
        var sweepAngle = endAngle - startAngle
        if (sweepAngle < 0) sweepAngle += 360.0
        g.drawArc((vertex.x - arcRadius).toInt(), (vertex.y - arcRadius).toInt(), (2 * arcRadius).toInt(), (2 * arcRadius).toInt(), startAngle.toInt(), sweepAngle.toInt())
    }
    override fun area() = 0.0
    override fun perimeter() = 0.0
    override fun contains(point: Point) = false
}


fun rotatePoint(p: Point, center: Point, angleDegrees: Double): Point {
    val angleRadians = Math.toRadians(angleDegrees)
    val sin = sin(angleRadians)
    val cos = cos(angleRadians)
    val dx = p.x - center.x
    val dy = p.y - center.y
    return Point(center.x + (dx * cos - dy * sin).toInt(), center.y + (dx * sin + dy * cos).toInt())
}

fun scalePoint(p: Point, center: Point, scale: Double): Point {
    val dx = p.x - center.x
    val dy = p.y - center.y
    return Point((center.x + dx * scale).toInt(), (center.y + dy * scale).toInt())
}

fun reflectPoint(p: Point, lineP1: Point, lineP2: Point): Point {
    val dx = (lineP2.x - lineP1.x).toDouble()
    val dy = (lineP2.y - lineP1.y).toDouble()
    val t = ((p.x - lineP1.x) * dx + (p.y - lineP1.y) * dy) / (dx * dx + dy * dy)
    val projX = lineP1.x + t * dx
    val projY = lineP1.y + t * dy
    val rx = 2 * projX - p.x
    val ry = 2 * projY - p.y
    return Point(rx.toInt(), ry.toInt())
}

enum class Tool {
    SELECT, ADD_CIRCLE, ADD_RECTANGLE, ADD_TRIANGLE, ADD_POLYGON, ADD_SEGMENT, ADD_RAY, ADD_LINE,
    ADD_BISECTOR, ADD_PARALLEL, ADD_PERPENDICULAR, ADD_ANGLE, ADD_TWO_POINT_CIRCLE, ADD_REGULAR_POLYGON,
    REFLECT, ROTATE_AROUND_POINT, CENTRAL_DILATION, ADD_COMPASS
}

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
                            is Circle -> dragOffset = Point(e.x - shape.center.x, e.y - shape.center.y)
                            is RectangleShape -> dragOffset = Point(e.x - shape.topLeft.x, e.y - shape.topLeft.y)
                            is TriangleShape -> dragOffset = Point(e.x - shape.p1.x, e.y - shape.p1.y)
                            is PolygonShape -> dragOffset = Point(e.x - shape.points.first().x, e.y - shape.points.first().y)
                            is SegmentShape -> dragOffset = Point(e.x - shape.p1.x, e.y - shape.p1.y)
                            is RayShape -> dragOffset = Point(e.x - shape.start.x, e.y - shape.start.y)
                            is LineShape -> dragOffset = Point(e.x - shape.p1.x, e.y - shape.p1.y)
                            is AngleShape -> dragOffset = Point(e.x - shape.vertex.x, e.y - shape.vertex.y)
                        }
                    }
                    repaint()
                } else {
                    tempPoints.add(e.point)
                    when (currentTool) {
                        Tool.ADD_CIRCLE, Tool.ADD_RECTANGLE, Tool.ADD_SEGMENT, Tool.ADD_RAY, Tool.ADD_LINE -> if (tempPoints.size == 2) finishDrawing()
                        Tool.ADD_TRIANGLE -> if (tempPoints.size == 3) finishDrawing()
                        Tool.ADD_POLYGON -> if (e.clickCount == 2 && tempPoints.size > 2) finishDrawing()
                        Tool.ADD_BISECTOR -> if (tempPoints.size == 2) finishDrawing()
                        Tool.ADD_PARALLEL, Tool.ADD_PERPENDICULAR -> if (tempPoints.size == 3) finishDrawing()
                        Tool.ADD_ANGLE -> if (tempPoints.size == 2) finishDrawing()
                        Tool.ADD_TWO_POINT_CIRCLE -> if (tempPoints.size == 2) finishDrawing()
                        Tool.ADD_REGULAR_POLYGON -> if (tempPoints.size == 2) finishDrawing()
                        Tool.REFLECT -> if (tempPoints.size == 2) finishDrawing()
                        Tool.ROTATE_AROUND_POINT -> if (tempPoints.size == 1) finishDrawing()
                        Tool.CENTRAL_DILATION -> if (tempPoints.size == 1) finishDrawing()
                        Tool.ADD_COMPASS -> if (tempPoints.size == 3) finishDrawing()
                        else -> {}
                    }
                }
            }
            override fun mouseReleased(e: MouseEvent) { dragOffset = Point(0, 0) }
        })
        addMouseMotionListener(object : MouseAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                if (currentTool == Tool.SELECT) {
                    selectedShape?.let { shape ->
                        when (shape) {
                            is Circle -> shape.center = Point(e.x - dragOffset.x, e.y - dragOffset.y)
                            is RectangleShape -> shape.topLeft = Point(e.x - dragOffset.x, e.y - dragOffset.y)
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
                                for (i in shape.points.indices) shape.points[i] = Point(shape.points[i].x + dx, shape.points[i].y + dy)
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
                            is AngleShape -> {
                                // Forgatás vagy eltolás esetén nem kezeljük külön
                            }
                        }
                    }
                    repaint()
                } else {
                    currentMousePos = e.point
                    repaint()
                }
            }
            override fun mouseMoved(e: MouseEvent) { if (currentTool != Tool.SELECT) { currentMousePos = e.point; repaint() } }
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
        for (x in 0 until width step gridSize) g2.drawLine(x, 0, x, height)
        for (y in 0 until height step gridSize) g2.drawLine(0, y, width, y)
    }
    private fun drawTempShape(g2: Graphics2D) {
        if (tempPoints.isEmpty()) return
        g2.color = Color.YELLOW
        tempPoints.forEach { g2.fillOval(it.x - 3, it.y - 3, 6, 6) }
        val previewPoints = mutableListOf<Point>().apply {
            addAll(tempPoints)
            currentMousePos?.let { add(it) }
        }
        when (currentTool) {
            Tool.ADD_CIRCLE, Tool.ADD_RECTANGLE, Tool.ADD_SEGMENT, Tool.ADD_RAY, Tool.ADD_LINE -> {
                if (previewPoints.size >= 2)
                    g2.drawLine(previewPoints.first().x, previewPoints.first().y, previewPoints.last().x, previewPoints.last().y)
            }
            Tool.ADD_TRIANGLE, Tool.ADD_POLYGON -> {
                if (previewPoints.size >= 2)
                    for (i in 0 until previewPoints.size - 1)
                        g2.drawLine(previewPoints[i].x, previewPoints[i].y, previewPoints[i + 1].x, previewPoints[i + 1].y)
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
            Tool.ADD_TRIANGLE -> shapes.add(TriangleShape(tempPoints[0], tempPoints[1], tempPoints[2]))
            Tool.ADD_POLYGON -> shapes.add(PolygonShape(tempPoints.toMutableList()))
            Tool.ADD_SEGMENT -> shapes.add(SegmentShape(tempPoints[0], tempPoints[1]))
            Tool.ADD_RAY -> shapes.add(RayShape(tempPoints[0], tempPoints[1]))
            Tool.ADD_LINE -> shapes.add(LineShape(tempPoints[0], tempPoints[1]))
            Tool.ADD_BISECTOR -> {
                val p1 = tempPoints[0]
                val p2 = tempPoints[1]
                val mid = Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2)
                val dx = p2.x - p1.x
                val dy = p2.y - p1.y
                val perpPoint = Point(mid.x - dy, mid.y + dx)
                shapes.add(LineShape(mid, perpPoint, color = Color.PINK))
            }
            Tool.ADD_PARALLEL -> {
                val baseP1 = tempPoints[0]
                val baseP2 = tempPoints[1]
                val through = tempPoints[2]
                val v = Point(baseP2.x - baseP1.x, baseP2.y - baseP1.y)
                val p2_new = Point(through.x + v.x, through.y + v.y)
                shapes.add(LineShape(through, p2_new, color = Color.ORANGE))
            }
            Tool.ADD_PERPENDICULAR -> {
                val baseP1 = tempPoints[0]
                val baseP2 = tempPoints[1]
                val through = tempPoints[2]
                val v = Point(baseP2.x - baseP1.x, baseP2.y - baseP1.y)
                val perp = Point(through.x - v.y, through.y + v.x)
                shapes.add(LineShape(through, perp, color = Color.ORANGE))
            }
            Tool.ADD_ANGLE -> {
                val vertex = tempPoints[0]
                val ray1 = tempPoints[1]
                val angle = JOptionPane.showInputDialog(this, "Adja meg a szöget fokban:")?.toDoubleOrNull() ?: run {
                    JOptionPane.showMessageDialog(this, "Érvénytelen szög.")
                    tempPoints.clear()
                    return
                }
                val radius = JOptionPane.showInputDialog(this, "Adja meg az ív sugárát:")?.toDoubleOrNull() ?: run {
                    JOptionPane.showMessageDialog(this, "Érvénytelen sugár.")
                    tempPoints.clear()
                    return
                }
                val ray2 = rotatePoint(ray1, vertex, angle)
                shapes.add(AngleShape(vertex, ray1, ray2, radius))
            }
            Tool.ADD_TWO_POINT_CIRCLE -> {
                val center = Point((tempPoints[0].x + tempPoints[1].x) / 2, (tempPoints[0].y + tempPoints[1].y) / 2)
                val radius = tempPoints[0].distance(tempPoints[1]) / 2.0
                shapes.add(Circle(center, radius, color = Color.ORANGE))
            }
            Tool.ADD_REGULAR_POLYGON -> {
                val center = tempPoints[0]
                val vertex = tempPoints[1]
                val sides = JOptionPane.showInputDialog(this, "Adja meg a sokszög oldalainak számát:")?.toIntOrNull() ?: run {
                    JOptionPane.showMessageDialog(this, "Érvénytelen szám.")
                    tempPoints.clear()
                    return
                }
                val dx = vertex.x - center.x
                val dy = vertex.y - center.y
                val radius = sqrt((dx * dx + dy * dy).toDouble())
                val startAngle = atan2(dy.toDouble(), dx.toDouble())
                val points = mutableListOf<Point>()
                for (i in 0 until sides) {
                    val angle = startAngle + 2 * Math.PI * i / sides
                    val x = center.x + (radius * cos(angle)).toInt()
                    val y = center.y + (radius * sin(angle)).toInt()
                    points.add(Point(x, y))
                }
                shapes.add(PolygonShape(points, color = Color.MAGENTA))
            }
            Tool.REFLECT -> {
                if (selectedShape == null) {
                    JOptionPane.showMessageDialog(this, "Nincs kiválasztott alakzat.")
                    tempPoints.clear()
                    return
                }
                val lineP1 = tempPoints[0]
                val lineP2 = tempPoints[1]
                when (val s = selectedShape) {
                    is Circle -> s.center = reflectPoint(s.center, lineP1, lineP2)
                    is RectangleShape -> s.topLeft = reflectPoint(s.topLeft, lineP1, lineP2)
                    is TriangleShape -> {
                        s.p1 = reflectPoint(s.p1, lineP1, lineP2)
                        s.p2 = reflectPoint(s.p2, lineP1, lineP2)
                        s.p3 = reflectPoint(s.p3, lineP1, lineP2)
                    }
                    is PolygonShape -> s.points = s.points.map { reflectPoint(it, lineP1, lineP2) }.toMutableList()
                    is SegmentShape -> {
                        s.p1 = reflectPoint(s.p1, lineP1, lineP2)
                        s.p2 = reflectPoint(s.p2, lineP1, lineP2)
                    }
                    is RayShape -> {
                        s.start = reflectPoint(s.start, lineP1, lineP2)
                        s.direction = reflectPoint(s.direction, lineP1, lineP2)
                    }
                    is LineShape -> {
                        s.p1 = reflectPoint(s.p1, lineP1, lineP2)
                        s.p2 = reflectPoint(s.p2, lineP1, lineP2)
                    }
                    is AngleShape -> {

                    } else -> {}
                }
            }
            Tool.ROTATE_AROUND_POINT -> {
                if (selectedShape == null) {
                    JOptionPane.showMessageDialog(this, "Nincs kiválasztott alakzat.")
                    tempPoints.clear()
                    return
                }
                val pivot = tempPoints[0]
                val angle = JOptionPane.showInputDialog(this, "Adja meg a forgatási szöget fokban:")?.toDoubleOrNull() ?: run {
                    JOptionPane.showMessageDialog(this, "Érvénytelen szög.")
                    tempPoints.clear()
                    return
                }
                when (val s = selectedShape) {
                    is Circle -> s.center = rotatePoint(s.center, pivot, angle)
                    is RectangleShape -> s.topLeft = rotatePoint(s.topLeft, pivot, angle)
                    is TriangleShape -> {
                        s.p1 = rotatePoint(s.p1, pivot, angle)
                        s.p2 = rotatePoint(s.p2, pivot, angle)
                        s.p3 = rotatePoint(s.p3, pivot, angle)
                    }
                    is PolygonShape -> s.points = s.points.map { rotatePoint(it, pivot, angle) }.toMutableList()
                    is SegmentShape -> {
                        s.p1 = rotatePoint(s.p1, pivot, angle)
                        s.p2 = rotatePoint(s.p2, pivot, angle)
                    }
                    is RayShape -> {
                        s.start = rotatePoint(s.start, pivot, angle)
                        s.direction = rotatePoint(s.direction, pivot, angle)
                    }
                    is LineShape -> {
                        s.p1 = rotatePoint(s.p1, pivot, angle)
                        s.p2 = rotatePoint(s.p2, pivot, angle)
                    }
                    is AngleShape -> {

                    } else -> {}
                }
            }
            Tool.CENTRAL_DILATION -> {
                if (selectedShape == null) {
                    JOptionPane.showMessageDialog(this, "Nincs kiválasztott alakzat.")
                    tempPoints.clear()
                    return
                }
                val center = tempPoints[0]
                val factor = JOptionPane.showInputDialog(this, "Adja meg a nyújtási faktort:")?.toDoubleOrNull() ?: run {
                    JOptionPane.showMessageDialog(this, "Érvénytelen faktor.")
                    tempPoints.clear()
                    return
                }
                when (val s = selectedShape) {
                    is Circle -> {
                        s.center = scalePoint(s.center, center, factor)
                        s.radius *= factor
                    }
                    is RectangleShape -> s.topLeft = scalePoint(s.topLeft, center, factor)
                    is TriangleShape -> {
                        s.p1 = scalePoint(s.p1, center, factor)
                        s.p2 = scalePoint(s.p2, center, factor)
                        s.p3 = scalePoint(s.p3, center, factor)
                    }
                    is PolygonShape -> s.points = s.points.map { scalePoint(it, center, factor) }.toMutableList()
                    is SegmentShape -> {
                        s.p1 = scalePoint(s.p1, center, factor)
                        s.p2 = scalePoint(s.p2, center, factor)
                    }
                    is RayShape -> {
                        s.start = scalePoint(s.start, center, factor)
                        s.direction = scalePoint(s.direction, center, factor)
                    }
                    is LineShape -> {
                        s.p1 = scalePoint(s.p1, center, factor)
                        s.p2 = scalePoint(s.p2, center, factor)
                    }
                    is AngleShape -> {

                    } else -> {}
                }
            }
            Tool.ADD_COMPASS -> {

                val d = tempPoints[0].distance(tempPoints[1])
                val center = tempPoints[2]
                shapes.add(Circle(center, d, color = Color.ORANGE))
            }
            else -> {}
        }
        tempPoints.clear()
        currentMousePos = null
        repaint()
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

        val btnSelect = JButton("Kiválasztás").apply { addActionListener { geometryPanel.currentTool = Tool.SELECT; geometryPanel.tempPoints.clear() } }
        val btnCircle = JButton(ImageIcon(javaClass.getResource("/icons/circle.png"))).apply { addActionListener { geometryPanel.currentTool = Tool.ADD_CIRCLE; geometryPanel.tempPoints.clear() } }
        val btnRectangle = JButton(ImageIcon(javaClass.getResource("/icons/rectangle.png"))).apply { addActionListener { geometryPanel.currentTool = Tool.ADD_RECTANGLE; geometryPanel.tempPoints.clear() } }
        val btnTriangle = JButton(ImageIcon(javaClass.getResource("/icons/triangle.png"))).apply { addActionListener { geometryPanel.currentTool = Tool.ADD_TRIANGLE; geometryPanel.tempPoints.clear() } }
        val btnPolygon = JButton(ImageIcon(javaClass.getResource("/icons/polygon.png"))).apply { addActionListener { geometryPanel.currentTool = Tool.ADD_POLYGON; geometryPanel.tempPoints.clear() } }
        val btnSegment = JButton("Szakasz").apply { addActionListener { geometryPanel.currentTool = Tool.ADD_SEGMENT; geometryPanel.tempPoints.clear() } }
        val btnRay = JButton("Félegyenes").apply { addActionListener { geometryPanel.currentTool = Tool.ADD_RAY; geometryPanel.tempPoints.clear() } }
        val btnLine = JButton("Vonal").apply { addActionListener { geometryPanel.currentTool = Tool.ADD_LINE; geometryPanel.tempPoints.clear() } }
        val btnRotate = JButton("Forgatás").apply { addActionListener {
            val s = geometryPanel.selectedShape ?: run { JOptionPane.showMessageDialog(this, "Nincs kiválasztott alakzat."); return@addActionListener }
            val angle = JOptionPane.showInputDialog(this, "Adja meg a forgatási szöget fokban:")?.toDoubleOrNull() ?: run { JOptionPane.showMessageDialog(this, "Érvénytelen szög."); return@addActionListener }
            when (s) {
                is TriangleShape -> { val c = s.centroid(); s.p1 = rotatePoint(s.p1, c, angle); s.p2 = rotatePoint(s.p2, c, angle); s.p3 = rotatePoint(s.p3, c, angle) }
                is PolygonShape -> { val c = s.centroid(); s.points = s.points.map { rotatePoint(it, c, angle) }.toMutableList() }
                is SegmentShape -> { val c = Point(((s.p1.x + s.p2.x) / 2), ((s.p1.y + s.p2.y) / 2)); s.p1 = rotatePoint(s.p1, c, angle); s.p2 = rotatePoint(s.p2, c, angle) }
                is RayShape -> { val c = s.start; s.direction = rotatePoint(s.direction, c, angle) }
                is LineShape -> { val c = Point(((s.p1.x + s.p2.x) / 2), ((s.p1.y + s.p2.y) / 2)); s.p1 = rotatePoint(s.p1, c, angle); s.p2 = rotatePoint(s.p2, c, angle) }
                is Circle -> { /* Forgatás nem szükséges */ }
                is RectangleShape -> { /* Forgatás nem támogatott */ }
                is AngleShape -> { /* Megoldható a vertex körüli forgatással */ }
            }
            geometryPanel.repaint()
        } }
        val btnScale = JButton("Skálázás").apply { addActionListener {
            val s = geometryPanel.selectedShape ?: run { JOptionPane.showMessageDialog(this, "Nincs kiválasztott alakzat."); return@addActionListener }
            val factor = JOptionPane.showInputDialog(this, "Adja meg a skálázási faktort:")?.toDoubleOrNull() ?: run { JOptionPane.showMessageDialog(this, "Érvénytelen faktor."); return@addActionListener }
            when (s) {
                is Circle -> s.radius *= factor
                is RectangleShape -> s.scale(factor)
                is TriangleShape -> s.scale(factor)
                is PolygonShape -> s.scale(factor)
                is SegmentShape -> s.scale(factor)
                is LineShape -> s.scale(factor)
                is RayShape -> {}
                else -> {}
            }

            geometryPanel.repaint()
        } }
        val btnColor = JButton("Szín").apply { addActionListener {
            val s = geometryPanel.selectedShape ?: run { JOptionPane.showMessageDialog(this, "Nincs kiválasztott alakzat."); return@addActionListener }
            val newColor = JColorChooser.showDialog(this, "Válasszon színt", s.color)
            if (newColor != null) { s.color = newColor; geometryPanel.repaint() }
        } }
        val btnDelete = JButton("Törlés").apply { addActionListener {
            val s = geometryPanel.selectedShape ?: run { JOptionPane.showMessageDialog(this, "Nincs kiválasztott alakzat."); return@addActionListener }
            geometryPanel.shapes.remove(s)
            geometryPanel.selectedShape = null
            geometryPanel.repaint()
        } }
        val btnClear = JButton("Ürítés").apply { addActionListener {
            geometryPanel.shapes.clear()
            geometryPanel.selectedShape = null
            geometryPanel.repaint()
        } }
        
        val btnBisector = JButton("Szakaszfelező").apply { addActionListener { geometryPanel.currentTool = Tool.ADD_BISECTOR; geometryPanel.tempPoints.clear() } }
        val btnParallel = JButton("Párhuzamos").apply { addActionListener { geometryPanel.currentTool = Tool.ADD_PARALLEL; geometryPanel.tempPoints.clear() } }
        val btnPerpendicular = JButton("Merőleges").apply { addActionListener { geometryPanel.currentTool = Tool.ADD_PERPENDICULAR; geometryPanel.tempPoints.clear() } }
        val btnAngle = JButton("Szög").apply { addActionListener { geometryPanel.currentTool = Tool.ADD_ANGLE; geometryPanel.tempPoints.clear() } }
        val btnTwoPointCircle = JButton("2P-kör").apply { addActionListener { geometryPanel.currentTool = Tool.ADD_TWO_POINT_CIRCLE; geometryPanel.tempPoints.clear() } }
        val btnRegularPolygon = JButton("Szab. sokszög").apply { addActionListener { geometryPanel.currentTool = Tool.ADD_REGULAR_POLYGON; geometryPanel.tempPoints.clear() } }
        val btnReflect = JButton("Tükrözés").apply { addActionListener { geometryPanel.currentTool = Tool.REFLECT; geometryPanel.tempPoints.clear() } }
        val btnRotateAround = JButton("Pont körüli forgatás").apply { addActionListener { geometryPanel.currentTool = Tool.ROTATE_AROUND_POINT; geometryPanel.tempPoints.clear() } }
        val btnCentralDilation = JButton("Centrális nyújtás").apply { addActionListener { geometryPanel.currentTool = Tool.CENTRAL_DILATION; geometryPanel.tempPoints.clear() } }
        val btnCompass = JButton("Körző").apply { addActionListener { geometryPanel.currentTool = Tool.ADD_COMPASS; geometryPanel.tempPoints.clear() } }


        toolBar.add(btnSelect)
        toolBar.add(btnCircle)
        toolBar.add(btnRectangle)
        toolBar.add(btnTriangle)
        toolBar.add(btnPolygon)
        toolBar.add(btnSegment)
        toolBar.add(btnRay)
        toolBar.add(btnLine)
        toolBar.add(btnRotate)
        toolBar.add(btnScale)
        toolBar.add(btnColor)
        toolBar.add(btnDelete)
        toolBar.add(btnClear)
        toolBar.addSeparator()
        toolBar.add(btnBisector)
        toolBar.add(btnParallel)
        toolBar.add(btnPerpendicular)
        toolBar.add(btnAngle)
        toolBar.add(btnTwoPointCircle)
        toolBar.add(btnRegularPolygon)
        toolBar.add(btnReflect)
        toolBar.add(btnRotateAround)
        toolBar.add(btnCentralDilation)
        toolBar.add(btnCompass)

        add(toolBar, BorderLayout.NORTH)
        Timer(500) {
            val s = geometryPanel.selectedShape
            propertiesText.text = if (s != null) {
                when (s) {
                    is Circle -> "Kör\nKözéppont: (${s.center.x},${s.center.y})\nSugár: ${"%.2f".format(s.radius)}\nTerület: ${"%.2f".format(s.area())}\nKerület: ${"%.2f".format(s.perimeter())}"
                    is RectangleShape -> "Téglalap\nBal felső: (${s.topLeft.x},${s.topLeft.y})\nSzélesség: ${"%.2f".format(s.width)}\nMagasság: ${"%.2f".format(s.height)}\nTerület: ${"%.2f".format(s.area())}\nKerület: ${"%.2f".format(s.perimeter())}"
                    is TriangleShape -> "Háromszög\nP1: (${s.p1.x},${s.p1.y})\nP2: (${s.p2.x},${s.p2.y})\nP3: (${s.p3.x},${s.p3.y})\nTerület: ${"%.2f".format(s.area())}\nKerület: ${"%.2f".format(s.perimeter())}"
                    is PolygonShape -> "Sokszög\nPontok: ${s.points.joinToString { "(${it.x},${it.y})" }}\nTerület: ${"%.2f".format(s.area())}\nKerület: ${"%.2f".format(s.perimeter())}"
                    is SegmentShape -> "Szakasz\nP1: (${s.p1.x},${s.p1.y})\nP2: (${s.p2.x},${s.p2.y})\nHossz: ${"%.2f".format(s.perimeter())}"
                    is RayShape -> "Félegyenes\nKezdő: (${s.start.x},${s.start.y})\nIrány: (${s.direction.x - s.start.x},${s.direction.y - s.start.y})"
                    is LineShape -> "Vonal\nP1: (${s.p1.x},${s.p1.y})\nP2: (${s.p2.x},${s.p2.y})"
                    is AngleShape -> "Szög\nVertex: (${s.vertex.x},${s.vertex.y})\nSugar: ${"%.2f".format(s.arcRadius)}"
                    else -> "Ismeretlen alakzat"
                }
            } else "Nincs kiválasztott alakzat."
        }.start()
        setSize(1200, 800)
        setLocationRelativeTo(null)
    }
    private fun exportPanel() {
        val fileChooser = JFileChooser()
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            var file = fileChooser.selectedFile
            if (!file.name.endsWith(".png")) file = File(file.parentFile, file.name + ".png")
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
        try { UIManager.setLookAndFeel(FlatDarkLaf()) } catch (ex: Exception) { ex.printStackTrace() }
        ComplexGeometryFrame().isVisible = true
    }
}
