package com.telegram.tgc

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.TextureView
import android.view.View
import com.telegram.tgc.chart.ChartDataSetRenderer
import com.telegram.tgc.chart.LevelsRenderer
import com.telegram.tgc.chart.ToastRenderer
import com.telegram.tgc.chart.XAxisRenderer
import com.telegram.tgc.model.ChartDataSet
import java.text.SimpleDateFormat
import java.util.*


class ChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextureView(context, attrs, defStyleAttr), TextureView.SurfaceTextureListener,
    View.OnTouchListener {

    private val renderer: ChartRenderer = ChartRenderer(this)

    fun setData(data: ChartDataSet) {
        val xs = data.columns.find { it.axisName == "x" }!!
        val ys = data.columns.filter { it.axisName != "x" }

        val e =
            ys.map { convertDatasetToChartPoints(xs.points, it.points) }


        renderer.setData(e, ys.map { it.color }, ys.map { it.axisName })
    }

    fun selectAxis(position: Int, enabled: Boolean) {
        renderer.selectAxis(position, enabled)
    }

    private fun convertDatasetToChartPoints(x: List<Long>, y: List<Long>): List<ChartPoint> {
        return x.mapIndexed { index, chartAxisData ->
            ChartPoint(XAxisPoint(chartAxisData), YAxisPoint(y[index]))
        }
    }

    override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, p1: Int, p2: Int) {

    }

    override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) {

    }

    override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?): Boolean {
        return renderer.stopRendering()
    }

    override fun onSurfaceTextureAvailable(p0: SurfaceTexture?, p1: Int, p2: Int) {
        renderer.start()
    }

    override fun onTouch(p0: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (event.rawY > height * 4 / 5) {
                    renderer.touchControl(event)
                } else {
                    renderer.touchPointSelector(event)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                renderer.moveControl(event)
            }
            MotionEvent.ACTION_UP -> {
                renderer.endControlInteraction(event)
            }
            else -> {

            }
        }
        return true
    }


    init {
        setOnTouchListener(this)
        surfaceTextureListener = this
    }

}

internal class ChartRenderer(private val surface: TextureView) : Thread() {

    @Volatile
    private var needRedraw = false

    private var windowWidth: Int = 0
    private var windowMinWidth = 0
    private var windowXPos: Int = 0
    private var windowEndPos: Int = 0

    private var chartPointsAtOnce = 6

    private val toastRenderer = ToastRenderer()
    private val xAxisRenderer = XAxisRenderer()

    private val chartHeight get() = surface.height * 9 / 12f
    private val controlHeight get() = surface.height / 6
    private val xAxisHeight get() = surface.height / 12

    private val controlPaint = Paint().apply {
        color = Color.LTGRAY
        alpha = 50
        isAntiAlias = true
    }

    private val borderControlPaint = Paint().apply {
        color = Color.BLACK
        alpha = 50
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private val stickControlPaint = Paint().apply {
        color = Color.BLACK
        alpha = 50
        style = Paint.Style.STROKE
        strokeWidth = 16f
        isAntiAlias = true
    }

    private var controlEvent: ControlEvent = ControlEvent.NONE

    private var touchOffset = 0f

    private var maxY = 0L

    private lateinit var levelsRenderer: LevelsRenderer

    fun selectAxis(position: Int, enabled: Boolean) {
        renderers[position].enabled = enabled
        needRedraw = true
    }

    fun setData(data: List<List<ChartPoint>>, colors: List<Int>, names: List<String>) {

        renderers.clear()

        data.forEachIndexed { index, list ->
            renderers.add(ChartDataSetRenderer.create(list).apply {
                val y = getMaxY()
                maxY = if (y > maxY) y else maxY
                this.color = colors[index]
                this.name = names[index]
            })
        }

        initRenderers()
    }

    private val renderers: ArrayList<ChartDataSetRenderer> = arrayListOf()

    /**
     * touch down
     */
    fun touchControl(event: MotionEvent) {
        toastRenderer.reset()
        controlEvent = when (event.rawX) {
            in (windowXPos.toFloat() - stickControlPaint.strokeWidth * 2)..(windowXPos + stickControlPaint.strokeWidth * 2) -> {
                ControlEvent.RESIZE_LEFT
            }
            in (windowXPos + windowWidth - stickControlPaint.strokeWidth * 2)..(windowXPos + windowWidth + stickControlPaint.strokeWidth * 2) -> {
                ControlEvent.RESIZE_RIGHT
            }
            else -> {
                ControlEvent.MOVE
            }
        }

        if (event.rawX in windowXPos..windowXPos + windowWidth) {
            windowEndPos = windowXPos + windowWidth
            touchOffset = event.rawX - windowXPos
        }
    }

    fun touchPointSelector(event: MotionEvent) {
        controlEvent = ControlEvent.MOVE_SELECTOR
        toastRenderer.position = event.rawX
    }

    /**
     * Touch up
     */
    fun endControlInteraction(event: MotionEvent) {
        moveControl(event)
        touchOffset = -1f
    }

    /**
     * touch move
     */
    fun moveControl(event: MotionEvent) {
        when (controlEvent) {
            ControlEvent.RESIZE_LEFT -> {
                val w = windowEndPos - windowXPos
                windowWidth = if (w < windowMinWidth)
                    windowMinWidth
                else w

                if (windowWidth + windowXPos < surface.width) {
                    windowXPos = event.rawX.toInt()
                }

            }
            ControlEvent.RESIZE_RIGHT -> {
                val w = (event.rawX - windowXPos).toInt()
                windowWidth = if (w < windowMinWidth)
                    windowMinWidth
                else w
            }
            ControlEvent.MOVE -> {
                if (touchOffset > 0) {
                    val x = (event.rawX.toInt() - touchOffset).toInt()

                    when {
                        x + windowWidth > surface.width -> {
                            windowXPos = surface.width - windowWidth
                            return
                        }
                        x < 0 -> {
                            windowXPos = 0
                            return
                        }
                        else -> windowXPos = x
                    }
                }
            }
            ControlEvent.MOVE_SELECTOR -> {
                toastRenderer.position = event.rawX
            }
            else -> {
            }
        }
        needRedraw = true
    }

    /**
     * Draws touch control with dimmed area
     */
    private fun Canvas.drawMoveControl() {

        drawRect( //Left Dimm Rect
            0f,
            chartHeight + xAxisHeight,
            windowXPos.toFloat(),
            surface.height.toFloat(),
            controlPaint
        )

        drawTouchControl()

        drawRect( //Right Dimm Rect
            (windowXPos + windowWidth).toFloat(),
            chartHeight + xAxisHeight,
            (surface.width).toFloat(),
            surface.height.toFloat(),
            controlPaint
        )
    }

    /**
     * Draws touch control, which describes visible rect on chart
     */
    private fun Canvas.drawTouchControl() {
        val horizontalStartX =
            windowXPos + stickControlPaint.strokeWidth - borderControlPaint.strokeWidth
        val horizontalendX =
            (windowXPos + windowWidth).toFloat() - stickControlPaint.strokeWidth / 2 + borderControlPaint.strokeWidth

        drawLine( //Horizontal Top
            horizontalStartX,
            chartHeight + borderControlPaint.strokeWidth / 2 + xAxisHeight,
            horizontalendX,
            chartHeight + borderControlPaint.strokeWidth / 2 + xAxisHeight,
            borderControlPaint
        )

        drawLine( //Horizontal Bottom
            horizontalStartX,
            height - borderControlPaint.strokeWidth / 2,
            horizontalendX,
            height - borderControlPaint.strokeWidth / 2,
            borderControlPaint
        )

        drawLine( //Vertical Left
            windowXPos + borderControlPaint.strokeWidth,
            chartHeight + xAxisHeight,
            windowXPos + borderControlPaint.strokeWidth,
            height.toFloat(),
            stickControlPaint
        )

        drawLine( //Vertical Right
            windowXPos + windowWidth + borderControlPaint.strokeWidth,
            chartHeight + xAxisHeight,
            windowXPos + windowWidth + borderControlPaint.strokeWidth,
            height.toFloat(),
            stickControlPaint
        )
    }

    /**
     * Draws bottom chart control
     */
    private fun Canvas.drawControl() {
        drawMoveControl()
    }

    override fun run() {

        while (!Thread.interrupted()) {
            if (needRedraw) {
                var maxFrameY = 0L
                renderers.forEach {
                    val y = it.calcChartFrame(
                        windowXPos.toFloat() / surface.width,
                        windowWidth.toFloat() / (surface.width),
                        windowMinWidth.toFloat() / windowWidth
                    )
                    if (y > maxFrameY) {
                        maxFrameY = y
                    }
                }
                val animState = levelsRenderer.updateMaxY(maxFrameY)

                if (toastRenderer.position > 0) {
                    toastRenderer.setDrawInfo(renderers.filter { it.enabled }.map {
                        it.getPointNearPosition(
                            toastRenderer.position,
                            chartHeight
                        )
                    })
                }

                renderers.find { it.enabled }?.let {
                    xAxisRenderer.bounds = it.chartBounds
                    xAxisRenderer.offset = it.currOffset
                    xAxisRenderer.scale = it.currScale
                }

                val canvas = surface.lockCanvas(null)
                try {
                    canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR)
                    levelsRenderer.draw(canvas)
                    renderers.forEach { it.draw(canvas) }
                    canvas.drawControl()

                    toastRenderer.draw(canvas)

                    renderers.forEach {
                        it.calcYScale(
                            maxY,
                            maxFrameY,
                            animState,
                            chartHeight
                        )
                    }
                    xAxisRenderer.draw(canvas)
                    needRedraw = false || animState < 1
                } finally {
                    surface.unlockCanvasAndPost(canvas)
                }

            }
        }
    }

    private fun initRenderers() {
        if (renderers.size > 0 && surface.width > 0) {
            renderers.forEach {
                chartPointsAtOnce = it.data.size / 5
                it.calcPaths(surface, maxY, chartPointsAtOnce)
            }

            windowMinWidth =
                (surface.width) / renderers[0].data.size * chartPointsAtOnce
            windowWidth = windowMinWidth
            windowXPos = surface.width - windowWidth
            levelsRenderer = LevelsRenderer.init(
                surface.width,
                chartHeight
            )
            xAxisRenderer.xAxisData = renderers[0].data.map { it.x }
            xAxisRenderer.yPos = chartHeight
            needRedraw = true
        }
    }

    override fun start() {
        super.start()
        initRenderers()
    }

    fun stopRendering(): Boolean {
        return try {
            interrupt()
            needRedraw = false
            true
        } catch (e: Exception) {
            false
        }
    }

}

class ChartPoint(
    val x: XAxisPoint,
    val y: YAxisPoint
) {
    var realYPercent: Float = 0f

    fun calculateYPercents(maxValue: Long) {
        realYPercent = 1 - (1f * y.value / maxValue)
    }
}

class XAxisPoint(
    val timestamp: Long
) {
    var simpleDateString: String
    var dateString: String

    init {
        val date = Date(timestamp)
        simpleDateString = simpleFormatter.format(date)
        dateString = formatter.format(date)
    }

    companion object {
        val simpleFormatter = SimpleDateFormat("MMM dd", Locale.US)
        val formatter = SimpleDateFormat("EEE, MMM dd", Locale.US)
    }
}

class YAxisPoint(
    val value: Long
)

enum class ControlEvent {
    NONE,
    MOVE,
    MOVE_SELECTOR,
    RESIZE_LEFT,
    RESIZE_RIGHT
}
