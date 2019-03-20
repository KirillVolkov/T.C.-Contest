package com.telegram.tgc.model

class ChartAxisData(
    val axisName: String,
    val points: List<Long>,
    val type: String,
    val name: String,
    val color: Int
)

class ChartDataSet(
    val columns: List<ChartAxisData>
)