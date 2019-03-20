package com.telegram.tgc.chart

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.telegram.tgc.chart.base.IChartRenderer

class LevelsRenderer private constructor() : IChartRenderer{

    private var currMaxY = 0L
    private var animationState: Float = 1f

    private var width: Int = 0
    private var height: Float = 0f

    private val horizontalOffset = 20f

    private var step = 0f

    private val levelPaint = Paint().apply {
        color = Color.LTGRAY
        isAntiAlias = true
        strokeWidth = 2f
        textSize = 20f
    }

    fun updateMaxY(maxY: Long): Float {
        if (maxY != currMaxY && animationState >= 1) {
            currMaxY = maxY
            animationState = 0f
        } else {
            if (animationState > 1) {
                animationState = 1f
            }
            if (animationState < 1) {
                animationState += 0.03f
            }
        }
        return animationState
    }

    override fun draw(canvas: Canvas) {
        //if (animationState < 1) {
            canvas.drawLevel(0, (this@LevelsRenderer.height), 255)
            canvas.drawLevels(currMaxY)
      //  }
    }

    /**
     * Draws Y-axis levels
     */
    private fun Canvas.drawLevels(maxY: Long) {
        val stepByY = maxY / 5

        for (i in 1..5) {
            drawLevel(
                stepByY * i,
                (this@LevelsRenderer.height) - (step * i),
                (animationState * 255).toInt()
            )
        }
    }

    /**
     * Draw single y-axis level
     */
    private fun Canvas.drawLevel(
        value: Long,
        level: Float,
        alpha: Int
    ) {
       // levelPaint.alpha = alpha
        drawText(
            value.toString(),
            horizontalOffset,
            (level) + textHeight / 2,
            levelPaint
        )

        drawLine(
            horizontalOffset,
            level + textHeight,
            this@LevelsRenderer.width.toFloat() - horizontalOffset,
            level + textHeight,
            levelPaint
        )
    }

    companion object {

        const val textHeight = 40

        fun init(width: Int, height: Float): LevelsRenderer {
            val levelsRenderer = LevelsRenderer()
            levelsRenderer.width = width
            levelsRenderer.height = height - textHeight * 2

            levelsRenderer.step = (levelsRenderer.height - textHeight) / 5f

            return levelsRenderer
        }
    }

}