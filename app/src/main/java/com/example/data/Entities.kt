package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val rollNumber: String,
    val fatherName: String,
    val mobileNumber: String,
    val address: String,
    val className: String,
    val section: String,
    val admissionDate: String,
    val monthlyFee: Double,
    val photoPath: String? = null,
    val notes: String = ""
)

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val code: String = "",
    val description: String = ""
)

@Entity(tableName = "questions")
data class McqQuestion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val subjectName: String = "",
    val chapter: String,
    val questionText: String,
    val imagePath: String? = null,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String, // "A", "B", "C", "D"
    val marks: Int = 1,
    val difficulty: String = "Medium" // "Easy", "Medium", "Hard"
)

@Entity(tableName = "tests")
data class Test(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subjectId: Long,
    val subjectName: String,
    val className: String,
    val chapter: String = "All Chapters",
    val timeLimitMinutes: Int = 30,
    val totalQuestions: Int = 10,
    val passingMarks: Int = 5,
    val isRandomQuestions: Boolean = true,
    val isRandomOptions: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "test_questions", primaryKeys = ["testId", "questionId"])
data class TestQuestionXRef(
    val testId: Long,
    val questionId: Long,
    val questionOrder: Int = 0
)

@Entity(tableName = "test_results")
data class TestResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val testId: Long,
    val studentId: Long? = null,
    val studentName: String,
    val rollNumber: String = "",
    val subjectName: String,
    val className: String = "",
    val totalQuestions: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val obtainedMarks: Int,
    val maxMarks: Int,
    val percentage: Double,
    val grade: String, // "A+", "A", "B", "C", "F"
    val timeUsedSeconds: Int,
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "attendance")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val studentName: String,
    val rollNumber: String,
    val className: String,
    val section: String,
    val date: String, // "YYYY-MM-DD"
    val status: String // "PRESENT", "ABSENT", "LEAVE", "LATE"
)

@Entity(tableName = "fee_payments")
data class FeePayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val studentName: String,
    val rollNumber: String,
    val amountPaid: Double,
    val paymentDate: String, // "YYYY-MM-DD"
    val monthFor: String, // e.g. "July 2026"
    val notes: String = ""
)

@Entity(tableName = "activities")
data class RecentActivity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String // "STUDENT", "TEST", "ATTENDANCE", "FEE", "SUBJECT", "QUESTION"
)
