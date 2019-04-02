package com.telegram.tgc.chart

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.telegram.tgc.XAxisPoint
import com.telegram.tgc.chart.base.IChartRenderer
import kotlin.math.roundToInt

class XAxisRenderer : IChartRenderer {

    var bounds: RectF = RectF()
    var xAxisData: List<XAxisPoint> = emptyList()

    var offset: Float = 0f
    var scale: Float = 1f

    var yPos = 0f

    var pointsAtOnce = 0

    val paint = Paint().apply {
        textSize = 32f
        color = Color.LTGRAY
        style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        val e = pointsAtOnce / scale
        val step = bounds.width() / (xAxisData.size)
        xAxisData.forEachIndexed { index, xAxisPoint ->
            when (index % (e / 6).roundToInt()) {
                2 -> {
                    canvas.drawText(
                        xAxisPoint.simpleDateString,
                        index * step - paint.measureText(xAxisPoint.simpleDateString) / 2 + offset,
                        yPos + 32f * 2,
                        paint.apply { alpha = 255 }
                    )
                }
            }
        }
    }
}