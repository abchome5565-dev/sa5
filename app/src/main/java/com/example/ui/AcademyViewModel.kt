package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.util.PdfPaperGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AcademyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = AcademyRepository(db, application)

    // Security Flow
    val isActivated: StateFlow<Boolean> = repository.isActivated.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private val _isAppLocked = MutableStateFlow(true)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _securityError = MutableStateFlow<String?>(null)
    val securityError: StateFlow<String?> = _securityError.asStateFlow()

    // Dashboard State
    val totalStudents = repository.studentCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val totalTests = repository.testCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val totalFeeCollected = repository.totalFeeCollected.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val recentActivities = repository.recentActivities.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayStr: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val todayAttendanceCount = repository.getTodayAttendanceCount(todayStr).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Student Search & List
    private val _studentSearchQuery = MutableStateFlow("")
    val studentSearchQuery: StateFlow<String> = _studentSearchQuery.asStateFlow()

    val studentsList: StateFlow<List<Student>> = _studentSearchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.allStudents
            else repository.searchStudents(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Subjects
    val subjectsList = repository.allSubjects.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // MCQ Questions
    private val _mcqSearchQuery = MutableStateFlow("")
    val mcqSearchQuery: StateFlow<String> = _mcqSearchQuery.asStateFlow()

    val mcqList: StateFlow<List<McqQuestion>> = _mcqSearchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.allQuestions
            else repository.searchQuestions(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tests
    val testsList = repository.allTests.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Test Execution State
    private val _activeTest = MutableStateFlow<Test?>(null)
    val activeTest: StateFlow<Test?> = _activeTest.asStateFlow()

    private val _activeQuestions = MutableStateFlow<List<McqQuestion>>(emptyList())
    val activeQuestions: StateFlow<List<McqQuestion>> = _activeQuestions.asStateFlow()

    private val _userAnswers = MutableStateFlow<Map<Int, String>>(emptyMap()) // Question Index -> Chosen Option ("A", "B", "C", "D")
    val userAnswers: StateFlow<Map<Int, String>> = _userAnswers.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _timerSecondsLeft = MutableStateFlow(0)
    val timerSecondsLeft: StateFlow<Int> = _timerSecondsLeft.asStateFlow()

    private val _activeTestResult = MutableStateFlow<TestResult?>(null)
    val activeTestResult: StateFlow<TestResult?> = _activeTestResult.asStateFlow()

    private var timerJob: Job? = null

    // Attendance State
    private val _selectedAttendanceDate = MutableStateFlow(todayStr)
    val selectedAttendanceDate: StateFlow<String> = _selectedAttendanceDate.asStateFlow()

    val attendanceForSelectedDate = _selectedAttendanceDate
        .flatMapLatest { date -> repository.getAttendanceByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Fee Payments
    val feePaymentsList = repository.allFeePayments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Results & Reports
    private val _resultSearchQuery = MutableStateFlow("")
    val resultSearchQuery: StateFlow<String> = _resultSearchQuery.asStateFlow()

    val resultsList = _resultSearchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.allResults
            else repository.searchResults(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Message Toast/Banner
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    // Security Actions
    fun submitActivationPassword(pass: String) {
        viewModelScope.launch {
            val success = repository.verifyActivationPassword(pass)
            if (success) {
                _isAppLocked.value = false
                _securityError.value = null
            } else {
                _securityError.value = "Incorrect Activation Password! Use 'Pakistan786'."
            }
        }
    }

    fun submitAppLockPin(pin: String) {
        viewModelScope.launch {
            val success = repository.verifyLockPin(pin)
            if (success) {
                _isAppLocked.value = false
                _securityError.value = null
            } else {
                _securityError.value = "Incorrect App Password!"
            }
        }
    }

    fun updateLockPin(newPin: String) {
        viewModelScope.launch {
            repository.updateLockPin(newPin)
            _uiMessage.value = "App Lock password updated successfully."
        }
    }

    // Student Actions
    fun setStudentSearchQuery(query: String) {
        _studentSearchQuery.value = query
    }

    fun saveStudent(student: Student) {
        viewModelScope.launch {
            if (student.id == 0L) {
                repository.addStudent(student)
                _uiMessage.value = "Student '${student.fullName}' added!"
            } else {
                repository.updateStudent(student)
                _uiMessage.value = "Student '${student.fullName}' updated!"
            }
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            _uiMessage.value = "Student '${student.fullName}' deleted."
        }
    }

    // Subject Actions
    fun saveSubject(subject: Subject) {
        viewModelScope.launch {
            if (subject.id == 0L) {
                repository.addSubject(subject)
                _uiMessage.value = "Subject '${subject.name}' added!"
            } else {
                repository.updateSubject(subject)
                _uiMessage.value = "Subject '${subject.name}' updated!"
            }
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
            _uiMessage.value = "Subject '${subject.name}' deleted."
        }
    }

    // Question Actions
    fun setMcqSearchQuery(query: String) {
        _mcqSearchQuery.value = query
    }

    fun saveQuestion(mcq: McqQuestion) {
        viewModelScope.launch {
            if (mcq.id == 0L) {
                repository.addQuestion(mcq)
                _uiMessage.value = "Question added for ${mcq.subjectName}!"
            } else {
                repository.updateQuestion(mcq)
                _uiMessage.value = "Question updated!"
            }
        }
    }

    fun deleteQuestion(mcq: McqQuestion) {
        viewModelScope.launch {
            repository.deleteQuestion(mcq)
            _uiMessage.value = "Question deleted."
        }
    }

    // Test Actions
    fun createTest(
        test: Test,
        manualQuestionIds: List<Long>? = null,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val testId = repository.createTest(test, manualQuestionIds)
            _uiMessage.value = "Test '${test.title}' created!"
            onCreated(testId)
        }
    }

    fun deleteTest(test: Test) {
        viewModelScope.launch {
            repository.deleteTest(test)
            _uiMessage.value = "Test deleted."
        }
    }

    // Start Student Test
    fun startStudentTest(testId: Long) {
        viewModelScope.launch {
            val (test, questions) = repository.getTestWithQuestions(testId)
            if (test != null && questions.isNotEmpty()) {
                val preparedQuestions = if (test.isRandomOptions) {
                    questions.map { q ->
                        // If random options requested, we keep correct answer matching
                        q
                    }
                } else questions

                _activeTest.value = test
                _activeQuestions.value = preparedQuestions
                _userAnswers.value = emptyMap()
                _currentQuestionIndex.value = 0
                _timerSecondsLeft.value = test.timeLimitMinutes * 60
                _activeTestResult.value = null

                startTimer()
            } else {
                _uiMessage.value = "Cannot start test: No questions found for this test!"
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerSecondsLeft.value > 0) {
                delay(1000)
                _timerSecondsLeft.value -= 1
            }
            // Auto submit when timer reaches 0
            submitStudentTest("Auto Submitted")
        }
    }

    fun selectTestAnswer(questionIndex: Int, option: String) {
        val updated = _userAnswers.value.toMutableMap()
        updated[questionIndex] = option
        _userAnswers.value = updated
    }

    fun nextQuestion() {
        if (_currentQuestionIndex.value < _activeQuestions.value.size - 1) {
            _currentQuestionIndex.value += 1
        }
    }

    fun previousQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value -= 1
        }
    }

    fun submitStudentTest(studentName: String = "Student") {
        timerJob?.cancel()
        val test = _activeTest.value ?: return
        val questions = _activeQuestions.value
        val answers = _userAnswers.value

        var correctCount = 0
        var wrongCount = 0

        questions.forEachIndexed { index, mcq ->
            val userChoice = answers[index]
            if (userChoice != null && userChoice.equals(mcq.correctAnswer, ignoreCase = true)) {
                correctCount++
            } else {
                wrongCount++
            }
        }

        val totalQ = questions.size
        val obtainedMarks = correctCount * 1
        val maxMarks = totalQ * 1
        val percentage = if (maxMarks > 0) (obtainedMarks.toDouble() / maxMarks) * 100 else 0.0

        val grade = when {
            percentage >= 85 -> "A+"
            percentage >= 75 -> "A"
            percentage >= 65 -> "B"
            percentage >= 50 -> "C"
            else -> "F"
        }

        val timeUsed = (test.timeLimitMinutes * 60) - _timerSecondsLeft.value

        val result = TestResult(
            testId = test.id,
            studentName = studentName,
            subjectName = test.subjectName,
            className = test.className,
            totalQuestions = totalQ,
            correctAnswers = correctCount,
            wrongAnswers = wrongCount,
            obtainedMarks = obtainedMarks,
            maxMarks = maxMarks,
            percentage = percentage,
            grade = grade,
            timeUsedSeconds = timeUsed
        )

        viewModelScope.launch {
            repository.saveTestResult(result)
            _activeTestResult.value = result
        }
    }

    // Attendance Actions
    fun setSelectedAttendanceDate(date: String) {
        _selectedAttendanceDate.value = date
    }

    fun saveAttendance(records: List<AttendanceRecord>) {
        viewModelScope.launch {
            repository.markAttendance(records, _selectedAttendanceDate.value)
            _uiMessage.value = "Attendance saved for ${_selectedAttendanceDate.value}!"
        }
    }

    // Fee Actions
    fun recordFeePayment(payment: FeePayment) {
        viewModelScope.launch {
            repository.recordFeePayment(payment)
            _uiMessage.value = "Fee payment of Rs. ${payment.amountPaid} recorded!"
        }
    }

    fun deleteFeePayment(payment: FeePayment) {
        viewModelScope.launch {
            repository.deleteFeePayment(payment)
            _uiMessage.value = "Payment record deleted."
        }
    }

    // PDF Paper Generation
    fun generatePdfPaper(context: Context, test: Test, onGenerated: (File) -> Unit) {
        viewModelScope.launch {
            val (_, questions) = repository.getTestWithQuestions(test.id)
            val file = PdfPaperGenerator.generateTestPaperPdf(context, test, questions)
            if (file != null) {
                _uiMessage.value = "PDF Generated: ${file.name}"
                onGenerated(file)
            } else {
                _uiMessage.value = "Failed to generate PDF paper!"
            }
        }
    }

    // Backup & Restore
    fun exportBackupToJson(context: Context, onExported: (String) -> Unit) {
        viewModelScope.launch {
            val jsonStr = repository.exportDataToJson()
            _uiMessage.value = "Backup data generated successfully!"
            onExported(jsonStr)
        }
    }

    fun importBackupFromJson(jsonStr: String) {
        viewModelScope.launch {
            val success = repository.importDataFromJson(jsonStr)
            if (success) {
                _uiMessage.value = "Database restored successfully from backup!"
            } else {
                _uiMessage.value = "Failed to restore backup: Invalid format."
            }
        }
    }
}
