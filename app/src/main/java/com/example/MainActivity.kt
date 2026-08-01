package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.AcademyViewModel
import com.example.ui.screens.*
import com.example.ui.theme.CatalystAcademyTheme
import com.example.util.PdfPaperGenerator

class MainActivity : ComponentActivity() {

    private val viewModel: AcademyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CatalystAcademyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    val isActivated by viewModel.isActivated.collectAsStateWithLifecycle()
                    val isLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
                    val securityError by viewModel.securityError.collectAsStateWithLifecycle()
                    val uiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()

                    LaunchedEffect(uiMessage) {
                        uiMessage?.let { msg ->
                            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                            viewModel.clearUiMessage()
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        composable("splash") {
                            SplashScreen(
                                isActivated = isActivated,
                                onNavigateNext = {
                                    if (isLocked) {
                                        navController.navigate("lock") {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate("dashboard") {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        composable("lock") {
                            PasswordLockScreen(
                                isActivated = isActivated,
                                errorMessage = securityError,
                                onSubmitPassword = { pass ->
                                    if (!isActivated) {
                                        viewModel.submitActivationPassword(pass)
                                    } else {
                                        viewModel.submitAppLockPin(pass)
                                    }
                                }
                            )

                            LaunchedEffect(isLocked) {
                                if (!isLocked) {
                                    navController.navigate("dashboard") {
                                        popUpTo("lock") { inclusive = true }
                                    }
                                }
                            }
                        }

                        composable("dashboard") {
                            val totalStudents by viewModel.totalStudents.collectAsStateWithLifecycle()
                            val totalTests by viewModel.totalTests.collectAsStateWithLifecycle()
                            val todayAttendance by viewModel.todayAttendanceCount.collectAsStateWithLifecycle()
                            val totalFeeCollected by viewModel.totalFeeCollected.collectAsStateWithLifecycle()
                            val recentActivities by viewModel.recentActivities.collectAsStateWithLifecycle()

                            DashboardScreen(
                                totalStudents = totalStudents,
                                totalTests = totalTests,
                                todayAttendance = todayAttendance,
                                totalFeeCollected = totalFeeCollected ?: 0.0,
                                recentActivities = recentActivities,
                                onNavigate = { route -> navController.navigate(route) }
                            )
                        }

                        composable("students") {
                            val students by viewModel.studentsList.collectAsStateWithLifecycle()
                            val searchQuery by viewModel.studentSearchQuery.collectAsStateWithLifecycle()

                            StudentManagementScreen(
                                students = students,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { viewModel.setStudentSearchQuery(it) },
                                onSaveStudent = { viewModel.saveStudent(it) },
                                onDeleteStudent = { viewModel.deleteStudent(it) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("subjects") {
                            val subjects by viewModel.subjectsList.collectAsStateWithLifecycle()

                            SubjectManagementScreen(
                                subjects = subjects,
                                onSaveSubject = { viewModel.saveSubject(it) },
                                onDeleteSubject = { viewModel.deleteSubject(it) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("questions") {
                            val questions by viewModel.mcqList.collectAsStateWithLifecycle()
                            val subjects by viewModel.subjectsList.collectAsStateWithLifecycle()
                            val searchQuery by viewModel.mcqSearchQuery.collectAsStateWithLifecycle()

                            QuestionBankScreen(
                                questions = questions,
                                subjects = subjects,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { viewModel.setMcqSearchQuery(it) },
                                onSaveQuestion = { viewModel.saveQuestion(it) },
                                onDeleteQuestion = { viewModel.deleteQuestion(it) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("test_creator") {
                            val subjects by viewModel.subjectsList.collectAsStateWithLifecycle()
                            val questions by viewModel.mcqList.collectAsStateWithLifecycle()

                            TestCreatorScreen(
                                subjects = subjects,
                                allQuestions = questions,
                                onCreateTest = { test, manualIds ->
                                    viewModel.createTest(test, manualIds) {
                                        navController.navigate("take_test_list")
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("take_test_list") {
                            val tests by viewModel.testsList.collectAsStateWithLifecycle()

                            TestListScreen(
                                tests = tests,
                                onStartTest = { testId ->
                                    viewModel.startStudentTest(testId)
                                    navController.navigate("student_test")
                                },
                                onGeneratePdf = { test ->
                                    viewModel.generatePdfPaper(this@MainActivity, test) { file ->
                                        PdfPaperGenerator.openPdfFile(this@MainActivity, file)
                                    }
                                },
                                onDeleteTest = { viewModel.deleteTest(it) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("student_test") {
                            val activeTest by viewModel.activeTest.collectAsStateWithLifecycle()
                            val questions by viewModel.activeQuestions.collectAsStateWithLifecycle()
                            val currentIndex by viewModel.currentQuestionIndex.collectAsStateWithLifecycle()
                            val userAnswers by viewModel.userAnswers.collectAsStateWithLifecycle()
                            val timerSecondsLeft by viewModel.timerSecondsLeft.collectAsStateWithLifecycle()
                            val activeResult by viewModel.activeTestResult.collectAsStateWithLifecycle()

                            LaunchedEffect(activeResult) {
                                if (activeResult != null) {
                                    navController.navigate("test_result") {
                                        popUpTo("student_test") { inclusive = true }
                                    }
                                }
                            }

                            if (activeTest != null) {
                                StudentTestScreen(
                                    test = activeTest!!,
                                    questions = questions,
                                    currentIndex = currentIndex,
                                    userAnswers = userAnswers,
                                    secondsLeft = timerSecondsLeft,
                                    onSelectAnswer = { qIdx, opt -> viewModel.selectTestAnswer(qIdx, opt) },
                                    onNext = { viewModel.nextQuestion() },
                                    onPrevious = { viewModel.previousQuestion() },
                                    onSubmit = { name -> viewModel.submitStudentTest(name) },
                                    onCancel = { navController.popBackStack() }
                                )
                            }
                        }

                        composable("test_result") {
                            val activeResult by viewModel.activeTestResult.collectAsStateWithLifecycle()

                            if (activeResult != null) {
                                TestResultScreen(
                                    result = activeResult!!,
                                    onHome = {
                                        navController.navigate("dashboard") {
                                            popUpTo("dashboard") { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }

                        composable("attendance") {
                            val students by viewModel.studentsList.collectAsStateWithLifecycle()
                            val attendanceRecords by viewModel.attendanceForSelectedDate.collectAsStateWithLifecycle()
                            val selectedDate by viewModel.selectedAttendanceDate.collectAsStateWithLifecycle()

                            AttendanceScreen(
                                students = students,
                                savedRecords = attendanceRecords,
                                selectedDate = selectedDate,
                                onDateChange = { viewModel.setSelectedAttendanceDate(it) },
                                onSaveAttendance = { records -> viewModel.saveAttendance(records) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("fee") {
                            val students by viewModel.studentsList.collectAsStateWithLifecycle()
                            val payments by viewModel.feePaymentsList.collectAsStateWithLifecycle()

                            FeeManagementScreen(
                                students = students,
                                payments = payments,
                                onRecordPayment = { viewModel.recordFeePayment(it) },
                                onDeletePayment = { viewModel.deleteFeePayment(it) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("reports") {
                            val results by viewModel.resultsList.collectAsStateWithLifecycle()
                            val searchQuery by viewModel.resultSearchQuery.collectAsStateWithLifecycle()

                            ReportsScreen(
                                results = results,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { viewModel.setStudentSearchQuery(it) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("backup") {
                            BackupRestoreScreen(
                                onExportJson = { callback ->
                                    viewModel.exportBackupToJson(this@MainActivity, callback)
                                },
                                onImportJson = { json ->
                                    viewModel.importBackupFromJson(json)
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("security") {
                            val lockPin by viewModel.repository.appLockPin.collectAsStateWithLifecycle("Pakistan786")

                            SecuritySettingsScreen(
                                currentPin = lockPin,
                                onUpdatePin = { newPin -> viewModel.updateLockPin(newPin) },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
