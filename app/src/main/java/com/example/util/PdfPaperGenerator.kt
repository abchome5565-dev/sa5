package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.McqQuestion
import com.example.data.Test
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfPaperGenerator {

    fun generateTestPaperPdf(
        context: Context,
        test: Test,
        questions: List<McqQuestion>,
        includeAnswerKey: Boolean = true
    ): File? {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // Standard A4 width in points (8.27 in * 72)
        val pageHeight = 842 // Standard A4 height in points (11.69 in * 72)

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(13, 71, 161) // Deep Blue
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(30, 136, 229) // Primary Blue
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val headerLabelPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val bodyPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 11f
            typeface = Typeface.DEFAULT
        }

        val questionNumPaint = Paint().apply {
            color = Color.rgb(13, 71, 161)
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        var y = 40f

        // Title Header
        canvas.drawText("CATALYST ACADEMY", pageWidth / 2f, y, titlePaint)
        y += 18f
        canvas.drawText("OFFICIAL EXAMINATION PAPER", pageWidth / 2f, y, subtitlePaint)
        y += 20f

        // Header info box
        canvas.drawLine(30f, y, pageWidth - 30f, y, linePaint)
        y += 15f

        val todayStr = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Date())
        canvas.drawText("Subject: ${test.subjectName}", 40f, y, headerLabelPaint)
        canvas.drawText("Class: ${test.className}", 250f, y, headerLabelPaint)
        canvas.drawText("Date: $todayStr", 420f, y, headerLabelPaint)
        y += 16f

        canvas.drawText("Title: ${test.title}", 40f, y, bodyPaint)
        canvas.drawText("Time Limit: ${test.timeLimitMinutes} Mins", 250f, y, bodyPaint)
        canvas.drawText("Total Marks: ${test.totalQuestions * 1}", 420f, y, bodyPaint)
        y += 15f

        canvas.drawLine(30f, y, pageWidth - 30f, y, linePaint)
        y += 25f

        // Render Questions
        questions.forEachIndexed { index, mcq ->
            // Check page overflow
            if (y > pageHeight - 100f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 40f
            }

            // Question Text
            val qNumText = "Q${index + 1}. "
            canvas.drawText(qNumText, 40f, y, questionNumPaint)

            // Wrap Question Text
            val availableWidth = pageWidth - 90f
            val words = mcq.questionText.split(" ")
            var line = ""
            var xOffset = 65f

            words.forEach { word ->
                val testLine = if (line.isEmpty()) word else "$line $word"
                if (bodyPaint.measureText(testLine) < availableWidth) {
                    line = testLine
                } else {
                    canvas.drawText(line, xOffset, y, bodyPaint)
                    y += 14f
                    line = word
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line, xOffset, y, bodyPaint)
                y += 16f
            }

            // Options grid
            canvas.drawText("(A) ${mcq.optionA}", 65f, y, bodyPaint)
            canvas.drawText("(B) ${mcq.optionB}", 300f, y, bodyPaint)
            y += 14f
            canvas.drawText("(C) ${mcq.optionC}", 65f, y, bodyPaint)
            canvas.drawText("(D) ${mcq.optionD}", 300f, y, bodyPaint)
            y += 22f
        }

        // Add Answer Key Page if requested
        if (includeAnswerKey && questions.isNotEmpty()) {
            if (y > pageHeight - 200f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 40f
            } else {
                y += 20f
                canvas.drawLine(30f, y, pageWidth - 30f, y, linePaint)
                y += 25f
            }

            canvas.drawText("ANSWER KEY (TEACHER COPY)", 40f, y, headerLabelPaint)
            y += 20f

            var keyX = 40f
            questions.forEachIndexed { idx, q ->
                val keyStr = "${idx + 1}. [${q.correctAnswer}]  "
                canvas.drawText(keyStr, keyX, y, questionNumPaint)
                keyX += 80f
                if (keyX > pageWidth - 100f) {
                    keyX = 40f
                    y += 18f
                }
            }
            y += 25f
        }

        pdfDocument.finishPage(page)

        // Save PDF file
        return try {
            val file = File(context.getExternalFilesDir(null), "Paper_${test.title.replace(" ", "_")}_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    fun generateStudentReportCardPdf(
        context: Context,
        result: com.example.data.TestResult
    ): File? {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(13, 71, 161) // Deep Blue
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(30, 136, 229) // Primary Blue
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val headerLabelPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val bodyPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
            typeface = Typeface.DEFAULT
        }

        val gradePaint = Paint().apply {
            color = if (result.grade != "F") Color.rgb(46, 125, 50) else Color.rgb(211, 47, 47)
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1.5f
        }

        var y = 50f

        // Header
        canvas.drawText("CATALYST ACADEMY", pageWidth / 2f, y, titlePaint)
        y += 22f
        canvas.drawText("STUDENT REPORT CARD / TEST RESULT", pageWidth / 2f, y, subtitlePaint)
        y += 25f

        canvas.drawLine(40f, y, pageWidth - 40f, y, linePaint)
        y += 30f

        // Student Info Block
        val todayStr = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Date(result.completedAt))
        canvas.drawText("Student Name: ${result.studentName}", 50f, y, headerLabelPaint)
        canvas.drawText("Roll Number: ${result.rollNumber.ifBlank { "N/A" }}", 340f, y, headerLabelPaint)
        y += 22f

        canvas.drawText("Subject: ${result.subjectName}", 50f, y, bodyPaint)
        canvas.drawText("Class: ${result.className.ifBlank { "General" }}", 340f, y, bodyPaint)
        y += 22f

        canvas.drawText("Date: $todayStr", 50f, y, bodyPaint)
        canvas.drawText("Time Used: ${result.timeUsedSeconds / 60}m ${result.timeUsedSeconds % 60}s", 340f, y, bodyPaint)
        y += 30f

        canvas.drawLine(40f, y, pageWidth - 40f, y, linePaint)
        y += 40f

        // Result Score Box
        canvas.drawText("Grade: ${result.grade}", pageWidth / 2f, y, gradePaint)
        y += 35f

        canvas.drawText("Performance Summary", pageWidth / 2f, y, subtitlePaint)
        y += 30f

        val leftX = 100f
        val rightX = 380f

        canvas.drawText("Total Questions:", leftX, y, headerLabelPaint)
        canvas.drawText("${result.totalQuestions}", rightX, y, bodyPaint)
        y += 24f

        canvas.drawText("Correct Answers:", leftX, y, headerLabelPaint)
        canvas.drawText("${result.correctAnswers}", rightX, y, bodyPaint)
        y += 24f

        canvas.drawText("Wrong Answers:", leftX, y, headerLabelPaint)
        canvas.drawText("${result.wrongAnswers}", rightX, y, bodyPaint)
        y += 24f

        canvas.drawText("Marks Obtained:", leftX, y, headerLabelPaint)
        canvas.drawText("${result.obtainedMarks} / ${result.maxMarks}", rightX, y, bodyPaint)
        y += 24f

        canvas.drawText("Percentage:", leftX, y, headerLabelPaint)
        canvas.drawText(String.format(Locale.getDefault(), "%.1f%%", result.percentage), rightX, y, bodyPaint)
        y += 40f

        canvas.drawLine(40f, y, pageWidth - 40f, y, linePaint)
        y += 60f

        // Signature lines
        canvas.drawLine(60f, y, 200f, y, linePaint)
        canvas.drawLine(380f, y, 520f, y, linePaint)
        y += 18f

        canvas.drawText("Teacher Signature", 130f, y, bodyPaint)
        canvas.drawText("Principal Stamp", 450f, y, bodyPaint)

        pdfDocument.finishPage(page)

        return try {
            val file = File(context.getExternalFilesDir(null), "ReportCard_${result.studentName.replace(" ", "_")}_${result.id}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    fun openPdfFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
