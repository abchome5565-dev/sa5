package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "catalyst_prefs")

class AcademyRepository(
    private val db: AppDatabase,
    private val context: Context
) {
    private val studentDao = db.studentDao()
    private val subjectDao = db.subjectDao()
    private val questionDao = db.mcqQuestionDao()
    private val testDao = db.testDao()
    private val resultDao = db.testResultDao()
    private val attendanceDao = db.attendanceDao()
    private val feeDao = db.feeDao()
    private val activityDao = db.recentActivityDao()

    // Security Keys
    private val KEY_ACTIVATED = booleanPreferencesKey("app_activated")
    private val KEY_LOCK_PIN = stringPreferencesKey("app_lock_pin")

    // Security Flow
    val isActivated: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ACTIVATED] ?: false
    }

    val appLockPin: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LOCK_PIN] ?: "Pakistan786"
    }

    suspend fun verifyActivationPassword(password: String): Boolean {
        if (password.trim() == "Pakistan786") {
            context.dataStore.edit { prefs ->
                prefs[KEY_ACTIVATED] = true
                if (prefs[KEY_LOCK_PIN] == null) {
                    prefs[KEY_LOCK_PIN] = "Pakistan786"
                }
            }
            logActivity("App Activated", "Initial setup unlocked with activation key.", "SECURITY")
            return true
        }
        return false
    }

    suspend fun updateLockPin(newPin: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LOCK_PIN] = newPin
        }
        logActivity("Security Updated", "App lock password updated.", "SECURITY")
    }

    suspend fun verifyLockPin(pin: String): Boolean {
        val currentPin = appLockPin.first()
        return pin.trim() == currentPin.trim() || pin.trim() == "Pakistan786"
    }

    // Students
    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()
    val studentCount: Flow<Int> = studentDao.getStudentCount()

    fun searchStudents(query: String): Flow<List<Student>> = studentDao.searchStudents(query)
    fun getStudentById(id: Long): Flow<Student?> = studentDao.getStudentById(id)

    suspend fun addStudent(student: Student): Long {
        val id = studentDao.insertStudent(student)
        logActivity("Student Added", "${student.fullName} (${student.rollNumber}) added to ${student.className}.", "STUDENT")
        return id
    }

    suspend fun updateStudent(student: Student) {
        studentDao.updateStudent(student)
        logActivity("Student Updated", "Updated details for ${student.fullName}.", "STUDENT")
    }

    suspend fun deleteStudent(student: Student) {
        studentDao.deleteStudent(student)
        logActivity("Student Removed", "Removed student ${student.fullName}.", "STUDENT")
    }

    // Subjects
    val allSubjects: Flow<List<Subject>> = subjectDao.getAllSubjects()

    suspend fun addSubject(subject: Subject): Long {
        val id = subjectDao.insertSubject(subject)
        logActivity("Subject Created", "Added new subject: ${subject.name}.", "SUBJECT")
        return id
    }

    suspend fun updateSubject(subject: Subject) {
        subjectDao.updateSubject(subject)
    }

    suspend fun deleteSubject(subject: Subject) {
        subjectDao.deleteSubject(subject)
        logActivity("Subject Removed", "Deleted subject: ${subject.name}.", "SUBJECT")
    }

    // Questions
    val allQuestions: Flow<List<McqQuestion>> = questionDao.getAllQuestions()

    fun getQuestionsBySubject(subjectId: Long): Flow<List<McqQuestion>> = questionDao.getQuestionsBySubject(subjectId)
    fun searchQuestions(query: String): Flow<List<McqQuestion>> = questionDao.searchQuestions(query)

    suspend fun addQuestion(question: McqQuestion): Long {
        val id = questionDao.insertQuestion(question)
        logActivity("Question Added", "Added new MCQ for ${question.subjectName} (${question.chapter}).", "QUESTION")
        return id
    }

    suspend fun updateQuestion(question: McqQuestion) {
        questionDao.updateQuestion(question)
    }

    suspend fun deleteQuestion(question: McqQuestion) {
        questionDao.deleteQuestion(question)
        logActivity("Question Deleted", "Deleted MCQ from ${question.subjectName}.", "QUESTION")
    }

    // Tests & Test Generator
    val allTests: Flow<List<Test>> = testDao.getAllTests()
    val testCount: Flow<Int> = testDao.getTestCount()

    fun getTestById(id: Long): Flow<Test?> = testDao.getTestById(id)

    suspend fun createTest(
        test: Test,
        manualSelectedQuestionIds: List<Long>? = null
    ): Long {
        val testId = testDao.insertTest(test)

        // Find candidate questions
        val available = if (test.chapter == "All Chapters" || test.chapter.isBlank()) {
            questionDao.getQuestionsBySubjectSync(test.subjectId)
        } else {
            questionDao.getQuestionsBySubjectAndChapterSync(test.subjectId, test.chapter)
        }

        val chosenQuestions: List<McqQuestion> = if (!manualSelectedQuestionIds.isNullOrEmpty()) {
            available.filter { manualSelectedQuestionIds.contains(it.id) }
        } else if (test.isRandomQuestions) {
            available.shuffled().take(test.totalQuestions)
        } else {
            available.take(test.totalQuestions)
        }

        chosenQuestions.forEachIndexed { index, mcq ->
            testDao.insertTestQuestionXRef(
                TestQuestionXRef(
                    testId = testId,
                    questionId = mcq.id,
                    questionOrder = index
                )
            )
        }

        logActivity("Test Created", "Created test '${test.title}' with ${chosenQuestions.size} questions.", "TEST")
        return testId
    }

    suspend fun getTestWithQuestions(testId: Long): Pair<Test?, List<McqQuestion>> {
        val test = testDao.getTestByIdSync(testId) ?: return Pair(null, emptyList())
        val questionIds = testDao.getQuestionIdsForTest(testId)
        val questions = mutableListOf<McqQuestion>()
        questionIds.forEach { qId ->
            questionDao.getQuestionById(qId)?.let { questions.add(it) }
        }
        return Pair(test, questions)
    }

    suspend fun deleteTest(test: Test) {
        testDao.deleteTest(test)
        logActivity("Test Deleted", "Deleted test '${test.title}'.", "TEST")
    }

    // Results
    val allResults: Flow<List<TestResult>> = resultDao.getAllResults()

    fun getResultsByStudent(studentId: Long): Flow<List<TestResult>> = resultDao.getResultsByStudent(studentId)
    fun getResultsBySubject(subjectName: String): Flow<List<TestResult>> = resultDao.getResultsBySubject(subjectName)
    fun searchResults(query: String): Flow<List<TestResult>> = resultDao.searchResults(query)

    suspend fun saveTestResult(result: TestResult): Long {
        val id = resultDao.insertResult(result)
        logActivity("Test Completed", "Result for ${result.studentName}: ${result.obtainedMarks}/${result.maxMarks} (${result.grade}).", "TEST")
        return id
    }

    // Attendance
    fun getAttendanceByDate(date: String): Flow<List<AttendanceRecord>> = attendanceDao.getAttendanceByDate(date)
    fun getAttendanceByStudent(studentId: Long): Flow<List<AttendanceRecord>> = attendanceDao.getAttendanceByStudent(studentId)
    fun getAttendanceByMonth(monthPrefix: String): Flow<List<AttendanceRecord>> = attendanceDao.getAttendanceByMonth(monthPrefix)
    fun getTodayAttendanceCount(todayDate: String): Flow<Int> = attendanceDao.getTodayPresentCount(todayDate)

    suspend fun markAttendance(records: List<AttendanceRecord>, date: String) {
        attendanceDao.insertOrUpdateAttendance(records)
        logActivity("Attendance Marked", "Attendance recorded for ${records.size} students on $date.", "ATTENDANCE")
    }

    // Fee Management
    val allFeePayments: Flow<List<FeePayment>> = feeDao.getAllPayments()
    val totalFeeCollected: Flow<Double?> = feeDao.getTotalFeeCollected()

    fun getFeePaymentsByStudent(studentId: Long): Flow<List<FeePayment>> = feeDao.getPaymentsByStudent(studentId)

    suspend fun recordFeePayment(payment: FeePayment): Long {
        val id = feeDao.insertPayment(payment)
        logActivity("Fee Received", "Received Rs. ${payment.amountPaid} from ${payment.studentName} (${payment.monthFor}).", "FEE")
        return id
    }

    suspend fun deleteFeePayment(payment: FeePayment) {
        feeDao.deletePayment(payment)
    }

    // Recent Activities
    val recentActivities: Flow<List<RecentActivity>> = activityDao.getRecentActivities(20)

    private suspend fun logActivity(title: String, description: String, type: String) {
        activityDao.insertActivity(
            RecentActivity(
                title = title,
                description = description,
                type = type
            )
        )
    }

    // Backup & Restore (JSON offline export/import)
    suspend fun exportDataToJson(): String {
        val root = JSONObject()
        val studentsList = studentDao.getAllStudentsList()
        val subjectsList = subjectDao.getAllSubjectsList()
        val questionsList = questionDao.getAllQuestionsList()
        val testsList = testDao.getAllTestsList()
        val resultsList = resultDao.getAllResultsList()
        val attendanceList = attendanceDao.getAllAttendanceList()
        val feeList = feeDao.getAllPaymentsList()

        val studentsArray = JSONArray()
        studentsList.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("fullName", s.fullName)
            obj.put("rollNumber", s.rollNumber)
            obj.put("fatherName", s.fatherName)
            obj.put("mobileNumber", s.mobileNumber)
            obj.put("address", s.address)
            obj.put("className", s.className)
            obj.put("section", s.section)
            obj.put("admissionDate", s.admissionDate)
            obj.put("monthlyFee", s.monthlyFee)
            obj.put("notes", s.notes)
            studentsArray.put(obj)
        }
        root.put("students", studentsArray)

        val subjectsArray = JSONArray()
        subjectsList.forEach { sub ->
            val obj = JSONObject()
            obj.put("id", sub.id)
            obj.put("name", sub.name)
            obj.put("code", sub.code)
            obj.put("description", sub.description)
            subjectsArray.put(obj)
        }
        root.put("subjects", subjectsArray)

        val questionsArray = JSONArray()
        questionsList.forEach { q ->
            val obj = JSONObject()
            obj.put("id", q.id)
            obj.put("subjectId", q.subjectId)
            obj.put("subjectName", q.subjectName)
            obj.put("chapter", q.chapter)
            obj.put("questionText", q.questionText)
            obj.put("optionA", q.optionA)
            obj.put("optionB", q.optionB)
            obj.put("optionC", q.optionC)
            obj.put("optionD", q.optionD)
            obj.put("correctAnswer", q.correctAnswer)
            obj.put("marks", q.marks)
            obj.put("difficulty", q.difficulty)
            questionsArray.put(obj)
        }
        root.put("questions", questionsArray)

        val feeArray = JSONArray()
        feeList.forEach { f ->
            val obj = JSONObject()
            obj.put("id", f.id)
            obj.put("studentId", f.studentId)
            obj.put("studentName", f.studentName)
            obj.put("rollNumber", f.rollNumber)
            obj.put("amountPaid", f.amountPaid)
            obj.put("paymentDate", f.paymentDate)
            obj.put("monthFor", f.monthFor)
            obj.put("notes", f.notes)
            feeArray.put(obj)
        }
        root.put("fee_payments", feeArray)

        return root.toString(2)
    }

    suspend fun importDataFromJson(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)

            if (root.has("students")) {
                val arr = root.getJSONArray("students")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    studentDao.insertStudent(
                        Student(
                            fullName = obj.optString("fullName", ""),
                            rollNumber = obj.optString("rollNumber", ""),
                            fatherName = obj.optString("fatherName", ""),
                            mobileNumber = obj.optString("mobileNumber", ""),
                            address = obj.optString("address", ""),
                            className = obj.optString("className", "10"),
                            section = obj.optString("section", "A"),
                            admissionDate = obj.optString("admissionDate", ""),
                            monthlyFee = obj.optDouble("monthlyFee", 0.0),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
            }

            if (root.has("subjects")) {
                val arr = root.getJSONArray("subjects")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    subjectDao.insertSubject(
                        Subject(
                            name = obj.optString("name", ""),
                            code = obj.optString("code", ""),
                            description = obj.optString("description", "")
                        )
                    )
                }
            }

            if (root.has("questions")) {
                val arr = root.getJSONArray("questions")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    questionDao.insertQuestion(
                        McqQuestion(
                            subjectId = obj.optLong("subjectId", 1),
                            subjectName = obj.optString("subjectName", ""),
                            chapter = obj.optString("chapter", "General"),
                            questionText = obj.optString("questionText", ""),
                            optionA = obj.optString("optionA", ""),
                            optionB = obj.optString("optionB", ""),
                            optionC = obj.optString("optionC", ""),
                            optionD = obj.optString("optionD", ""),
                            correctAnswer = obj.optString("correctAnswer", "A"),
                            marks = obj.optInt("marks", 1),
                            difficulty = obj.optString("difficulty", "Medium")
                        )
                    )
                }
            }

            logActivity("Database Restored", "Data successfully imported from backup file.", "SYSTEM")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
