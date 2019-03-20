package com.telegram.tgc.chart

import android.graphics.*
import android.view.TextureView
import com.telegram.tgc.ChartPoint
import com.telegram.tgc.chart.base.IChartRenderer
import com.telegram.tgc.model.ChartSelectedPoint
import kotlin.math.roundToInt

class ChartDataSetRenderer private constructor() : IChartRenderer {

    var color = Color.BLACK
        set(value) {
            field = value
            paint.color = value
        }
    var name: String = ""
    private val controlPath = Path()
    private val chartPath = Path()
    val chartBounds = RectF()
    var currOffset = 0f
    var currScale = 1f
    private var currYScale = 1f
    private var currYOffset = 0f
    private var maxFrameY = 0L
    private var prevFrameScale = 0f
    private var prevMaxFrameY = 0L

    var enabled: Boolean = true

    var data: List<ChartPoint> = emptyList()
        private set

    private var paint = Paint().apply {
        color = this@ChartDataSetRenderer.color
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 4f
        isAntiAlias = true
    }

    fun getMaxY(): Long {
        return data.maxBy { it.y.value }?.y?.value ?: 0L
    }

    override fun draw(canvas: Canvas) {
        if (enabled) {
            canvas.drawPath(controlPath, paint)
            canvas.drawPath(chartPath, paint)
        }
    }

    fun calcChartFrame(
        offset: Float,
        selectedRange: Float,
        scale: Float
    ): Long { //TODO OPTIMIZE
        if (enabled) {
            val translateMatrix = Matrix()
            translateMatrix.setTranslate(-currOffset, 0f)
            chartPath.transform(translateMatrix)
            if (currScale != scale && scale > 0) {
                setScale(scale, currYScale)
            }
            currOffset = -offset * chartBounds.width()
            translateMatrix.setTranslate(currOffset, 0f)
            chartPath.transform(translateMatrix)

            maxFrameY = calcMaxFrameY(offset, selectedRange)
            return maxFrameY
        } else {
            return 0L
        }
    }

    fun getPointNearPosition(xPos: Float, height: Float): ChartSelectedPoint {
        val actualX = Math.abs(currOffset) + xPos
        val index = (data.size * actualX / chartBounds.width()).roundToInt()
        if (index >= data.size) {
            println()
        }
        val p = data[index]
        return ChartSelectedPoint(
            PointF(data.indexOf(p) * step * currScale + currOffset, p.realYPercent * currYScale * height - currYOffset),
            p,
            color,
            name
        )
    }

    fun calcYScale(
        absoluteMaxY: Long,
        maxFrameY: Long,
        animationState: Float,
        height: Float
    ) {
        if (enabled) {
            if (animationState > 0f && animationState <= 1f) {
                var sy = absoluteMaxY.toFloat() / maxFrameY
                sy = prevFrameScale + (sy - prevFrameScale) * animationState
                if (animationState == 1f || prevMaxFrameY != maxFrameY) {
                    prevFrameScale = sy
                }
                val translateMatrix = Matrix()
                translateMatrix.setTranslate(0f, currYOffset)
                chartPath.transform(translateMatrix)
                setScale(currScale, sy)
                currYOffset = height * sy - height
                translateMatrix.setTranslate(0f, -currYOffset)
                chartPath.transform(translateMatrix)
            }
        }
    }

    private fun calcMaxFrameY(offset: Float, selectedRange: Float): Long {
        return data.subList(
            ((data.size - 1) * offset).roundToInt(),
            ((data.size - 1) * (offset + selectedRange)).roundToInt()
        )
            .maxBy { it.y.value }?.y?.value ?: 0L
    }

    private fun setScale(scale: Float, scaleY: Float) { //TODO OPTIMIZE
        val scaleMatrix = Matrix()
        scaleMatrix.setScale(1 / currScale, 1 / currYScale)
        chartPath.transform(scaleMatrix)
        scaleMatrix.setScale(scale, scaleY)
        chartPath.transform(scaleMatrix)
        currScale = scale
        currYScale = scaleY
        chartPath.computeBounds(chartBounds, true)
    }

    var step = 0f

    fun calcPaths(surface: TextureView, max: Long, pointsAtOnce: Int) {
        data.forEach {
            it.calculateYPercents(max)
        }
        step = (surface.width.toFloat()) / (data.size - 1)

        controlPath.moveTo(
            0f,
            surface.height * 1 / 6 * data[0].realYPercent + surface.height * 5 / 6
        )
        for (i in 1 until data.size) {
            controlPath.lineTo(
                ((step * i)),
                surface.height * 1 / 6 * data[i].realYPercent + surface.height * 5 / 6
            )
        }

        step = (surface.width.toFloat()) / (pointsAtOnce - 1)
        chartPath.moveTo(
            0f,
            data[0].realYPercent * surface.height * 9 / 12
        )
        for (i in 1 until data.size) {
            chartPath.lineTo(
                (step * i),
                data[i].realYPercent * surface.height * 9 / 12
            )
        }
        chartPath.computeBounds(chartBounds, true)
        println(chartBounds)
    }

    companion object {
        fun create(
            data: List<ChartPoint>
        ): ChartDataSetRenderer {
            val chartDataSetRenderer = ChartDataSetRenderer()
            chartDataSetRenderer.data = data
            return chartDataSetRenderer
        }
    }
}