package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.McqQuestion
import com.example.data.Subject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionBankScreen(
    questions: List<McqQuestion>,
    subjects: List<Subject>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSaveQuestion: (McqQuestion) -> Unit,
    onDeleteQuestion: (McqQuestion) -> Unit,
    onBack: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingMcq by remember { mutableStateOf<McqQuestion?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Question Bank (MCQs)", fontWeight = FontWeight.Bold) },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingMcq = null
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_mcq_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add MCQ")
            }
        },
        modifier = Modifier.testTag("questions_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Search Question Text, Subject, Chapter...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("mcq_search_bar")
            )

            if (questions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No questions in question bank.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(questions) { mcq ->
                        QuestionCard(
                            mcq = mcq,
                            onEdit = {
                                editingMcq = mcq
                                showDialog = true
                            },
                            onDelete = { onDeleteQuestion(mcq) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        QuestionFormDialog(
            mcq = editingMcq,
            subjects = subjects,
            onDismiss = { showDialog = false },
            onSave = { updated ->
                onSaveQuestion(updated)
                showDialog = false
            }
        )
    }
}

@Composable
fun QuestionCard(
    mcq: McqQuestion,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mcq_item_${mcq.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${mcq.subjectName} | ${mcq.chapter}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF1976D2))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD32F2F))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = mcq.questionText,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            if (!mcq.imagePath.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    coil.compose.AsyncImage(
                        model = mcq.imagePath,
                        contentDescription = "Question Image",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OptionText(label = "(A) ${mcq.optionA}", isCorrect = mcq.correctAnswer == "A")
                OptionText(label = "(B) ${mcq.optionB}", isCorrect = mcq.correctAnswer == "B")
                OptionText(label = "(C) ${mcq.optionC}", isCorrect = mcq.correctAnswer == "C")
                OptionText(label = "(D) ${mcq.optionD}", isCorrect = mcq.correctAnswer == "D")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Correct Answer: [${mcq.correctAnswer}]",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = "Marks: ${mcq.marks} | Difficulty: ${mcq.difficulty}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun OptionText(label: String, isCorrect: Boolean) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = if (isCorrect) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionFormDialog(
    mcq: McqQuestion?,
    subjects: List<Subject>,
    onDismiss: () -> Unit,
    onSave: (McqQuestion) -> Unit
) {
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull { it.id == mcq?.subjectId } ?: subjects.firstOrNull()) }
    var subjectExpanded by remember { mutableStateOf(false) }

    var chapter by remember { mutableStateOf(mcq?.chapter ?: "Chapter 1") }
    var questionText by remember { mutableStateOf(mcq?.questionText ?: "") }
    var optionA by remember { mutableStateOf(mcq?.optionA ?: "") }
    var optionB by remember { mutableStateOf(mcq?.optionB ?: "") }
    var optionC by remember { mutableStateOf(mcq?.optionC ?: "") }
    var optionD by remember { mutableStateOf(mcq?.optionD ?: "") }
    var correctAnswer by remember { mutableStateOf(mcq?.correctAnswer ?: "A") }
    var marksStr by remember { mutableStateOf(mcq?.marks?.toString() ?: "1") }
    var difficulty by remember { mutableStateOf(mcq?.difficulty ?: "Medium") }
    var imagePath by remember { mutableStateOf(mcq?.imagePath ?: "") }

    val questionImagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imagePath = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (mcq == null) "Add New Question" else "Edit Question") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
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
                }

                item {
                    OutlinedTextField(
                        value = chapter,
                        onValueChange = { chapter = it },
                        label = { Text("Chapter / Topic *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = questionText,
                        onValueChange = { questionText = it },
                        label = { Text("Question Text *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = imagePath,
                            onValueChange = { imagePath = it },
                            label = { Text("Question Image URI / Path (Optional)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { questionImagePickerLauncher.launch("image/*") }) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Pick Image")
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = optionA,
                        onValueChange = { optionA = it },
                        label = { Text("Option A *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = optionB,
                        onValueChange = { optionB = it },
                        label = { Text("Option B *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = optionC,
                        onValueChange = { optionC = it },
                        label = { Text("Option C *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = optionD,
                        onValueChange = { optionD = it },
                        label = { Text("Option D *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Correct Answer *", style = MaterialTheme.typography.labelMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("A", "B", "C", "D").forEach { opt ->
                            FilterChip(
                                selected = correctAnswer == opt,
                                onClick = { correctAnswer = opt },
                                label = { Text("Option $opt") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = marksStr,
                            onValueChange = { marksStr = it },
                            label = { Text("Marks") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = difficulty,
                            onValueChange = { difficulty = it },
                            label = { Text("Difficulty") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedSubject != null && questionText.isNotBlank() && optionA.isNotBlank() && optionB.isNotBlank()) {
                        val newMcq = McqQuestion(
                            id = mcq?.id ?: 0L,
                            subjectId = selectedSubject!!.id,
                            subjectName = selectedSubject!!.name,
                            chapter = chapter.trim(),
                            questionText = questionText.trim(),
                            imagePath = imagePath.ifBlank { null },
                            optionA = optionA.trim(),
                            optionB = optionB.trim(),
                            optionC = optionC.trim(),
                            optionD = optionD.trim(),
                            correctAnswer = correctAnswer,
                            marks = marksStr.toIntOrNull() ?: 1,
                            difficulty = difficulty.trim()
                        )
                        onSave(newMcq)
                    }
                },
                enabled = selectedSubject != null && questionText.isNotBlank() && optionA.isNotBlank() && optionB.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
