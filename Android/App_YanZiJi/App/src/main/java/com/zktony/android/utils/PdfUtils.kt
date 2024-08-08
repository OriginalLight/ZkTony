package com.zktony.android.utils

import android.annotation.SuppressLint
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
import com.zktony.android.utils.extra.dateFormat
import com.zktony.log.LogUtils
import com.zktony.room.entities.Log
import com.zktony.room.entities.LogSnapshot
import java.io.File
import java.math.RoundingMode
import kotlin.math.ceil
import kotlin.math.floor


object PdfUtils {

    /**
     * 生成PDF
     * @param file File
     * @param log Log
     * @param snapshots List<LogSnapshot>
     */
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
            document.add(generateTable(log))
            // 添加一个分页符
            document.add(AreaBreak(AreaBreakType.NEXT_PAGE))

            // 时间-电流图
            generateChart(snapshots, 0)?.let {
                document.add(Image(ImageDataFactory.create(ByteArrayOutputStream().apply {
                    it.compress(Bitmap.CompressFormat.JPEG, 100, this)
                }.toByteArray())))
            }

            // 时间-电压图
            generateChart(snapshots, 1)?.let {
                document.add(Image(ImageDataFactory.create(ByteArrayOutputStream().apply {
                    it.compress(Bitmap.CompressFormat.JPEG, 100, this)
                }.toByteArray())))
            }

            // 时间-功率图
            generateChart(snapshots, 2)?.let {
                document.add(Image(ImageDataFactory.create(ByteArrayOutputStream().apply {
                    it.compress(Bitmap.CompressFormat.JPEG, 100, this)
                }.toByteArray())))
            }

            // close the document
            document.close()
            // close the writer
            pdfWriter.close()
        } catch (e: Exception) {
            LogUtils.error(e.stackTraceToString(), true)
        }
    }

    /**
     * 生成表格
     * @param log Log
     * @return Table
     */
    private fun generateTable(log: Log): Table {
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
            arrayOf("程序名称", log.name),
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
            arrayOf("开始时间", log.createTime.dateFormat()),
            arrayOf("结束时间", log.endTime.dateFormat())
        ).filterNotNull()

        // Add 8 rows with 2 columns each
        for (i in array.indices) {
            for (j in array[i].indices) {
                table.addCell(Cell().add(Paragraph(array[i][j])))
            }
        }

        return table
    }

    /**
     * 生成图表
     * @param snapshots List<LogSnapshot>
     * @param chartType Int
     * @return Bitmap?
     */
    @SuppressLint("DefaultLocale")
    private fun generateChart(snapshots: List<LogSnapshot>, chartType: Int): Bitmap? {
        // check
        if (snapshots.isEmpty()) {
            return null
        }
        // x轴数据数最多20个点
        // y轴数据数最多10个点
        var finalSnapshots: List<LogSnapshot> = snapshots

        if (snapshots.size > 20) {
            // 从snapshots中根据时间取出30个数据
            val scale = snapshots.size / 20
            finalSnapshots = snapshots.filter { it.time % scale == 0 }.take(20)
        }

        val points = Array(finalSnapshots.size) { arrayOf(0f, 0f) }
        finalSnapshots.forEachIndexed { index, logSnapshot ->
            val value = when (chartType) {
                0 -> logSnapshot.current.toFloatOrNull() ?: 0f
                1 -> logSnapshot.voltage.toFloatOrNull() ?: 0f
                2 -> logSnapshot.power.toFloatOrNull() ?: 0f
                else -> 0f
            }
            points[index] = arrayOf(logSnapshot.time.toFloat(), value)
        }

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
        val margin = 32f
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


        // draw the x-axis text
        for (i in points.indices) {
            canvas.drawText(
                points[i][0].toBigDecimal().setScale(1, RoundingMode.HALF_UP)
                    .stripTrailingZeros().toPlainString(),
                margin + i * xAxisWith / points.size,
                margin + yAxisWith + 24,
                paint.apply {
                    color = 0xFF000000.toInt()
                    style = Paint.Style.FILL
                }
            )
        }

        // draw the y-axis text
        val max = ceil(points.maxOf { it[1] })
        val min = floor(points.minOf { it[1] })

        for (i in 0..10) {
            canvas.drawText(
                (min + i * (max - min) / 10).toBigDecimal().setScale(1, RoundingMode.HALF_UP)
                    .stripTrailingZeros().toPlainString(),
                margin - 32,
                margin + yAxisWith - i * yAxisWith / 10,
                paint.apply {
                    color = 0xFF000000.toInt()
                    style = Paint.Style.FILL
                }
            )
        }

        // draw the points
        for (i in points.indices) {
            canvas.drawCircle(
                margin + i * xAxisWith / points.size,
                margin + yAxisWith - (points[i][1] - min) * yAxisWith / (max - min),
                3f,
                paint.apply {
                    color = 0xFF000000.toInt()
                    style = Paint.Style.FILL
                }
            )
        }

        // draw the lines
        for (i in 0 until points.size - 1) {
            canvas.drawLine(
                margin + i * xAxisWith / points.size,
                margin + yAxisWith - (points[i][1] - min) * yAxisWith / (max - min),
                margin + (i + 1) * xAxisWith / points.size,
                margin + yAxisWith - (points[i + 1][1] - min) * yAxisWith / (max - min),
                paint.apply {
                    color = 0xFF000000.toInt()
                    style = Paint.Style.STROKE
                }
            )
        }

        // right top text
        canvas.drawText(
            "X - 时间(s)",
            margin + xAxisWith - 32,
            24f,
            paint.apply {
                color = 0xFF000000.toInt()
                style = Paint.Style.FILL
            }
        )

        val text = when (chartType) {
            0 -> "Y - 电流(A)"
            1 -> "Y - 电压(V)"
            2 -> "Y - 功率(W)"
            else -> "Y - 未知"
        }

        canvas.drawText(
            text,
            margin + xAxisWith - 32,
            48f,
            paint.apply {
                color = 0xFF000000.toInt()
                style = Paint.Style.FILL
            }
        )

        return bitmap
    }

}