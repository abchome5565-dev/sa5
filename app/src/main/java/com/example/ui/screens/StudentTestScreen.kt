package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.McqQuestion
import com.example.data.Test
import com.example.data.TestResult
import com.example.ui.theme.PrimaryDarkBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestListScreen(
    tests: List<Test>,
    onStartTest: (Long) -> Unit,
    onGeneratePdf: (Test) -> Unit,
    onDeleteTest: (Test) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Available Tests", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        modifier = Modifier.testTag("test_list_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (tests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tests created yet.\nCreate a test using Test Creator.", textAlign = TextAlign.Center, color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tests) { test ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_item_${test.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = test.title,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Subject: ${test.subjectName} | Class: ${test.className}",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Text(
                                    text = "Questions: ${test.totalQuestions} | Time: ${test.timeLimitMinutes} Mins | Pass: ${test.passingMarks}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = { onStartTest(test.id) },
                                        modifier = Modifier.weight(1f).testTag("start_test_button_${test.id}")
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("START TEST")
                                    }

                                    OutlinedButton(
                                        onClick = { onGeneratePdf(test) }
                                    ) {
                                        Text("PDF PAPER")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentTestScreen(
    test: Test,
    questions: List<McqQuestion>,
    currentIndex: Int,
    userAnswers: Map<Int, String>,
    secondsLeft: Int,
    onSelectAnswer: (Int, String) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit
) {
    var studentNameInput by remember { mutableStateOf("") }
    var showNamePrompt by remember { mutableStateOf(true) }
    var showConfirmSubmit by remember { mutableStateOf(false) }

    if (showNamePrompt) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Enter Student Name") },
            text = {
                OutlinedTextField(
                    value = studentNameInput,
                    onValueChange = { studentNameInput = it },
                    label = { Text("Full Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_name_test_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = { showNamePrompt = false },
                    enabled = studentNameInput.isNotBlank()
                ) {
                    Text("START EXAMINATION")
                }
            },
            dismissButton = {
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
            }
        )
    }

    val currentMcq = questions.getOrNull(currentIndex)
    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val timerStr = String.format("%02d:%02d", minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(test.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Surface(
                            color = if (secondsLeft < 120) Color(0xFFFFEBEE) else Color(0xFFE3F2FD),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.HourglassTop,
                                    contentDescription = "Timer",
                                    tint = if (secondsLeft < 120) Color.Red else PrimaryDarkBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = timerStr,
                                    fontWeight = FontWeight.Bold,
                                    color = if (secondsLeft < 120) Color.Red else PrimaryDarkBlue,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onPrevious,
                        enabled = currentIndex > 0
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PREV")
                    }

                    Button(
                        onClick = { showConfirmSubmit = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.testTag("submit_test_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SUBMIT")
                    }

                    Button(
                        onClick = onNext,
                        enabled = currentIndex < questions.size - 1
                    ) {
                        Text("NEXT")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        },
        modifier = Modifier.testTag("student_test_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Progress Indicator Bar
            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / questions.size.coerceAtLeast(1) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Question ${currentIndex + 1} of ${questions.size}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Candidate: $studentNameInput",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (currentMcq != null) {
                var showZoomDialog by remember { mutableStateOf(false) }

                if (showZoomDialog && !currentMcq.imagePath.isNullOrBlank()) {
                    AlertDialog(
                        onDismissRequest = { showZoomDialog = false },
                        title = { Text("Question Image (Zoom)") },
                        text = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                coil.compose.AsyncImage(
                                    model = currentMcq.imagePath,
                                    contentDescription = "Zoomed Question Image",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        },
                        confirmButton = {
                            Button(onClick = { showZoomDialog = false }) {
                                Text("Close")
                            }
                        }
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = currentMcq.questionText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )

                        if (!currentMcq.imagePath.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clickable { showZoomDialog = true }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    coil.compose.AsyncImage(
                                        model = currentMcq.imagePath,
                                        contentDescription = "Question Image",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Text(
                                        "Tap to Zoom",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(8.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Answer Option Choices
                        val selectedOpt = userAnswers[currentIndex]

                        OptionChoiceRow(
                            optionCode = "A",
                            optionText = currentMcq.optionA,
                            isSelected = selectedOpt == "A",
                            onSelect = { onSelectAnswer(currentIndex, "A") }
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OptionChoiceRow(
                            optionCode = "B",
                            optionText = currentMcq.optionB,
                            isSelected = selectedOpt == "B",
                            onSelect = { onSelectAnswer(currentIndex, "B") }
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OptionChoiceRow(
                            optionCode = "C",
                            optionText = currentMcq.optionC,
                            isSelected = selectedOpt == "C",
                            onSelect = { onSelectAnswer(currentIndex, "C") }
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OptionChoiceRow(
                            optionCode = "D",
                            optionText = currentMcq.optionD,
                            isSelected = selectedOpt == "D",
                            onSelect = { onSelectAnswer(currentIndex, "D") }
                        )
                    }
                }
            }
        }
    }

    if (showConfirmSubmit) {
        AlertDialog(
            onDismissRequest = { showConfirmSubmit = false },
            title = { Text("Submit Test?") },
            text = { Text("Are you sure you want to finalize and submit your test now?") },
            confirmButton = {
                Button(onClick = {
                    showConfirmSubmit = false
                    onSubmit(studentNameInput.ifBlank { "Student" })
                }) {
                    Text("YES, SUBMIT")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmSubmit = false }) {
                    Text("CONTINUE TEST")
                }
            }
        )
    }
}

@Composable
fun OptionChoiceRow(
    optionCode: String,
    optionText: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, PrimaryDarkBlue) else null,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .testTag("option_choice_$optionCode")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) PrimaryDarkBlue else Color.Gray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = optionCode,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = optionText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) PrimaryDarkBlue else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}
