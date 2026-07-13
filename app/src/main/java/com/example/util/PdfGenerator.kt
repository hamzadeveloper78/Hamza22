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
                color = Color.rgb(30, 41, 59) // slate-800
                textSize = 18f
                isAntiAlias = true
                isFakeBoldText = true
            }

            val metaPaint = TextPaint().apply {
                color = Color.rgb(100, 116, 139) // slate-500
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
                color = Color.rgb(226, 232, 240) // slate-200
                style = Paint.Style.STROKE
                strokeWidth = 0.5f
            }

            val headerBgPaint = Paint().apply {
                color = Color.rgb(79, 70, 229) // indigo-600
                style = Paint.Style.FILL
            }

            val zebraPaint = Paint().apply {
                color = Color.rgb(248, 250, 252) // slate-50
                style = Paint.Style.FILL
            }

            val currentTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("ar")).format(Date())

            // Columns widths (Total CONTENT_WIDTH = 535)
            // Layout (from right to left in Arabic view, but physically laid out by x coordinate)
            // Left to Right:
            // 1. Phone (110) - 2. Level (85) - 3. Specialization (120) - 4. Name (175) - 5. ID (45)
            val colWidths = floatArrayOf(110f, 85f, 120f, 175f, 45f)
            val colHeaders = arrayOf("رقم الهاتف", "المستوى", "التخصص", "اسم الطالب", "الرقم")

            // Rows per page calculation
            val headerHeight = 110f
            val tableHeaderHeight = 25f
            val rowHeight = 25f
            val footerHeight = 40f
            val maxTableHeight = PAGE_HEIGHT - headerHeight - tableHeaderHeight - footerHeight
            val rowsPerPage = (maxTableHeight / rowHeight).toInt()

            val totalPages = if (students.isEmpty()) 1 else Math.ceil(students.size.toDouble() / rowsPerPage).toInt()

            var currentStudentIndex = 0

            for (pageNumber in 1..totalPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                // 1. Header Section (Only on first page, or simplified on later pages)
                var currentY = MARGIN

                if (pageNumber == 1) {
                    // Title
                    drawRtlText(canvas, "تقرير نظام إدارة الطلاب الشامل", PAGE_WIDTH - MARGIN, currentY, titlePaint, Layout.Alignment.ALIGN_NORMAL)
                    currentY += 25f

                    // Metadata
                    val metaText = "تاريخ التصدير: $currentTimestamp   |   إجمالي الطلاب: ${students.size} طالب   |   حالة الاتصال: محلي (دون إنترنت)"
                    drawRtlText(canvas, metaText, PAGE_WIDTH - MARGIN, currentY, metaPaint, Layout.Alignment.ALIGN_NORMAL)
                    currentY += 20f

                    // Decorative Line
                    val linePaint = Paint().apply {
                        color = Color.rgb(79, 70, 229)
                        strokeWidth = 2f
                    }
                    canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, linePaint)
                    currentY += 15f
                } else {
                    // Mini header for sub-pages
                    drawRtlText(canvas, "تقرير نظام إدارة الطلاب - تابع", PAGE_WIDTH - MARGIN, currentY, titlePaint.apply { textSize = 12f }, Layout.Alignment.ALIGN_NORMAL)
                    currentY += 18f
                    val metaText = "إجمالي الطلاب: ${students.size}   |   صفحة $pageNumber من $totalPages"
                    drawRtlText(canvas, metaText, PAGE_WIDTH - MARGIN, currentY, metaPaint, Layout.Alignment.ALIGN_NORMAL)
                    currentY += 10f
                    val linePaint = Paint().apply {
                        color = Color.rgb(79, 70, 229)
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
                    // Draw cell background / boundaries
                    if (i > 0) {
                        canvas.drawLine(currentX, currentY, currentX, currentY + tableHeaderHeight, borderPaint)
                    }
                    // Draw text centered/aligned right in cell
                    val textX = currentX + w - 8f
                    val textY = currentY + 6f
                    drawRtlText(canvas, colHeaders[i], textX, textY, headerTextPaint, Layout.Alignment.ALIGN_NORMAL, w - 10f)
                    currentX += w
                }
                currentY += tableHeaderHeight

                // 3. Table Rows
                val itemsOnThisPage = Math.min(students.size - currentStudentIndex, rowsPerPage)
                for (r in 0 until itemsOnThisPage) {
                    val student = students[currentStudentIndex]
                    currentStudentIndex++

                    // Zebra striping
                    if (r % 2 == 1) {
                        canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + rowHeight, zebraPaint)
                    }

                    // Row bottom border
                    canvas.drawLine(MARGIN, currentY + rowHeight, PAGE_WIDTH - MARGIN, currentY + rowHeight, borderPaint)

                    // Row contents
                    // Map student fields to our columns (Right to Left: ID, Name, Specialization, Level, Phone)
                    val rowData = arrayOf(
                        student.phone,
                        student.level,
                        student.specialization,
                        student.name,
                        student.id.toString()
                    )

                    var cellX = MARGIN
                    for (i in rowData.indices) {
                        val w = colWidths[i]
                        if (i > 0) {
                            canvas.drawLine(cellX, currentY, cellX, currentY + rowHeight, borderPaint)
                        }

                        val textX = cellX + w - 8f
                        val textY = currentY + 6f
                        drawRtlText(canvas, rowData[i], textX, textY, cellTextPaint, Layout.Alignment.ALIGN_NORMAL, w - 10f)
                        cellX += w
                    }
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
