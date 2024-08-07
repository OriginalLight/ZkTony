package com.zktony.android.utils

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.zktony.android.ui.utils.getAttributes
import com.zktony.room.entities.Log
import java.io.File

object PdfUtils {

    fun generatePdf(file: File) {
        val document = PdfDocument()
        // draw the table on the page
        val pageInfo1 = PdfDocument.PageInfo.Builder(612, 792, 1).create()
        val page1 = document.startPage(pageInfo1)
        generateTable(page1)
        document.finishPage(page1)

        // draw current chart on the page
        val pageInfo2 = PdfDocument.PageInfo.Builder(612, 792, 2).create()
        val page2 = document.startPage(pageInfo2)
        generateChart(page2)
        document.finishPage(page2)

        // draw voltage chart on the page
        val pageInfo3 = PdfDocument.PageInfo.Builder(612, 792, 3).create()
        val page3 = document.startPage(pageInfo3)
        generateChart(page3)
        document.finishPage(page3)

        // draw power chart on the page
        val pageInfo4 = PdfDocument.PageInfo.Builder(612, 792, 4).create()
        val page4 = document.startPage(pageInfo4)
        generateChart(page4)
        document.finishPage(page4)

        if (file.exists()) {
            file.delete()
            file.createNewFile()
        } else {
            file.createNewFile()
        }

        document.writeTo(file.outputStream())
        document.close()
    }

    private fun generateTable(page: PdfDocument.Page) {
        // draw a table on the page with 3 rows and 3 columns of text data
        val paint = Paint()
        val log = Log(name = "Test", value = "20", time = "10", flowSpeed = "10")
        val array = arrayOf(
            arrayOf("实验名称", log.name),
            arrayOf("实验类型", if (log.experimentalType == 0) "转膜" else "染色"),
            arrayOf(
                "实验模式", when (log.experimentalMode) {
                    0 -> "恒压"
                    1 -> "恒流"
                    2 -> "恒功率"
                    else -> "未知"
                }
            ),
            arrayOf(
                "数值", "${log.value} ${
                    when (log.experimentalMode) {
                        0 -> "V"
                        1 -> "A"
                        2 -> "W"
                        else -> "未知"
                    }
                }"
            ),
            if (log.experimentalType == 0) arrayOf(
                "流量",
                "${log.flowSpeed} mL/min"
            ) else null,
            arrayOf("时间", "${log.time} min"),
            arrayOf("胶种类", if (log.glueType == 0) "普通胶" else "梯度胶"),
            arrayOf("胶浓度", log.getGlueConcentrationStr()),
            arrayOf(
                "胶厚度", "${
                    when (log.glueThickness) {
                        0 -> "0.75"
                        1 -> "1.0"
                        2 -> "1.5"
                        else -> "未知"
                    }
                } mm"
            ),
            arrayOf("蛋白大小", "${log.proteinSize} kDa"),
            arrayOf("缓冲液类型", if (log.bufferType == 0) "厂家" else "其他"),
        )

        val table = array.filterNotNull()

        val cellWidth = (612f - 64f) / 2
        val margin = 32f
        val cellHeight = 32f

        // draw the border cells
        page.canvas.drawRect(
            margin,
            margin,
            612 - margin,
            margin + (table.size) * cellHeight,
            paint.apply {
                color = 0xFF000000.toInt()
                style = Paint.Style.STROKE
            })

        for (i in 0..<table.size - 1) {
            page.canvas.drawLine(
                margin,
                margin + (i + 1) * cellHeight,
                612 - margin,
                margin + (i + 1) * cellHeight,
                paint.apply {
                    color = 0xFF000000.toInt()
                    style = Paint.Style.STROKE
                }
            )
        }

        page.canvas.drawLine(
            306f,
            margin,
            306f,
            margin + (table.size) * cellHeight,
            paint.apply {
                color = 0xFF000000.toInt()
                style = Paint.Style.STROKE
            }
        )

        // draw the text data
        for (i in table.indices) {
            for (j in 0..<table[i].size) {
                page.canvas.drawText(
                    table[i][j],
                    margin + 12f + j * cellWidth,
                    20f + (i + 1) * cellHeight,
                    paint.apply {
                        color = 0xFF000000.toInt()
                        style = Paint.Style.FILL
                    }
                )
            }
        }
    }

    private fun generateChart(page: PdfDocument.Page) {
        // draw a chart on the page
        val paint = Paint()
        val points = arrayOf(
            arrayOf(0f, 0f),
            arrayOf(10f, 10f),
            arrayOf(20f, 20f),
            arrayOf(30f, 30f),
            arrayOf(40f, 40f),
            arrayOf(50f, 50f),
            arrayOf(60f, 60f),
        )
        val margin = 48f
        val xAxisWith = 612 - 2 * margin
        val yAxisWith = xAxisWith * 0.75f
        // draw the x-axis
        page.canvas.drawLine(
            margin,
            margin + yAxisWith,
            margin + xAxisWith,
            margin + yAxisWith, paint.apply {
            color = 0xFF000000.toInt()
            style = Paint.Style.STROKE
        })
        // draw the y-axis
        page.canvas.drawLine(
            margin,
            margin,
            margin,
            margin + yAxisWith,
            paint.apply {
                color = 0xFF000000.toInt()
                style = Paint.Style.STROKE
            })

        // draw the points
        for (i in points.indices) {
            page.canvas.drawCircle(
                margin + points[i][0] * xAxisWith / 60,
                margin + yAxisWith - points[i][1] * yAxisWith / 60,
                4f,
                paint.apply {
                    color = 0xFF000000.toInt()
                    style = Paint.Style.FILL
                }
            )
        }

        // draw the lines
        for (i in 0..<points.size - 1) {
            page.canvas.drawLine(
                margin + points[i][0] * xAxisWith / 60,
                margin + yAxisWith - points[i][1] * yAxisWith / 60,
                margin + points[i + 1][0] * xAxisWith / 60,
                margin + yAxisWith - points[i + 1][1] * yAxisWith / 60,
                paint.apply {
                    color = 0xFF000000.toInt()
                    style = Paint.Style.STROKE
                }
            )
        }

        // draw the x-axis text
        for (i in 0..6) {
            page.canvas.drawText(
                "${i * 10}",
                margin + i * 10 * xAxisWith / 60,
                margin + yAxisWith + 20,
                paint.apply {
                    color = 0xFF000000.toInt()
                    style = Paint.Style.FILL
                }
            )
        }

        // draw the y-axis text
        for (i in 0..6) {
            page.canvas.drawText(
                "${i * 10}",
                margin - 20,
                margin + yAxisWith - i * 10 * yAxisWith / 60,
                paint.apply {
                    color = 0xFF000000.toInt()
                    style = Paint.Style.FILL
                }
            )
        }

        // draw the x-axis unit
        page.canvas.drawText(
            "s",
            margin + xAxisWith + 20,
            margin + yAxisWith + 20,
            paint.apply {
                color = 0xFF000000.toInt()
                style = Paint.Style.FILL
            }
        )

        // draw the y-axis unit
        page.canvas.drawText(
            "A",
            margin - 20,
            margin - 20,
            paint.apply {
                color = 0xFF000000.toInt()
                style = Paint.Style.FILL
            }
        )

        // draw the title
        page.canvas.drawText(
            "时间-电流曲线",
            margin,
            margin + yAxisWith + 48f,
            paint.apply {
                color = 0xFF000000.toInt()
                style = Paint.Style.FILL
            }
        )
    }

}