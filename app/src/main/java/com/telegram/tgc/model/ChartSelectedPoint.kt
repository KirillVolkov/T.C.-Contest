package com.telegram.tgc.model

import android.graphics.PointF
import com.telegram.tgc.ChartPoint

class ChartSelectedPoint(
    val point: PointF,
    val chartPoint: ChartPoint,
    val color: Int,
    val name: String
)