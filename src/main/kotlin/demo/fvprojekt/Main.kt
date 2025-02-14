@file:Suppress("SameParameterValue", "MemberVisibilityCanBePrivate")

package demo.fvprojekt.demo.fvprojekt

import net.objecthunter.exp4j.Expression
import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.function.Function
import net.objecthunter.exp4j.operator.Operator
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.*
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min

// ==========================================
// Adatstruktúrák
// ==========================================

/**
 * Globális beállítások a grafikus megjelenítéshez.
 * Az invertColors és leftPanelWidth tulajdonságok mostantól a beállításokban módosíthatóak.
 */
data class GraphSettings(
    var backgroundColor: Color = Color(30, 30, 30),
    var gridColor: Color = Color(60, 60, 60),
    var axisColor: Color = Color.WHITE,
    var showGrid: Boolean = true,
    var showAxis: Boolean = true,
    var showDomainArea: Boolean = true,
    var showZeroPoints: Boolean = true,
    var showIntersections: Boolean = true,
    var pointSize: Int = 8,
    var domainAlpha: Float = 0.1f,
    var stepForNumericalSearch: Double = 0.01,
    var invertColors: Boolean = false,
    var leftPanelWidth: Int = 380
)

/**
 * Adatmodell egy függvényhez: kifejezés, domain, megjelenítési beállítások és az exp4j Expression.
 */
data class FunctionData(
    var expressionText: String = "x",
    var domainStart: Double = -10.0,
    var domainEnd: Double = 10.0,
    var lineColor: Color = Color.ORANGE,
    var lineStroke: Float = 2f,
    var visible: Boolean = true,
    var showDomain: Boolean = true,
    var expression: Expression? = null
)

// ==========================================
// Függvény bevitel panel
// ==========================================

/**
 * Panel, ahol egy függvény adatai szerkeszthetők.
 */
class FunctionInputPanel(
    val onRemove: (panel: FunctionInputPanel) -> Unit
) : JPanel() {

    val txtExpression = JTextField("f(x) = x", 18)
    val txtDomainStart = JTextField("-10", 5)
    val txtDomainEnd = JTextField("10", 5)
    val btnColor = JButton("Szín")
    val spnLineWidth = JSpinner(SpinnerNumberModel(2.0, 0.5, 20.0, 0.5))
    val chkVisible = JCheckBox("Látható", true)
    val chkDomain = JCheckBox("Domain", true)
    val btnRemove = JButton("✕")

    init {
        layout = GridBagLayout()
        background = Color(45, 45, 45)
        val gbc = GridBagConstraints().apply {
            insets = Insets(3, 3, 3, 3)
            fill = GridBagConstraints.HORIZONTAL
        }

        // 1. Sor: Expression
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1
        add(JLabel("f(x) =").apply { foreground = Color.WHITE }, gbc)
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 4
        txtExpression.background = Color(60, 60, 60)
        txtExpression.foreground = Color.WHITE
        add(txtExpression, gbc)

        // 2. Sor: Domain
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1
        add(JLabel("Domain: [").apply { foreground = Color.WHITE }, gbc)
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 1
        txtDomainStart.background = Color(60, 60, 60)
        txtDomainStart.foreground = Color.WHITE
        add(txtDomainStart, gbc)
        gbc.gridx = 2
        add(JLabel(";").apply { foreground = Color.WHITE }, gbc)
        gbc.gridx = 3
        txtDomainEnd.background = Color(60, 60, 60)
        txtDomainEnd.foreground = Color.WHITE
        add(txtDomainEnd, gbc)
        gbc.gridx = 4
        add(JLabel("]").apply { foreground = Color.WHITE }, gbc)

        // 3. Sor: Szín és vonalvastagság
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1
        btnColor.background = Color.ORANGE
        btnColor.foreground = Color.BLACK
        add(btnColor, gbc)
        gbc.gridx = 1
        add(JLabel("Vonal:").apply { foreground = Color.WHITE }, gbc)
        gbc.gridx = 2
        spnLineWidth.preferredSize = Dimension(60, 22)
        add(spnLineWidth, gbc)

        // 4. Sor: CheckBox-ok és törlés gomb
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1
        chkVisible.background = background
        chkVisible.foreground = Color.WHITE
        add(chkVisible, gbc)
        gbc.gridx = 1
        chkDomain.background = background
        chkDomain.foreground = Color.WHITE
        add(chkDomain, gbc)
        gbc.gridx = 4
        btnRemove.background = Color(80, 50, 50)
        btnRemove.foreground = Color.WHITE
        btnRemove.toolTipText = "Függvény törlése"
        add(btnRemove, gbc)

        // Színválasztó
        btnColor.addActionListener {
            val newCol = JColorChooser.showDialog(this, "Válassz színt", btnColor.background)
            if (newCol != null) btnColor.background = newCol
        }
        // Eltávolítás
        btnRemove.addActionListener { onRemove(this) }
    }

    fun toFunctionData(): FunctionData {
        var expr = txtExpression.text.trim().lowercase().replace(" ", "")
        if (expr.startsWith("f(x)=")) expr = expr.substring(5)
        val domainS = txtDomainStart.text.toDoubleOrNull() ?: -10.0
        val domainE = txtDomainEnd.text.toDoubleOrNull() ?: 10.0
        val lw = (spnLineWidth.value as? Double)?.toFloat() ?: 2f
        return FunctionData(
            expressionText = expr,
            domainStart = min(domainS, domainE),
            domainEnd = max(domainS, domainE),
            lineColor = btnColor.background,
            lineStroke = lw,
            visible = chkVisible.isSelected,
            showDomain = chkDomain.isSelected
        )
    }
}

// ==========================================
// Grafikus panel
// ==========================================

/**
 * A grafikus panel, amely kirajzolja a rácsot, tengelyeket, függvényeket, domain sávot, zérushelyeket, metszéspontokat.
 * Támogatja a panningot, zoomolást (görgővel és gombbal) és a nézet visszaállítását.
 */
class GraphPanel : JPanel() {
    var functionList: List<FunctionData> = emptyList()
    var graphSettings: GraphSettings = GraphSettings()
    var zeroPointsMap: Map<FunctionData, List<Double>> = emptyMap()
    var intersectionPoints: List<Pair<Double, Double>> = emptyList()
    var scale = 40.0

    // Panning offsetok
    var offsetX = 0
    var offsetY = 0
    private var lastDragX = 0
    private var lastDragY = 0

    init {
        // Panning egérrel
        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                lastDragX = e.x
                lastDragY = e.y
            }
        })
        addMouseMotionListener(object : MouseAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                val dx = e.x - lastDragX
                val dy = e.y - lastDragY
                offsetX += dx
                offsetY += dy
                lastDragX = e.x
                lastDragY = e.y
                repaint()
            }
        })
        // Zoomolás egérgörgővel
        addMouseWheelListener { e: MouseWheelEvent ->
            if (e.wheelRotation < 0) zoomIn() else zoomOut()
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Háttér
        g2.color = graphSettings.backgroundColor
        g2.fillRect(0, 0, width, height)

        // Középpont panninggel
        val centerX = width / 2 + offsetX
        val centerY = height / 2 + offsetY

        // Rács
        if (graphSettings.showGrid) {
            g2.color = graphSettings.gridColor
            val stepPx = scale.toInt()
            // Függőleges vonalak
            var xLine = centerX
            while (xLine < width) {
                g2.drawLine(xLine, 0, xLine, height)
                xLine += stepPx
            }
            xLine = centerX - stepPx
            while (xLine >= 0) {
                g2.drawLine(xLine, 0, xLine, height)
                xLine -= stepPx
            }
            // Vízszintes vonalak
            var yLine = centerY
            while (yLine < height) {
                g2.drawLine(0, yLine, width, yLine)
                yLine += stepPx
            }
            yLine = centerY - stepPx
            while (yLine >= 0) {
                g2.drawLine(0, yLine, width, yLine)
                yLine -= stepPx
            }
        }

        // Tengelyek
        if (graphSettings.showAxis) {
            g2.color = graphSettings.axisColor
            g2.drawLine(0, centerY, width, centerY)
            g2.drawLine(centerX, 0, centerX, height)
        }

        // Domain sáv
        if (graphSettings.showDomainArea) {
            functionList.forEach { fd ->
                if (fd.visible && fd.showDomain) {
                    val x1 = centerX + (fd.domainStart * scale).toInt()
                    val x2 = centerX + (fd.domainEnd * scale).toInt()
                    val rectX = min(x1, x2)
                    val rectW = abs(x2 - x1)
                    val c = fd.lineColor
                    val domainColor = Color(c.red, c.green, c.blue, (graphSettings.domainAlpha * 255).toInt())
                    g2.color = domainColor
                    g2.fillRect(rectX, 0, rectW, height)
                    g2.color = c
                    g2.stroke = BasicStroke(1f)
                    g2.drawLine(x1, 0, x1, height)
                    g2.drawLine(x2, 0, x2, height)
                }
            }
        }

        // Függvények kirajzolása
        functionList.forEach { fd ->
            if (!fd.visible) return@forEach
            val expr = fd.expression ?: return@forEach
            drawFunction(g2, expr, fd.domainStart, fd.domainEnd, fd.lineColor, fd.lineStroke, centerX, centerY)
        }

        // Zérushelyek
        if (graphSettings.showZeroPoints) {
            g2.color = Color.MAGENTA
            zeroPointsMap.forEach { (fd, zlist) ->
                if (!fd.visible) return@forEach
                for (z in zlist) {
                    val px = (centerX + z * scale).toInt()
                    val py = centerY
                    g2.fillOval(
                        px - graphSettings.pointSize / 2,
                        py - graphSettings.pointSize / 2,
                        graphSettings.pointSize,
                        graphSettings.pointSize
                    )
                }
            }
        }

        // Metszéspontok
        if (graphSettings.showIntersections) {
            g2.color = Color.YELLOW
            intersectionPoints.forEach { (mx, my) ->
                val px = centerX + (mx * scale).toInt()
                val py = centerY - (my * scale).toInt()
                g2.fillOval(
                    px - graphSettings.pointSize / 2,
                    py - graphSettings.pointSize / 2,
                    graphSettings.pointSize,
                    graphSettings.pointSize
                )
            }
        }
    }

    /**
     * Függvény kirajzolása adott tartományon, a megadott középponttal.
     */
    private fun drawFunction(
        g2: Graphics2D,
        expr: Expression,
        start: Double,
        end: Double,
        col: Color,
        strokeWidth: Float,
        centerX: Int,
        centerY: Int
    ) {
        val steps = 500
        val dx = (end - start) / steps
        val poly = mutableListOf<Point>()
        var xVal = start
        for (i in 0..steps) {
            val yVal = expr.setVariable("x", xVal).evaluate()
            val px = centerX + (xVal * scale).toInt()
            val py = centerY - (yVal * scale).toInt()
            poly.add(Point(px, py))
            xVal += dx
        }
        g2.color = col
        g2.stroke = BasicStroke(strokeWidth)
        for (i in 0 until poly.size - 1) {
            val p1 = poly[i]
            val p2 = poly[i + 1]
            g2.drawLine(p1.x, p1.y, p2.x, p2.y)
        }
    }

    // Zoom gombokhoz
    fun zoomIn() {
        scale *= 1.2
        repaint()
    }

    fun zoomOut() {
        scale /= 1.2
        repaint()
    }

    /**
     * Nézet visszaállítása: alapértelmezett scale és panning offset.
     */
    fun resetView() {
        scale = 40.0
        offsetX = 0
        offsetY = 0
        repaint()
    }
}

// ==========================================
// Beállítások ablak
// ==========================================

/**
 * A beállításokat módosító ablak.
 * Az itt elérhető beállítások között szerepel most az invert színséma és a bal panel szélességének módosítása is.
 */
class SettingsDialog(
    owner: Frame?,
    private val settings: GraphSettings,
    private val onSettingsChanged: () -> Unit
) : JDialog(owner, "Beállítások", true) {

    private val cmbTheme = JComboBox(arrayOf("FlatLaf Dark", "FlatLaf Light", "FlatLaf IntelliJ", "System"))
    private val chkGrid = JCheckBox("Rács megjelenítése", settings.showGrid)
    private val chkAxis = JCheckBox("Tengelyek megjelenítése", settings.showAxis)
    private val chkDomain = JCheckBox("Domain-sáv megjelenítése", settings.showDomainArea)
    private val chkZeros = JCheckBox("Zérushelyek megjelenítése", settings.showZeroPoints)
    private val chkIntersections = JCheckBox("Metszéspontok megjelenítése", settings.showIntersections)
    private val spnStep = JSpinner(SpinnerNumberModel(settings.stepForNumericalSearch, 1e-5, 1.0, 0.001))
    private val spnPointSize = JSpinner(SpinnerNumberModel(settings.pointSize, 1, 50, 1))
    private val btnBgColor = JButton("Háttér színe").apply {
        background = settings.backgroundColor
        foreground = Color.WHITE
    }
    // Új beállítások: invert színséma és bal panel szélessége
    private val chkInvert = JCheckBox("Invert színséma", settings.invertColors)
    private val spnLeftPanelWidth = JSpinner(SpinnerNumberModel(settings.leftPanelWidth, 200, 800, 10))

    init {
        layout = BorderLayout()
        val panel = JPanel(GridBagLayout())
        panel.background = Color(50, 50, 50)
        val gbc = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(5, 5, 5, 5)
        }

        // Téma
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1
        panel.add(JLabel("Téma:").apply { foreground = Color.WHITE }, gbc)
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 1
        cmbTheme.selectedItem = "FlatLaf Dark"
        panel.add(cmbTheme, gbc)

        // Rács
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2
        chkGrid.background = panel.background
        chkGrid.foreground = Color.WHITE
        panel.add(chkGrid, gbc)

        // Tengely
        gbc.gridy = 2
        chkAxis.background = panel.background
        chkAxis.foreground = Color.WHITE
        panel.add(chkAxis, gbc)

        // Domain
        gbc.gridy = 3
        chkDomain.background = panel.background
        chkDomain.foreground = Color.WHITE
        panel.add(chkDomain, gbc)

        // Zérushelyek
        gbc.gridy = 4
        chkZeros.background = panel.background
        chkZeros.foreground = Color.WHITE
        panel.add(chkZeros, gbc)

        // Metszéspontok
        gbc.gridy = 5
        chkIntersections.background = panel.background
        chkIntersections.foreground = Color.WHITE
        panel.add(chkIntersections, gbc)

        // Numerikus keresés lépés
        gbc.gridy = 6; gbc.gridwidth = 1
        panel.add(JLabel("Numerikus keresés lépés:").apply { foreground = Color.WHITE }, gbc)
        gbc.gridx = 1; gbc.gridy = 6
        panel.add(spnStep, gbc)

        // Pontméret
        gbc.gridx = 0; gbc.gridy = 7
        panel.add(JLabel("Pontméret (zérus/metszés):").apply { foreground = Color.WHITE }, gbc)
        gbc.gridx = 1; gbc.gridy = 7
        panel.add(spnPointSize, gbc)

        // Háttérszín
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2
        btnBgColor.addActionListener {
            val col = JColorChooser.showDialog(this, "Háttérszín választása", btnBgColor.background)
            if (col != null) btnBgColor.background = col
        }
        panel.add(btnBgColor, gbc)

        // Új beállítások: Inverz színséma
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 1
        panel.add(JLabel("Invert színséma:").apply { foreground = Color.WHITE }, gbc)
        gbc.gridx = 1; gbc.gridy = 9
        panel.add(chkInvert, gbc)

        // Új beállítások: Bal panel szélessége
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 1
        panel.add(JLabel("Bal panel szélessége (px):").apply { foreground = Color.WHITE }, gbc)
        gbc.gridx = 1; gbc.gridy = 10
        panel.add(spnLeftPanelWidth, gbc)

        // Gombok
        val bottomPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        val btnOk = JButton("OK")
        val btnCancel = JButton("Mégse")
        bottomPanel.add(btnOk)
        bottomPanel.add(btnCancel)

        btnOk.addActionListener {
            applySettings()
            onSettingsChanged()
            dispose()
        }
        btnCancel.addActionListener { dispose() }

        add(panel, BorderLayout.CENTER)
        add(bottomPanel, BorderLayout.SOUTH)
        pack()
        setLocationRelativeTo(owner)
    }

    private fun applySettings() {
        when (cmbTheme.selectedItem) {
            "FlatLaf Dark" -> trySetLookAndFeel("com.formdev.flatlaf.FlatDarkLaf")
            "FlatLaf Light" -> trySetLookAndFeel("com.formdev.flatlaf.FlatLightLaf")
            "FlatLaf IntelliJ" -> trySetLookAndFeel("com.formdev.flatlaf.FlatIntelliJLaf")
            "System" -> trySetLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        }
        settings.showGrid = chkGrid.isSelected
        settings.showAxis = chkAxis.isSelected
        settings.showDomainArea = chkDomain.isSelected
        settings.showZeroPoints = chkZeros.isSelected
        settings.showIntersections = chkIntersections.isSelected
        settings.stepForNumericalSearch = (spnStep.value as Number).toDouble()
        settings.pointSize = (spnPointSize.value as Number).toInt()
        settings.backgroundColor = btnBgColor.background

        // Új beállítások alkalmazása:
        settings.invertColors = chkInvert.isSelected
        settings.leftPanelWidth = (spnLeftPanelWidth.value as Number).toInt()
        if (settings.invertColors) {
            settings.backgroundColor = Color.WHITE
            settings.gridColor = Color.LIGHT_GRAY
            settings.axisColor = Color.BLACK
        } else {
            settings.backgroundColor = Color(30, 30, 30)
            settings.gridColor = Color(60, 60, 60)
            settings.axisColor = Color.WHITE
        }
    }

    private fun trySetLookAndFeel(lafClass: String) {
        try { UIManager.setLookAndFeel(lafClass) } catch (_: Exception) { }
        SwingUtilities.updateComponentTreeUI(this)
    }
}

// ==========================================
// Fő alkalmazás keret
// ==========================================

/**
 * A fő ablak, melyben a bal oldali menü (függvény bevitel + magyarázat) és a grafikus panel jelenik meg.
 * A felső gombok csoportosítva vannak, és a File menüben találhatóak az exportálási módszerek.
 */
class MainFrame : JFrame("Nagy Függvényábrázoló Példa") {

    private val graphPanel = GraphPanel().apply { preferredSize = Dimension(900, 700) }
    // Függvény bevitel paneljei
    private val fvContainer = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        background = Color(40, 40, 40)
    }
    private val scrollPane = JScrollPane(
        fvContainer,
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
    ).apply { preferredSize = Dimension(380, 700) }

    // A bal oldali menü – egy JTabbedPane, mely két fület tartalmaz: "Függvények" és "Magyarázat"
    private val leftTabbedPane = JTabbedPane().apply {
        addTab("Függvények", JPanel(BorderLayout()).apply {
            add(scrollPane, BorderLayout.CENTER)
        })
        addTab("Magyarázat", JScrollPane(JTextArea(
            """
            Számítás menete:
            1. Írd be a függvény képletét, pl. f(x)= x^2 - 4.
            2. Állítsd be a domain-t (érvényes tartomány).
            3. Válaszd ki a színt és a vonalvastagságot.
            4. A "Számol & Rajzol" gombbal a függvény kirajzolódik,
               zérushelyek és metszéspontok meghatározása is megtörténik.
            5. A grafikonon panninggal, zoomolással (görgővel vagy gombokkal)
               navigálhatsz.
            6. A koordináta követő sáv mutatja az aktuális (x,y) értékeket.
            """.trimIndent()
        ).apply {
            isEditable = false
            background = Color(40, 40, 40)
            foreground = Color.WHITE
            font = Font("Monospaced", Font.PLAIN, 12)
        }))
    }
    // A bal panel szélessége a GraphSettings-ben tárolt érték
    private var leftPanelWidth = 380

    // JSplitPane: bal oldal – menü, jobb oldal – grafikus panel
    private var splitPane: JSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftTabbedPane, graphPanel).apply {
        dividerLocation = leftPanelWidth
        isOneTouchExpandable = true
    }

    // Státusz sáv a koordináták megjelenítéséhez
    private val lblCoordinates = JLabel(" ")

    // Gombok csoportosítva:
    // Függvény műveletek
    private val btnAddFunction = JButton("+").apply {
        font = Font(font.name, Font.BOLD, 20)
        background = Color(80, 80, 80)
        foreground = Color.WHITE
        toolTipText = "Új függvény hozzáadása"
    }
    private val btnClearFunctions = JButton("Függvények törlése").apply {
        background = Color(80, 80, 80)
        foreground = Color.WHITE
    }
    // Nézet vezérlés
    private val btnZoomIn = JButton("Zoom In").apply {
        background = Color(80, 80, 80)
        foreground = Color.WHITE
        toolTipText = "Nagyítás"
    }
    private val btnZoomOut = JButton("Zoom Out").apply {
        background = Color(80, 80, 80)
        foreground = Color.WHITE
        toolTipText = "Kicsinyítés"
    }
    private val btnResetView = JButton("Visszaállítás").apply {
        background = Color(80, 80, 80)
        foreground = Color.WHITE
        toolTipText = "Alap nézet visszaállítása (zoom + panning)"
    }
    // Egyéb funkciók
    private val btnSettings = JButton("Beállítások").apply {
        background = Color(80, 80, 80)
        foreground = Color.WHITE
    }
    private val btnCalc = JButton("Számol & Rajzol").apply {
        background = Color(100, 100, 100)
        foreground = Color.WHITE
        font = Font(font.name, Font.BOLD, 14)
    }

    // A File menüben elérhető exportálási lehetőségek
    private val menuBar = JMenuBar().apply {
        val fileMenu = JMenu("File")
        val miExportPNG = JMenuItem("Export PNG")
        miExportPNG.addActionListener { exportGraphPanel() }
        val miSaveLog = JMenuItem("Eredmény mentése")
        miSaveLog.addActionListener { saveLog() }
        fileMenu.add(miExportPNG)
        fileMenu.add(miSaveLog)
        add(fileMenu)
    }

    // További komponens: eredmény kijelző (lblResult)
    private val lblResult = JLabel("<html><i>Eredmények itt</i></html>").apply {
        foreground = Color.LIGHT_GRAY
        preferredSize = Dimension(380, 80)
    }

    // A felső gombokat tartalmazó panel (csoportosítva)
    private val functionButtonsPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
        background = Color(40, 40, 40)
        add(btnAddFunction)
        add(btnClearFunctions)
    }
    private val viewButtonsPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
        background = Color(40, 40, 40)
        add(btnZoomIn)
        add(btnZoomOut)
        add(btnResetView)
    }
    private val miscButtonsPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
        background = Color(40, 40, 40)
        add(btnSettings)
        add(btnCalc)
    }
    private val topPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        background = Color(40, 40, 40)
        add(functionButtonsPanel)
        add(viewButtonsPanel)
        add(miscButtonsPanel)
    }

    private val graphSettings = GraphSettings()

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        layout = BorderLayout()
        // Beállítjuk a menüsávot
        jMenuBar = menuBar

        // A bal oldali menü összeállítása
        val leftPanel = JPanel(BorderLayout()).apply {
            background = Color(40, 40, 40)
            add(topPanel, BorderLayout.NORTH)
            add(leftTabbedPane, BorderLayout.CENTER)
            add(lblResult, BorderLayout.SOUTH)
        }
        splitPane.leftComponent = leftPanel
        add(splitPane, BorderLayout.CENTER)

        // Státusz sáv a koordinátákhoz
        val statusBar = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            background = Color(60, 60, 60)
            add(lblCoordinates)
        }
        add(statusBar, BorderLayout.SOUTH)

        // Eseménykezelők:
        btnAddFunction.addActionListener { addFunctionPanel("f(x)= x", -10.0, 10.0, randomColor()) }
        btnClearFunctions.addActionListener {
            fvContainer.removeAll()
            fvContainer.revalidate()
            fvContainer.repaint()
            graphPanel.functionList = emptyList()
            graphPanel.repaint()
        }
        btnCalc.addActionListener { doCalculate() }
        btnSettings.addActionListener {
            val dlg = SettingsDialog(this, graphSettings) {
                // Alkalmazzuk az új bal panel szélességet:
                splitPane.dividerLocation = graphSettings.leftPanelWidth
                SwingUtilities.updateComponentTreeUI(this)
                graphPanel.repaint()
            }
            dlg.isVisible = true
        }
        btnZoomIn.addActionListener { graphPanel.zoomIn() }
        btnZoomOut.addActionListener { graphPanel.zoomOut() }
        btnResetView.addActionListener { graphPanel.resetView() }

        // Koordináta követés a grafikus panelen
        graphPanel.addMouseMotionListener(object : MouseAdapter() {
            override fun mouseMoved(e: MouseEvent) {
                val centerX = graphPanel.width / 2 + graphPanel.offsetX
                val centerY = graphPanel.height / 2 + graphPanel.offsetY
                val x = (e.x - centerX) / graphPanel.scale.toDouble()
                val y = (centerY - e.y) / graphPanel.scale.toDouble()
                lblCoordinates.text = "x = ${"%.2f".format(x)} , y = ${"%.2f".format(y)}"
            }
        })

        graphPanel.graphSettings = graphSettings

        pack()
        setLocationRelativeTo(null)
    }

    /**
     * Új függvény bevitel panel hozzáadása.
     */
    private fun addFunctionPanel(expr: String, domainS: Double, domainE: Double, col: Color) {
        val fpanel = FunctionInputPanel { panelToRemove ->
            fvContainer.remove(panelToRemove)
            fvContainer.revalidate()
            fvContainer.repaint()
        }
        fpanel.txtExpression.text = expr
        fpanel.txtDomainStart.text = domainS.toString()
        fpanel.txtDomainEnd.text = domainE.toString()
        fpanel.btnColor.background = col
        fvContainer.add(fpanel)
        fvContainer.revalidate()
        fvContainer.repaint()
    }

    /**
     * Numerikus keresés zérushelyekre.
     */
    private fun findZeros(expr: Expression, start: Double, end: Double, step: Double = 0.01): List<Double> {
        val res = mutableListOf<Double>()
        var x = start
        val eps = 1e-7
        var prevVal = expr.setVariable("x", x).evaluate()
        while (x <= end) {
            val currentVal = expr.setVariable("x", x).evaluate()
            if (abs(currentVal) < eps) {
                res.add(x)
            } else if (prevVal * currentVal < 0) {
                res.add(x - (step / 2.0))
            }
            x += step
            prevVal = currentVal
        }
        return res.distinct().sorted()
    }

    /**
     * Numerikus metszéspont-keresés (f1 - f2 = 0).
     */
    private fun findIntersections(
        e1: Expression,
        e2: Expression,
        start: Double,
        end: Double,
        step: Double
    ): List<Pair<Double, Double>> {
        val list = mutableListOf<Pair<Double, Double>>()
        var x = start
        val eps = 1e-7
        var prevVal = e1.setVariable("x", x).evaluate() - e2.setVariable("x", x).evaluate()
        while (x <= end) {
            val diff = e1.setVariable("x", x).evaluate() - e2.setVariable("x", x).evaluate()
            if (abs(diff) < eps) {
                list.add(Pair(x, e1.setVariable("x", x).evaluate()))
            } else if (prevVal * diff < 0) {
                val mid = x - (step / 2.0)
                list.add(Pair(mid, e1.setVariable("x", mid).evaluate()))
            }
            x += step
            prevVal = diff
        }
        return list
    }

    /**
     * "Számol & Rajzol" eseménykezelő.
     */
    private fun doCalculate() {
        try {
            val dataList = mutableListOf<FunctionData>()
            for (i in 0 until fvContainer.componentCount) {
                val fpanel = fvContainer.getComponent(i) as? FunctionInputPanel ?: continue
                val fd = fpanel.toFunctionData()
                if (fd.expressionText.isBlank()) continue

                val eBuilder = ExpressionBuilder(fd.expressionText).variable("x")
                globalFunctions.forEach { eBuilder.function(it) }
                eBuilder.operator(factorialOperator)
                val exp = eBuilder.build()
                fd.expression = exp
                dataList.add(fd)
            }

            // Zérushelyek
            val zeroMap = mutableMapOf<FunctionData, List<Double>>()
            for (fd in dataList) {
                val expr = fd.expression ?: continue
                if (!fd.visible) {
                    zeroMap[fd] = emptyList()
                    continue
                }
                zeroMap[fd] = findZeros(expr, fd.domainStart, fd.domainEnd, graphSettings.stepForNumericalSearch)
            }

            // Metszéspontok
            val allIntersections = mutableListOf<Pair<Double, Double>>()
            for (i in dataList.indices) {
                for (j in i + 1 until dataList.size) {
                    val f1 = dataList[i]
                    val f2 = dataList[j]
                    if (!f1.visible || !f2.visible) continue
                    val e1 = f1.expression ?: continue
                    val e2 = f2.expression ?: continue
                    val start = max(f1.domainStart, f2.domainStart)
                    val end = min(f1.domainEnd, f2.domainEnd)
                    if (end > start) {
                        allIntersections.addAll(
                            findIntersections(e1, e2, start, end, graphSettings.stepForNumericalSearch)
                        )
                    }
                }
            }

            graphPanel.functionList = dataList
            graphPanel.zeroPointsMap = zeroMap
            graphPanel.intersectionPoints = allIntersections
            graphPanel.repaint()

            val sb = StringBuilder("<html>")
            for (fd in dataList) {
                sb.append("<b>${fd.expressionText}</b>, domain=[${fd.domainStart}, ${fd.domainEnd}]<br>")
                val zs = zeroMap[fd].orEmpty()
                if (zs.isEmpty()) {
                    sb.append("<i>Nincs zérushely a domainben</i><br>")
                } else {
                    sb.append("Zérushelyek: ${zs.joinToString { "%.4f".format(it) }}<br>")
                }
                sb.append("<br>")
            }
            if (allIntersections.isNotEmpty()) {
                sb.append("<b>Metszéspontok:</b><br>")
                allIntersections.forEach { (mx, my) ->
                    sb.append("x=%.4f, y=%.4f<br>".format(mx, my))
                }
            } else {
                sb.append("<i>Nincsenek metszéspontok vagy nincs legalább 2 látható függvény.</i>")
            }
            sb.append("</html>")
            lblResult.text = sb.toString()

        } catch (ex: Exception) {
            ex.printStackTrace()
            JOptionPane.showMessageDialog(this, "Hiba: ${ex.message}", "Hiba", JOptionPane.ERROR_MESSAGE)
        }
    }

    /**
     * Grafikon exportálása PNG fájlba.
     */
    private fun exportGraphPanel() {
        val fileChooser = JFileChooser()
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            var file = fileChooser.selectedFile
            if (!file.name.lowercase().endsWith(".png")) {
                file = file.parentFile.resolve(file.name + ".png")
            }
            val image = BufferedImage(graphPanel.width, graphPanel.height, BufferedImage.TYPE_INT_ARGB)
            val g = image.createGraphics()
            graphPanel.paint(g)
            g.dispose()
            try {
                ImageIO.write(image, "png", file)
                JOptionPane.showMessageDialog(this, "Export sikeres!")
            } catch (ex: Exception) {
                JOptionPane.showMessageDialog(this, "Export hiba: ${ex.message}")
            }
        }
    }

    /**
     * Eredmény mentése szövegfájlba.
     */
    private fun saveLog() {
        val fileChooser = JFileChooser()
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            var file = fileChooser.selectedFile
            if (!file.name.lowercase().endsWith(".txt")) {
                file = file.parentFile.resolve(file.name + ".txt")
            }
            try {
                file.writeText(lblResult.text.replace(Regex("<.*?>"), ""))
                JOptionPane.showMessageDialog(this, "Mentés sikeres!")
            } catch (ex: Exception) {
                JOptionPane.showMessageDialog(this, "Mentés hiba: ${ex.message}")
            }
        }
    }

    /**
     * Kis segéd random szín generálása.
     */
    private fun randomColor(): Color {
        val h = Math.random().toFloat()
        val s = 0.5f + Math.random().toFloat() * 0.5f
        val b = 0.7f + Math.random().toFloat() * 0.3f
        return Color.getHSBColor(h, s, b)
    }

    companion object {
        // Globális függvények az exp4j számára
        val globalFunctions = listOf(
            object : Function("abs", 1) {
                override fun apply(values: DoubleArray): Double = kotlin.math.abs(values[0])
            },
            object : Function("sign", 1) {
                override fun apply(values: DoubleArray): Double {
                    val v = values[0]
                    return when {
                        v > 0.0 -> 1.0
                        v < 0.0 -> -1.0
                        else -> 0.0
                    }
                }
            },
            object : Function("floor", 1) {
                override fun apply(values: DoubleArray): Double = floor(values[0])
            },
            object : Function("ceil", 1) {
                override fun apply(values: DoubleArray): Double = ceil(values[0])
            },
            object : Function("log10", 1) {
                override fun apply(values: DoubleArray): Double = log10(values[0])
            },
            object : Function("log2", 1) {
                override fun apply(values: DoubleArray): Double = log(values[0], 2.0)
            }
        )

        // Faktorális operátor
        val factorialOperator = object : Operator("!", 1, true, Operator.PRECEDENCE_POWER + 1) {
            override fun apply(vararg values: Double): Double {
                val x = values[0]
                val n = x.toInt()
                if (n < 0) throw ArithmeticException("Negatív számnak nincs faktoriálisa!")
                var f = 1.0
                for (i in 2..n) f *= i
                return f
            }
        }
    }
}

// ==========================================
// Globális LAF beállítás és belépési pont
// ==========================================

/**
 * Próbálja beállítani a FlatLaf Dark-ot, ha nem sikerül, akkor a rendszer LAF-ját.
 */
fun trySetGlobalLookAndFeel() {
    try {
        UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf")
    } catch (ex: Exception) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (_: Exception) { }
    }
}

/**
 * A program belépési pontja.
 */
fun main() {
    SwingUtilities.invokeLater {
        trySetGlobalLookAndFeel()
        val frame = MainFrame()
        frame.isVisible = true
    }
}
