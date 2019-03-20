package com.telegram.tgc

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Point
import com.google.gson.JsonParser
import com.telegram.tgc.model.ChartAxisData
import com.telegram.tgc.model.ChartDataSet

fun calculateIntersectionPoint(
    a: Point,
    b: Point,
    c: Point,
    d: Point
): Point? {

    val a1 = b.y - a.y
    val b1 = a.x - b.x
    val c1 = a1 * (a.x) + b1 * (a.y)

    val a2 = d.y - c.y
    val b2 = c.x - d.x
    val c2 = a2 * (c.x) + b2 * (c.y)

    val determinant = a1 * b2 - a2 * b1

    if (determinant == 0) {
        return null
    }

    val x = (b2 * c1 - b1 * c2) / determinant
    val y = (a1 * c2 - a2 * c1) / determinant
    return Point(x, y)
}

fun parseData(resources: Resources): List<ChartDataSet> {

    val datas = ArrayList<ChartDataSet>()

    val text = resources.openRawResource(R.raw.chart_data)
        .bufferedReader().use { it.readText() }

    val datasets = JsonParser().parse(text).asJsonArray

    datasets.forEach { dataset ->
        val i = dataset.asJsonObject

        val types = i.get("types").asJsonObject
        val names = i.get("names").asJsonObject
        val colors = i.get("colors").asJsonObject

        datas.add(ChartDataSet(i.get("columns").asJsonArray.map { column ->
            val columns = column.asJsonArray
            val title = columns[0].asString
            columns.remove(0)
            when (title) {
                "x" -> {
                    val c = columns.asJsonArray.map { it.asLong }
                    ChartAxisData(
                        title,
                        c,
                        types.get(title).asString,
                        "x",
                        Color.BLACK
                    )
                }
                else -> {
                    val c = columns.asJsonArray.map { it.asLong }
                    ChartAxisData(
                        title,
                        c,
                        types.get(title).asString,
                        names.get(title).asString,
                        Color.parseColor(colors.get(title).asString)
                    )
                }
            }
        }
        ))

    }
    return datas
}