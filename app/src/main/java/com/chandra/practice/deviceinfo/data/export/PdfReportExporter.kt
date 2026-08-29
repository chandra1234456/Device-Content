package com.chandra.practice.deviceinfo.data.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.StaticLayout
import android.text.TextPaint
import com.chandra.practice.deviceinfo.data.model.DeviceInfoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val DEVELOPER_NAME = "Balachandra Dasari"
private const val DEVELOPER_EMAIL = "chandradev3660@gmail.com"

// A4 at 72dpi, in points.
private const val PAGE_WIDTH = 595
private const val PAGE_HEIGHT = 842
private const val MARGIN = 40f

/** Where [PdfReportExporter.export] ended up saving the file, for the confirmation message. */
sealed interface PdfSaveLocation {
    data class Downloads(val fileName: String) : PdfSaveLocation
    data class AppFolder(val fileName: String, val path: String) : PdfSaveLocation
}

object PdfReportExporter {

    suspend fun export(
        context: Context,
        appName: String,
        categoryTitle: String,
        items: List<DeviceInfoItem>,
    ): PdfSaveLocation = withContext(Dispatchers.IO) {
        val fileName = buildFileName(categoryTitle)
        val document = PdfDocument()
        try {
            renderPages(document, appName, categoryTitle, items)
            saveToDownloads(context, fileName, document)
        } finally {
            document.close()
        }
    }

    private fun buildFileName(categoryTitle: String): String {
        val safeCategory = categoryTitle.replace(Regex("[^A-Za-z0-9]+"), "_").trim('_')
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "DeviceContent_${safeCategory}_$timestamp.pdf"
    }

    private fun renderPages(
        document: PdfDocument,
        appName: String,
        categoryTitle: String,
        items: List<DeviceInfoItem>,
    ) {
        val titlePaint = TextPaint().apply { isAntiAlias = true; textSize = 20f; isFakeBoldText = true; color = Color.parseColor("#1C1B1F") }
        val metaPaint = TextPaint().apply { isAntiAlias = true; textSize = 11f; color = Color.parseColor("#49454F") }
        val labelPaint = TextPaint().apply { isAntiAlias = true; textSize = 11f; isFakeBoldText = true; color = Color.parseColor("#6750A4") }
        val valuePaint = TextPaint().apply { isAntiAlias = true; textSize = 12f; color = Color.parseColor("#1C1B1F") }
        val footerPaint = TextPaint().apply { isAntiAlias = true; textSize = 9f; color = Color.parseColor("#79747E") }

        val contentWidth = (PAGE_WIDTH - MARGIN * 2).toInt()
        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        var canvas = page.canvas
        var y = MARGIN

        fun finishPage() {
            canvas.drawText("Page $pageNumber", MARGIN, PAGE_HEIGHT - 20f, footerPaint)
            canvas.drawText(
                "$DEVELOPER_NAME · $DEVELOPER_EMAIL",
                PAGE_WIDTH - MARGIN,
                PAGE_HEIGHT - 20f,
                footerPaint.apply { textAlign = android.graphics.Paint.Align.RIGHT },
            )
            footerPaint.textAlign = android.graphics.Paint.Align.LEFT
            document.finishPage(page)
        }

        fun startNewPage() {
            finishPage()
            pageNumber++
            page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
            canvas = page.canvas
            y = MARGIN
        }

        // Header — only on the first page.
        canvas.drawText(appName, MARGIN, y + 20f, titlePaint)
        y += 32f
        canvas.drawText("Device Information Report — $categoryTitle", MARGIN, y + 14f, metaPaint)
        y += 18f
        canvas.drawText(
            "Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}",
            MARGIN,
            y + 14f,
            metaPaint,
        )
        y += 18f
        canvas.drawText("Developer: $DEVELOPER_NAME", MARGIN, y + 14f, metaPaint)
        y += 16f
        canvas.drawText("Email: $DEVELOPER_EMAIL", MARGIN, y + 14f, metaPaint)
        y += 30f

        items.forEach { item ->
            val labelLayout = StaticLayout.Builder.obtain(item.label, 0, item.label.length, labelPaint, contentWidth).build()
            val valueLayout = StaticLayout.Builder.obtain(item.value, 0, item.value.length, valuePaint, contentWidth).build()
            val blockHeight = labelLayout.height + valueLayout.height + 14f

            if (y + blockHeight > PAGE_HEIGHT - MARGIN) {
                startNewPage()
            }

            canvas.save()
            canvas.translate(MARGIN, y)
            labelLayout.draw(canvas)
            canvas.restore()
            y += labelLayout.height + 2f

            canvas.save()
            canvas.translate(MARGIN, y)
            valueLayout.draw(canvas)
            canvas.restore()
            y += valueLayout.height + 12f
        }

        finishPage()
    }

    private fun saveToDownloads(context: Context, fileName: String, document: PdfDocument): PdfSaveLocation {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("Couldn't create the PDF in Downloads")
            resolver.openOutputStream(uri)?.use { out -> document.writeTo(out) }
                ?: error("Couldn't open an output stream for the new file")
            PdfSaveLocation.Downloads(fileName)
        } else {
            // Pre-scoped-storage fallback: no WRITE_EXTERNAL_STORAGE permission is requested for
            // this (Android 8.1/9 make up a vanishing share of real installs), so save to the
            // app's own external files directory instead, which never needs a permission.
            val dir = context.getExternalFilesDir(null) ?: context.filesDir
            val file = File(dir, fileName)
            file.outputStream().use { out -> document.writeTo(out) }
            PdfSaveLocation.AppFolder(fileName, dir.path)
        }
    }
}
