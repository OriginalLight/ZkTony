package com.zktony.android.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.io.source.ByteArrayOutputStream
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.AreaBreakType
import com.itextpdf.layout.properties.UnitValue
import com.zktony.room.entities.Log
import com.zktony.room.entities.LogSnapshot
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.random.Random


object PdfUtils {

    fun generatePdf(file: File, log: Log, snapshots: List<LogSnapshot>) {
        try {
            // gen a pdf with itext
            val pdfWriter = PdfWriter(file.outputStream())
            val pdfDocument = PdfDocument(pdfWriter)

            //获取页面的宽度像素
            val document = Document(pdfDocument, PageSize.A4, true)
            //设置字体
            document.setFont(PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H"))
            //设置字体大小（都要自己设置常量）
            document.setFontSize(16f)
            //设置字体颜色
            document.setFontColor(DeviceRgb.BLACK)

            //设置title
            document.add(Paragraph("实验记录"))
            // 创建一个表格
            val table = Table(
                arrayOf(
                    UnitValue.createPercentValue(50f),
                    UnitValue.createPercentValue(50f)
                )
            ).useAllAvailableWidth()

            val array = arrayOf(
                arrayOf(
                    "状态", when (log.status) {
                        0 -> "完成"
                        1 -> "中止"
                        2 -> "出错"
                        else -> "未知"
                    }
                ),
                arrayOf("通道", "通道${log.channel + 1}"),
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
            ).filterNotNull()

            // Add 8 rows with 2 columns each
            for (i in array.indices) {
                for (j in array[i].indices) {
                    table.addCell(Cell().add(Paragraph(array[i][j])))
                }
            }

            document.add(table)

            document.add(AreaBreak(AreaBreakType.NEXT_PAGE))

            // 时间-电流图
            document.add(Image(ImageDataFactory.create(ByteArrayOutputStream().apply {
                generateChart(snapshots).compress(Bitmap.CompressFormat.JPEG, 100, this)
            }.toByteArray())))

            // 时间-电压图
            document.add(Image(ImageDataFactory.create(ByteArrayOutputStream().apply {
                generateChart(snapshots).compress(Bitmap.CompressFormat.JPEG, 100, this)
            }.toByteArray())))

            // 时间-功率图
            document.add(Image(ImageDataFactory.create(ByteArrayOutputStream().apply {
                generateChart(snapshots).compress(Bitmap.CompressFormat.JPEG, 100, this)
            }.toByteArray())))

            // close the document
            document.close()
            // close the writer
            pdfWriter.close()

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun generateChart(snapshots: List<LogSnapshot>): Bitmap {
        //创建一个画布 画布的大小是屏幕大小
        val canvas = Canvas()
        //创建一个bitmap 画布的大小是屏幕大小
        val bitmap = Bitmap.createBitmap(600, 280, Bitmap.Config.ARGB_8888)
        //将画布与bitmap关联
        canvas.setBitmap(bitmap)
        //设置画布的背景颜色
        canvas.drawColor(0xffffffff.toInt())
        // draw a chart on the page
        val paint = Paint()

        val points = Array(30) { arrayOf(0f, 0f) }

        repeat(30) {
            points[it] = arrayOf(it * 10f, Random.nextFloat() * 20)
        }

        val margin = 24f
        val xAxisWith = 600f - 2 * margin
        val yAxisWith = 280f - 2 * margin

        // draw the x-axis
        canvas.drawLine(
            margin,
            margin + yAxisWith,
            margin + xAxisWith,
            margin + yAxisWith, paint.apply {
                color = 0xFF000000.toInt()
                style = Paint.Style.STROKE
            })
        // draw the y-axis
        canvas.drawLine(
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
            canvas.drawCircle(
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
            canvas.drawLine(
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
            canvas.drawText(
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
            canvas.drawText(
                "${i * 10}",
                margin - 20,
                margin + yAxisWith - i * 10 * yAxisWith / 60,
                paint.apply {
                    color = 0xFF000000.toInt()
                    style = Paint.Style.FILL
                }
            )
        }

        return bitmap
    }

}