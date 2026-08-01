package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AttendanceRecord
import com.example.data.Student

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    students: List<Student>,
    savedRecords: List<AttendanceRecord>,
    selectedDate: String,
    onDateChange: (String) -> Unit,
    onSaveAttendance: (List<AttendanceRecord>) -> Unit,
    onBack: () -> Unit
) {
    // Map studentId -> Status ("PRESENT", "ABSENT", "LEAVE", "LATE")
    val attendanceMap = remember { mutableStateMapOf<Long, String>() }

    LaunchedEffect(students, savedRecords) {
        attendanceMap.clear()
        // Default to PRESENT for all students unless saved record exists
        students.forEach { s ->
            val existing = savedRecords.firstOrNull { it.studentId == s.id }
            attendanceMap[s.id] = existing?.status ?: "PRESENT"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Attendance", fontWeight = FontWeight.Bold) },
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
                    val records = students.map { s ->
                        AttendanceRecord(
                            studentId = s.id,
                            studentName = s.fullName,
                            rollNumber = s.rollNumber,
                            className = s.className,
                            section = s.section,
                            date = selectedDate,
                            status = attendanceMap[s.id] ?: "PRESENT"
                        )
                    }
                    onSaveAttendance(records)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("save_attendance_fab")
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save Attendance")
            }
        },
        modifier = Modifier.testTag("attendance_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Date Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Date: $selectedDate",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Button(
                        onClick = {
                            students.forEach { s -> attendanceMap[s.id] = "PRESENT" }
                        }
                    ) {
                        Text("ALL PRESENT")
                    }
                }
            }

            if (students.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No students found. Add students first.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(students) { student ->
                        val currentStatus = attendanceMap[student.id] ?: "PRESENT"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(student.fullName, fontWeight = FontWeight.Bold)
                                        Text("Roll No: ${student.rollNumber} | ${student.className}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    AttendanceChip("PRESENT", "P", currentStatus == "PRESENT", Color(0xFF2E7D32)) {
                                        attendanceMap[student.id] = "PRESENT"
                                    }
                                    AttendanceChip("ABSENT", "A", currentStatus == "ABSENT", Color(0xFFD32F2F)) {
                                        attendanceMap[student.id] = "ABSENT"
                                    }
                                    AttendanceChip("LEAVE", "L", currentStatus == "LEAVE", Color(0xFFF57C00)) {
                                        attendanceMap[student.id] = "LEAVE"
                                    }
                                    AttendanceChip("LATE", "Late", currentStatus == "LATE", Color(0xFF7B1FA2)) {
                                        attendanceMap[student.id] = "LATE"
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

@Composable
fun RowScope.AttendanceChip(
    statusKey: String,
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label, fontWeight = FontWeight.Bold) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color,
            selectedLabelColor = Color.White
        ),
        modifier = Modifier.weight(1f)
    )
}
