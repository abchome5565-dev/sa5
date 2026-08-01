package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Student::class,
        Subject::class,
        McqQuestion::class,
        Test::class,
        TestQuestionXRef::class,
        TestResult::class,
        AttendanceRecord::class,
        FeePayment::class,
        RecentActivity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao
    abstract fun subjectDao(): SubjectDao
    abstract fun mcqQuestionDao(): McqQuestionDao
    abstract fun testDao(): TestDao
    abstract fun testResultDao(): TestResultDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun feeDao(): FeeDao
    abstract fun recentActivityDao(): RecentActivityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "catalyst_academy.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            // Seed Default Subjects
            val defaultSubjects = listOf(
                Subject(name = "English", code = "ENG-101", description = "Grammar, Literature & Comprehension"),
                Subject(name = "Urdu", code = "URD-101", description = "Urdu Grammar & Literature"),
                Subject(name = "Mathematics", code = "MATH-101", description = "Algebra, Geometry & Arithmetic"),
                Subject(name = "Science", code = "SCI-101", description = "General Science Concepts"),
                Subject(name = "Physics", code = "PHY-101", description = "Mechanics, Electricity & Thermodynamics"),
                Subject(name = "Chemistry", code = "CHEM-101", description = "Organic & Inorganic Chemistry"),
                Subject(name = "Biology", code = "BIO-101", description = "Human Anatomy & Cell Biology"),
                Subject(name = "Computer", code = "CS-101", description = "Computer Fundamentals & Programming")
            )

            for (subject in defaultSubjects) {
                val subId = db.subjectDao().insertSubject(subject)

                // Seed initial sample questions for Math & Physics & English & Computer
                when (subject.name) {
                    "Mathematics" -> {
                        db.mcqQuestionDao().insertQuestion(
                            McqQuestion(
                                subjectId = subId,
                                subjectName = "Mathematics",
                                chapter = "Algebra",
                                questionText = "What is the value of x if 2x + 6 = 14?",
                                optionA = "x = 2",
                                optionB = "x = 4",
                                optionC = "x = 6",
                                optionD = "x = 8",
                                correctAnswer = "B",
                                marks = 1,
                                difficulty = "Easy"
                            )
                        )
                        db.mcqQuestionDao().insertQuestion(
                            McqQuestion(
                                subjectId = subId,
                                subjectName = "Mathematics",
                                chapter = "Geometry",
                                questionText = "What is the sum of angles in a triangle?",
                                optionA = "90 degrees",
                                optionB = "180 degrees",
                                optionC = "360 degrees",
                                optionD = "270 degrees",
                                correctAnswer = "B",
                                marks = 1,
                                difficulty = "Easy"
                            )
                        )
                        db.mcqQuestionDao().insertQuestion(
                            McqQuestion(
                                subjectId = subId,
                                subjectName = "Mathematics",
                                chapter = "Algebra",
                                questionText = "What is (a + b)^2 equal to?",
                                optionA = "a^2 + b^2",
                                optionB = "a^2 + 2ab + b^2",
                                optionC = "a^2 - 2ab + b^2",
                                optionD = "2a + 2b",
                                correctAnswer = "B",
                                marks = 2,
                                difficulty = "Medium"
                            )
                        )
                    }
                    "Physics" -> {
                        db.mcqQuestionDao().insertQuestion(
                            McqQuestion(
                                subjectId = subId,
                                subjectName = "Physics",
                                chapter = "Mechanics",
                                questionText = "What is SI unit of Force?",
                                optionA = "Joule",
                                optionB = "Watt",
                                optionC = "Newton",
                                optionD = "Pascal",
                                correctAnswer = "C",
                                marks = 1,
                                difficulty = "Easy"
                            )
                        )
                        db.mcqQuestionDao().insertQuestion(
                            McqQuestion(
                                subjectId = subId,
                                subjectName = "Physics",
                                chapter = "Kinematics",
                                questionText = "What is the acceleration due to gravity on Earth surface?",
                                optionA = "9.8 m/s^2",
                                optionB = "10.8 m/s^2",
                                optionC = "8.9 m/s^2",
                                optionD = "12 m/s^2",
                                correctAnswer = "A",
                                marks = 1,
                                difficulty = "Easy"
                            )
                        )
                    }
                    "Computer" -> {
                        db.mcqQuestionDao().insertQuestion(
                            McqQuestion(
                                subjectId = subId,
                                subjectName = "Computer",
                                chapter = "Fundamentals",
                                questionText = "Which component is known as the Brain of the Computer?",
                                optionA = "RAM",
                                optionB = "Hard Disk",
                                optionC = "CPU",
                                optionD = "Power Supply",
                                correctAnswer = "C",
                                marks = 1,
                                difficulty = "Easy"
                            )
                        )
                        db.mcqQuestionDao().insertQuestion(
                            McqQuestion(
                                subjectId = subId,
                                subjectName = "Computer",
                                chapter = "Networking",
                                questionText = "What does LAN stand for?",
                                optionA = "Local Access Network",
                                optionB = "Local Area Network",
                                optionC = "Logical Array Node",
                                optionD = "Link Access Number",
                                correctAnswer = "B",
                                marks = 1,
                                difficulty = "Easy"
                            )
                        )
                    }
                }
            }

            // Seed sample Students
            val student1 = Student(
                fullName = "Muhammad Ali",
                rollNumber = "CA-101",
                fatherName = "Tariq Mahmood",
                mobileNumber = "03001234567",
                address = "Main Street, Lahore",
                className = "Class 10",
                section = "A",
                admissionDate = "2026-01-10",
                monthlyFee = 3500.0,
                notes = "Honor student"
            )
            val student2 = Student(
                fullName = "Ayesha Khan",
                rollNumber = "CA-102",
                fatherName = "Riaz Khan",
                mobileNumber = "03129876543",
                address = "Model Town, Lahore",
                className = "Class 10",
                section = "A",
                admissionDate = "2026-01-15",
                monthlyFee = 3500.0,
                notes = "Top scorer in Mathematics"
            )
            val id1 = db.studentDao().insertStudent(student1)
            val id2 = db.studentDao().insertStudent(student2)

            // Seed initial Fee Payment
            db.feeDao().insertPayment(
                FeePayment(
                    studentId = id1,
                    studentName = "Muhammad Ali",
                    rollNumber = "CA-101",
                    amountPaid = 3500.0,
                    paymentDate = "2026-07-01",
                    monthFor = "July 2026",
                    notes = "Paid via cash"
                )
            )

            // Seed initial activity
            db.recentActivityDao().insertActivity(
                RecentActivity(
                    title = "System Setup Complete",
                    description = "Catalyst Academy initialized with default subjects and students.",
                    type = "SYSTEM"
                )
            )
        }
    }
}
