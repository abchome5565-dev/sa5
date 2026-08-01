package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.McqQuestion
import com.example.data.Subject
import com.example.data.Test
import com.example.util.PdfPaperGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestCreatorScreen(
    subjects: List<Subject>,
    allQuestions: List<McqQuestion>,
    onCreateTest: (Test, List<Long>?) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("Monthly Quiz") }
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull()) }
    var subjectExpanded by remember { mutableStateOf(false) }

    var className by remember { mutableStateOf("Class 10") }
    var chapter by remember { mutableStateOf("All Chapters") }
    var timeLimitMinutesStr by remember { mutableStateOf("30") }
    var totalQuestionsStr by remember { mutableStateOf("10") }
    var passingMarksStr by remember { mutableStateOf("5") }

    var isRandomQuestions by remember { mutableStateOf(true) }
    var isRandomOptions by remember { mutableStateOf(true) }
    var isManualSelection by remember { mutableStateOf(false) }

    val selectedManualIds = remember { mutableStateListOf<Long>() }

    // Filtered questions for manual selection
    val availableQuestions = remember(selectedSubject, chapter, allQuestions) {
        allQuestions.filter { q ->
            (selectedSubject == null || q.subjectId == selectedSubject?.id) &&
                    (chapter == "All Chapters" || chapter.isBlank() || q.chapter.contains(chapter, ignoreCase = true))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test Creator", fontWeight = FontWeight.Bold) },
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
        modifier = Modifier.testTag("test_creator_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Test Configuration",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Test Title *") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_title_input")
                        )

                        ExposedDropdownMenuBox(
                            expanded = subjectExpanded,
                            onExpandedChange = { subjectExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedSubject?.name ?: "Select Subject",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Subject *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = subjectExpanded,
                                onDismissRequest = { subjectExpanded = false }
                            ) {
                                subjects.forEach { sub ->
                                    DropdownMenuItem(
                                        text = { Text(sub.name) },
                                        onClick = {
                                            selectedSubject = sub
                                            subjectExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = className,
                                onValueChange = { className = it },
                                label = { Text("Class") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = chapter,
                                onValueChange = { chapter = it },
                                label = { Text("Chapter") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = timeLimitMinutesStr,
                                onValueChange = { timeLimitMinutesStr = it },
                                label = { Text("Time Limit (Mins)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = totalQuestionsStr,
                                onValueChange = { totalQuestionsStr = it },
                                label = { Text("Total Questions") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = passingMarksStr,
                            onValueChange = { passingMarksStr = it },
                            label = { Text("Passing Marks") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Options Switch Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Randomization & Selection",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Shuffle Questions Randomly")
                            Switch(checked = isRandomQuestions, onCheckedChange = { isRandomQuestions = it })
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Shuffle MCQ Options")
                            Switch(checked = isRandomOptions, onCheckedChange = { isRandomOptions = it })
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Manual Question Selection")
                            Switch(checked = isManualSelection, onCheckedChange = { isManualSelection = it })
                        }
                    }
                }
            }

            if (isManualSelection) {
                item {
                    Text(
                        text = "Select Questions (${selectedManualIds.size} Selected)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                if (availableQuestions.isEmpty()) {
                    item {
                        Text("No available MCQs for this subject.", color = Color.Gray)
                    }
                } else {
                    items(availableQuestions) { mcq ->
                        val isChecked = selectedManualIds.contains(mcq.id)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedManualIds.add(mcq.id)
                                        else selectedManualIds.remove(mcq.id)
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(mcq.questionText, fontWeight = FontWeight.SemiBold)
                                    Text("${mcq.subjectName} | ${mcq.chapter}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Button(
                    onClick = {
                        if (selectedSubject != null && title.isNotBlank()) {
                            val newTest = Test(
                                title = title.trim(),
                                subjectId = selectedSubject!!.id,
                                subjectName = selectedSubject!!.name,
                                className = className.trim(),
                                chapter = chapter.trim(),
                                timeLimitMinutes = timeLimitMinutesStr.toIntOrNull() ?: 30,
                                totalQuestions = if (isManualSelection) selectedManualIds.size else (totalQuestionsStr.toIntOrNull() ?: 10),
                                passingMarks = passingMarksStr.toIntOrNull() ?: 5,
                                isRandomQuestions = isRandomQuestions,
                                isRandomOptions = isRandomOptions
                            )
                            onCreateTest(
                                newTest,
                                if (isManualSelection) selectedManualIds.toList() else null
                            )
                        }
                    },
                    enabled = selectedSubject != null && title.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_test_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SAVE TEST TO DATABASE", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
