package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TestResult
import com.example.ui.theme.PrimaryDarkBlue

@Composable
fun TestResultScreen(
    result: TestResult,
    onHome: () -> Unit
) {
    val isPassed = result.grade != "F"
    val gradeColor = when (result.grade) {
        "A+", "A" -> Color(0xFF2E7D32)
        "B", "C" -> Color(0xFFF57C00)
        else -> Color(0xFFD32F2F)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .testTag("test_result_screen"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(28.dp)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (isPassed) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = result.grade,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = gradeColor
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isPassed) "CONGRATULATIONS!" else "KEEP PRACTICING!",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = gradeColor
                    )
                )

                Text(
                    text = "${result.studentName} | ${result.subjectName}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Divider()

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Grid
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ResultRow("Total Questions", "${result.totalQuestions}")
                    ResultRow("Correct Answers", "${result.correctAnswers}", textColor = Color(0xFF2E7D32))
                    ResultRow("Wrong Answers", "${result.wrongAnswers}", textColor = Color(0xFFD32F2F))
                    ResultRow("Obtained Marks", "${result.obtainedMarks} / ${result.maxMarks}")
                    ResultRow("Percentage", String.format("%.1f%%", result.percentage))
                    ResultRow("Time Used", "${result.timeUsedSeconds / 60}m ${result.timeUsedSeconds % 60}s")
                }

                Spacer(modifier = Modifier.height(28.dp))

                val context = androidx.compose.ui.platform.LocalContext.current

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            val pdfFile = com.example.util.PdfPaperGenerator.generateStudentReportCardPdf(context, result)
                            if (pdfFile != null) {
                                com.example.util.PdfPaperGenerator.openPdfFile(context, pdfFile)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("export_result_pdf_button")
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PDF REPORT", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onHome,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("result_home_button")
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("DASHBOARD", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ResultRow(label: String, value: String, textColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = textColor))
    }
}
