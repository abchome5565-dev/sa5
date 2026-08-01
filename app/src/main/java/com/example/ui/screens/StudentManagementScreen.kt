package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.Student
import com.example.ui.theme.PrimaryDarkBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentManagementScreen(
    students: List<Student>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSaveStudent: (Student) -> Unit,
    onDeleteStudent: (Student) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var viewingStudent by remember { mutableStateOf<Student?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Management", fontWeight = FontWeight.Bold) },
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
                    editingStudent = null
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_student_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Student")
            }
        },
        modifier = Modifier.testTag("students_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Search by Name, Roll No, Class...") },
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
                    .testTag("student_search_bar")
            )

            if (students.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PersonOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No students added yet.\nTap + button to add a new student." else "No matching students found.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(students) { student ->
                        StudentCard(
                            student = student,
                            onClick = { viewingStudent = student },
                            onEdit = {
                                editingStudent = student
                                showAddDialog = true
                            },
                            onDelete = { onDeleteStudent(student) }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddDialog) {
        StudentFormDialog(
            student = editingStudent,
            onDismiss = { showAddDialog = false },
            onSave = { updated ->
                onSaveStudent(updated)
                showAddDialog = false
            }
        )
    }

    // Student Detail Dialog
    if (viewingStudent != null) {
        StudentDetailDialog(
            student = viewingStudent!!,
            onDismiss = { viewingStudent = null }
        )
    }
}

@Composable
fun StudentCard(
    student: Student,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_item_${student.id}"),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (!student.photoPath.isNullOrBlank()) {
                    coil.compose.AsyncImage(
                        model = student.photoPath,
                        contentDescription = "Student Photo",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = student.fullName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDarkBlue
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.fullName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Roll No: ${student.rollNumber} | ${student.className} (${student.section})",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Text(
                    text = "Phone: ${student.mobileNumber}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            IconButton(onClick = onClick) {
                Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF1976D2))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD32F2F))
            }
        }
    }
}

@Composable
fun StudentFormDialog(
    student: Student?,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit
) {
    var fullName by remember { mutableStateOf(student?.fullName ?: "") }
    var rollNumber by remember { mutableStateOf(student?.rollNumber ?: "") }
    var fatherName by remember { mutableStateOf(student?.fatherName ?: "") }
    var mobileNumber by remember { mutableStateOf(student?.mobileNumber ?: "") }
    var address by remember { mutableStateOf(student?.address ?: "") }
    var className by remember { mutableStateOf(student?.className ?: "Class 10") }
    var section by remember { mutableStateOf(student?.section ?: "A") }
    var admissionDate by remember { mutableStateOf(student?.admissionDate ?: "2026-01-01") }
    var monthlyFeeStr by remember { mutableStateOf(student?.monthlyFee?.toString() ?: "3500") }
    var photoPath by remember { mutableStateOf(student?.photoPath ?: "") }
    var notes by remember { mutableStateOf(student?.notes ?: "") }

    val photoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            photoPath = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (student == null) "Add New Student" else "Edit Student") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = photoPath,
                            onValueChange = { photoPath = it },
                            label = { Text("Photo URI / Path (Optional)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { photoPickerLauncher.launch("image/*") }) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Pick Photo")
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = rollNumber,
                        onValueChange = { rollNumber = it },
                        label = { Text("Roll Number *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = fatherName,
                        onValueChange = { fatherName = it },
                        label = { Text("Father Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it },
                        label = { Text("Mobile Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = className,
                            onValueChange = { className = it },
                            label = { Text("Class") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = section,
                            onValueChange = { section = it },
                            label = { Text("Section") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = monthlyFeeStr,
                        onValueChange = { monthlyFeeStr = it },
                        label = { Text("Monthly Fee (Rs.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isNotBlank() && rollNumber.isNotBlank()) {
                        val newStudent = Student(
                            id = student?.id ?: 0L,
                            fullName = fullName.trim(),
                            rollNumber = rollNumber.trim(),
                            fatherName = fatherName.trim(),
                            mobileNumber = mobileNumber.trim(),
                            address = address.trim(),
                            className = className.trim(),
                            section = section.trim(),
                            admissionDate = admissionDate,
                            monthlyFee = monthlyFeeStr.toDoubleOrNull() ?: 0.0,
                            photoPath = photoPath.ifBlank { null },
                            notes = notes.trim()
                        )
                        onSave(newStudent)
                    }
                },
                enabled = fullName.isNotBlank() && rollNumber.isNotBlank()
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

@Composable
fun StudentDetailDialog(
    student: Student,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(student.fullName, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Roll Number: ${student.rollNumber}", fontWeight = FontWeight.SemiBold)
                Text("Father Name: ${student.fatherName}")
                Text("Class: ${student.className} (Sec: ${student.section})")
                Text("Mobile Number: ${student.mobileNumber}")
                Text("Address: ${student.address}")
                Text("Admission Date: ${student.admissionDate}")
                Text("Monthly Fee: Rs. ${student.monthlyFee.toInt()}")
                if (student.notes.isNotBlank()) {
                    Text("Notes: ${student.notes}")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
