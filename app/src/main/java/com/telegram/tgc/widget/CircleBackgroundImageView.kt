package com.telegram.tgc.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import com.telegram.tgc.R

class CircleBackgroundImageView @JvmOverloads constructor(
    context: Context, private val attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    @ColorInt
    var circleColorResId: Int = 0

    var checked: Boolean = true

    init {
        attrs?.let {
            val a = context.obtainStyledAttributes(
                attrs,
                R.styleable.CircleBackgroundImageView,
                defStyleAttr,
                0
            )
            circleColorResId =
                a.getColor(R.styleable.CircleBackgroundImageView_circle_color, defStyleAttr)
            a.recycle()
        }
    }

    private val paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = 2f
    }

    override fun onDraw(canvas: Canvas?) {
        if (circleColorResId != 0) {
            paint.style = if (checked) {
                Paint.Style.FILL
            } else {
                Paint.Style.STROKE
            }
            paint.color = circleColorResId
            val radius = if (height < width) height / 2f else width / 2f
            canvas?.drawCircle(width / 2f, height / 2f, radius - 2, paint)
        }
        super.onDraw(canvas)
    }
}