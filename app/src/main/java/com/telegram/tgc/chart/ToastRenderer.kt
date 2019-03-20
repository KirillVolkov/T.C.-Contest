package com.telegram.tgc.chart

import android.graphics.*

import com.telegram.tgc.chart.base.IChartRenderer
import com.telegram.tgc.model.ChartSelectedPoint

class ToastRenderer : IChartRenderer {

    var position: Float = -1f

    private var items: List<ChartSelectedPoint> = emptyList()
    private var axisInfoBounds: List<RectF> = emptyList()

    private fun getToastWidth(): Float {
        paint.textSize = smallText
        return Math.max(
            axisInfoBounds.sumByFloat { it.width() + smallText },
            paint.measureText(items[0].chartPoint.x.dateString) + smallText
        )
    }

    private fun getToastHeight(): Float {
        return axisInfoBounds[0].height() + smallText * 2
    }

    fun reset() {
        position = -1f
        items = emptyList()
    }

    private val paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 4f
        textSize = 48f
    }

    private val bigText = 64f
    private val smallText = 32f

    private val paintFill = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val toastPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
        setShadowLayer(16f, 0f, 0f, Color.LTGRAY)
    }

    fun setDrawInfo(items: List<ChartSelectedPoint>) {
        this.items = items
        axisInfoBounds = items.map { calcAxisInfoBounds(it, 0f, 0f) }
    }

    override fun draw(canvas: Canvas) {
        if (position > 0 && items.isNotEmpty()) {
            paint.style = Paint.Style.STROKE
            val pos = items[0].point.x
            canvas.drawLine(pos, 0f, pos, canvas.height * 4 / 5f, paint.apply { color = Color.LTGRAY })
            items.forEach {
                canvas.drawCircle(pos, it.point.y, 10f, paintFill)
                canvas.drawCircle(pos, it.point.y, 10f, paint.apply {
                    color = it.color
                })
            }
            drawToast(canvas, pos, smallText)
        }
    }

    private fun drawToast(canvas: Canvas, x: Float, y: Float) {
        paint.style = Paint.Style.FILL
        paint.textSize = smallText
        paint.color = Color.BLACK
        val w = getToastWidth()
        val rect = RectF(x - w / 4, y, x + w * 3 / 4, y + getToastHeight())
        canvas.drawRoundRect(rect, 16f, 16f, toastPaint)
        canvas.drawText(items[0].chartPoint.x.dateString, rect.left + smallText / 2, y + smallText, paint)
        var posX = rect.left + 26f
        items.forEachIndexed { index, chartSelectedPoint ->
            paint.color = chartSelectedPoint.color
            posX = canvas.drawAxisInfo(chartSelectedPoint, axisInfoBounds[index], posX, rect.top + smallText)
        }
    }

    private fun calcAxisInfoBounds(point: ChartSelectedPoint, xPos: Float, yPos: Float): RectF {
        paint.textSize = bigText
        val s1 = paint.measureText(point.name)
        paint.textSize = smallText
        val s2 = paint.measureText(point.chartPoint.y.value.toString())
        return RectF(xPos, yPos, Math.max(s1, s2), yPos + smallText + bigText + 20)
    }

    private fun Canvas.drawAxisInfo(point: ChartSelectedPoint, rect: RectF, xOffset: Float, yOffset: Float): Float {
        paint.textSize = bigText
        drawText(point.name, xOffset, yOffset + paint.textSize, paint)
        paint.textSize = smallText
        drawText(point.chartPoint.y.value.toString(), xOffset, yOffset + rect.height(), paint)
        return xOffset + rect.width() + smallText
    }
}

inline fun <T> Iterable<T>.sumByFloat(selector: (T) -> Float): Float {
    var sum = 0f
    for (element in this) {
        sum += selector(element)
    }
    return sum
}