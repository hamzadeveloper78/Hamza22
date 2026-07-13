package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.data.Student
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    // A4 dimensions in PostScript points (1/72 inch)
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 30f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (2 * MARGIN)

    suspend fun generateStudentReport(context: Context, students: List<Student>): Uri? = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        try {
            val titlePaint = TextPaint().apply {
                color = Color.rgb(15, 23, 42) // slate-900 (instead of rgb(30, 41, 59))
                textSize = 18f
                isAntiAlias = true
                isFakeBoldText = true
            }

            val metaPaint = TextPaint().apply {
                color = Color.rgb(71, 85, 105) // slate-600
                textSize = 9f
                isAntiAlias = true
            }

            val headerTextPaint = TextPaint().apply {
                color = Color.WHITE
                textSize = 10f
                isAntiAlias = true
                isFakeBoldText = true
            }

            val cellTextPaint = TextPaint().apply {
                color = Color.rgb(51, 65, 85) // slate-700
                textSize = 9f
                isAntiAlias = true
            }

            val borderPaint = Paint().apply {
                color = Color.rgb(203, 213, 225) // slate-300
                style = Paint.Style.STROKE
                strokeWidth = 0.5f
            }

            val headerBgPaint = Paint().apply {
                color = Color.rgb(13, 148, 136) // Teal-600
                style = Paint.Style.FILL
            }

            val zebraPaint = Paint().apply {
                color = Color.rgb(248, 250, 252) // slate-50
                style = Paint.Style.FILL
            }

            val currentTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("ar")).format(Date())

            val boldCellPaint = TextPaint().apply {
                color = Color.rgb(15, 23, 42) // slate-900
                textSize = 9.5f
                isAntiAlias = true
                isFakeBoldText = true
            }

            val smallCellPaint = TextPaint().apply {
                color = Color.rgb(71, 85, 105) // slate-600
                textSize = 8f
                isAntiAlias = true
            }

            val levelTextPaint = TextPaint().apply {
                color = Color.rgb(17, 94, 89) // Teal-800
                textSize = 8.5f
                isAntiAlias = true
                isFakeBoldText = true
            }

            // Columns widths (Total CONTENT_WIDTH = 535)
            // Left to Right:
            // 1. Phone & Residence (165f) - 2. Specialization & Level (170f) - 3. Name & Identity (200f)
            val colWidths = floatArrayOf(165f, 170f, 200f)
            val colHeaders = arrayOf("رقم الهاتف والعنوان بالتفصيل", "التخصص والمستوى الأكاديمي", "اسم الطالب وإثبات الهوية")

            // Rows per page calculation
            val headerHeight = 150f
            val tableHeaderHeight = 28f
            val rowHeight = 38f
            val footerHeight = 40f
            val maxTableHeight = PAGE_HEIGHT - headerHeight - tableHeaderHeight - footerHeight
            val rowsPerPage = (maxTableHeight / rowHeight).toInt()

            val totalPages = if (students.isEmpty()) 1 else Math.ceil(students.size.toDouble() / rowsPerPage).toInt()

            var currentStudentIndex = 0

            for (pageNumber in 1..totalPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                // 1. Header Section
                var currentY = MARGIN

                if (pageNumber == 1) {
                    // Left-Side Header (الجمهورية اليمنية، اب، شارع الثلاثين)
                    drawRtlText(canvas, "الجمهورية اليمنية\nإب\nشارع الثلاثين", MARGIN, currentY, metaPaint.apply { textSize = 9.5f; isFakeBoldText = true }, Layout.Alignment.ALIGN_OPPOSITE, 200f)

                    // Right-Side Header (سكن امل الغد، تلفون 777559252)
                    drawRtlText(canvas, "سكن أمل الغد\nتلفون: 777559252", PAGE_WIDTH - MARGIN, currentY, metaPaint.apply { textSize = 9.5f; isFakeBoldText = true }, Layout.Alignment.ALIGN_NORMAL, 200f)

                    currentY += 50f

                    // Title Centered
                    val centerTitlePaint = TextPaint().apply {
                        color = Color.rgb(13, 148, 136) // Teal-600
                        textSize = 15f
                        isAntiAlias = true
                        isFakeBoldText = true
                    }
                    drawRtlText(canvas, "تقرير كشف الطلاب الشامل", (PAGE_WIDTH / 2f) + 100f, currentY, centerTitlePaint, Layout.Alignment.ALIGN_NORMAL, 200f)
                    currentY += 20f

                    // Metadata
                    val metaText = "تاريخ التصدير: $currentTimestamp   |   إجمالي الطلاب: ${students.size} طالب"
                    drawRtlText(canvas, metaText, PAGE_WIDTH - MARGIN, currentY, metaPaint.apply { textSize = 8.5f; isFakeBoldText = false }, Layout.Alignment.ALIGN_NORMAL)
                    currentY += 15f

                    // Decorative Line
                    val linePaint = Paint().apply {
                        color = Color.rgb(13, 148, 136) // Teal-600
                        strokeWidth = 2f
                    }
                    canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, linePaint)
                    currentY += 15f
                } else {
                    // Mini header for sub-pages
                    drawRtlText(canvas, "تقرير كشف الطلاب الشامل - تابع", PAGE_WIDTH - MARGIN, currentY, titlePaint.apply { textSize = 11f }, Layout.Alignment.ALIGN_NORMAL)
                    currentY += 16f
                    val metaText = "إجمالي الطلاب: ${students.size}   |   صفحة $pageNumber من $totalPages"
                    drawRtlText(canvas, metaText, PAGE_WIDTH - MARGIN, currentY, metaPaint.apply { textSize = 8f }, Layout.Alignment.ALIGN_NORMAL)
                    currentY += 10f
                    val linePaint = Paint().apply {
                        color = Color.rgb(13, 148, 136) // Teal-600
                        strokeWidth = 1f
                    }
                    canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, linePaint)
                    currentY += 15f
                }

                // 2. Table Header
                canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + tableHeaderHeight, headerBgPaint)

                var currentX = MARGIN
                for (i in colHeaders.indices) {
                    val w = colWidths[i]
                    if (i > 0) {
                        canvas.drawLine(currentX, currentY, currentX, currentY + tableHeaderHeight, borderPaint)
                    }
                    val textX = currentX + w - 8f
                    val textY = currentY + 8f
                    drawRtlText(canvas, colHeaders[i], textX, textY, headerTextPaint, Layout.Alignment.ALIGN_NORMAL, w - 10f)
                    currentX += w
                }
                currentY += tableHeaderHeight

                // 3. Table Rows
                val itemsOnThisPage = Math.min(students.size - currentStudentIndex, rowsPerPage)
                for (r in 0 until itemsOnThisPage) {
                    val student = students[currentStudentIndex]
                    val displayIndex = currentStudentIndex + 1
                    currentStudentIndex++

                    // Zebra striping
                    if (r % 2 == 1) {
                        canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + rowHeight, zebraPaint)
                    }

                    // Row bottom border
                    canvas.drawLine(MARGIN, currentY + rowHeight, PAGE_WIDTH - MARGIN, currentY + rowHeight, borderPaint)

                    // Vertical Dividers
                    val rightDividerX = PAGE_WIDTH - MARGIN - 200f
                    canvas.drawLine(rightDividerX, currentY, rightDividerX, currentY + rowHeight, borderPaint)

                    val centerDividerX = PAGE_WIDTH - MARGIN - 200f - 170f
                    canvas.drawLine(centerDividerX, currentY, centerDividerX, currentY + rowHeight, borderPaint)

                    // Right Section: Student Name & Identity
                    val nameText = "$displayIndex. ${student.name}"
                    drawRtlText(canvas, nameText, PAGE_WIDTH - MARGIN - 6f, currentY + 4f, boldCellPaint, Layout.Alignment.ALIGN_NORMAL, 188f)
                    val idText = "الهوية: ${student.idType} (${student.idNumber})"
                    drawRtlText(canvas, idText, PAGE_WIDTH - MARGIN - 6f, currentY + 21f, smallCellPaint, Layout.Alignment.ALIGN_NORMAL, 188f)

                    // Center Section: Specialization & Academic Level
                    drawRtlText(canvas, student.specialization, rightDividerX - 6f, currentY + 4f, cellTextPaint, Layout.Alignment.ALIGN_NORMAL, 158f)
                    drawRtlText(canvas, student.level, rightDividerX - 6f, currentY + 21f, levelTextPaint, Layout.Alignment.ALIGN_NORMAL, 158f)

                    // Left Section: Phone & Detailed Address
                    drawRtlText(canvas, "تلفون: ${student.phone}", centerDividerX - 6f, currentY + 4f, cellTextPaint, Layout.Alignment.ALIGN_NORMAL, 153f)
                    val addressText = "السكن: محافظة ${student.governorate}، م. ${student.district}" + 
                        if (student.sector.isNotEmpty() || student.village.isNotEmpty()) {
                            " (عزلة ${student.sector}، ق. ${student.village})"
                        } else ""
                    drawRtlText(canvas, addressText, centerDividerX - 6f, currentY + 21f, smallCellPaint, Layout.Alignment.ALIGN_NORMAL, 153f)

                    currentY += rowHeight
                }

                // Draw outer table boundary box
                canvas.drawRect(MARGIN, currentY - (itemsOnThisPage * rowHeight) - tableHeaderHeight, PAGE_WIDTH - MARGIN, currentY, borderPaint)

                // 4. Page Footer
                val footerY = PAGE_HEIGHT - footerHeight
                canvas.drawLine(MARGIN, footerY, PAGE_WIDTH - MARGIN, footerY, borderPaint)

                val footerText = "لوحة إدارة الطلاب الذكية  •  صفحة $pageNumber من $totalPages"
                drawRtlText(canvas, footerText, PAGE_WIDTH - MARGIN, footerY + 12f, metaPaint, Layout.Alignment.ALIGN_NORMAL)

                val copyrightText = "جمهورية اليمن - وزارة التعليم العالي"
                drawRtlText(canvas, copyrightText, MARGIN + 120f, footerY + 12f, metaPaint, Layout.Alignment.ALIGN_OPPOSITE)

                pdfDocument.finishPage(page)
            }

            // Save document to cache file
            val reportFile = File(context.cacheDir, "student_management_report.pdf")
            FileOutputStream(reportFile).use { fos ->
                pdfDocument.writeTo(fos)
            }
            FileProvider.getUriForFile(context, "com.example.fileprovider", reportFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            pdfDocument.close()
        }
    }

    /**
     * Helper function to draw Arabic text correctly with RTL orientation.
     * Uses StaticLayout to handle bidi shaping and line breaking.
     */
    private fun drawRtlText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        paint: TextPaint,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
        maxWidth: Float = CONTENT_WIDTH
    ) {
        canvas.save()
        // Move to the position where we want to draw the text block
        // For Arabic/RTL text block, we align it nicely.
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, maxWidth.toInt())
            .setAlignment(alignment)
            .setLineSpacing(0f, 1.0f)
            .setIncludePad(false)
            .build()

        // If aligning normal in an RTL layout, we want the right edge of layout to line up with x.
        // StaticLayout translates text internally. To position it at target x, we translate canvas.
        val xTranslate = if (alignment == Layout.Alignment.ALIGN_NORMAL) {
            x - maxWidth
        } else {
            x
        }

        canvas.translate(xTranslate, y)
        layout.draw(canvas)
        canvas.restore()
    }
}
