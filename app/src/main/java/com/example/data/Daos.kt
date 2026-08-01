package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY fullName ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students ORDER BY fullName ASC")
    suspend fun getAllStudentsList(): List<Student>

    @Query("SELECT * FROM students WHERE fullName LIKE '%' || :query || '%' OR rollNumber LIKE '%' || :query || '%' OR className LIKE '%' || :query || '%' ORDER BY fullName ASC")
    fun searchStudents(query: String): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    fun getStudentById(id: Long): Flow<Student?>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentByIdSync(id: Long): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("SELECT COUNT(*) FROM students")
    fun getStudentCount(): Flow<Int>

    @Query("DELETE FROM students")
    suspend fun clearAll()
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects ORDER BY name ASC")
    suspend fun getAllSubjectsList(): List<Subject>

    @Query("SELECT * FROM subjects WHERE id = :id")
    fun getSubjectById(id: Long): Flow<Subject?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Update
    suspend fun updateSubject(subject: Subject)

    @Delete
    suspend fun deleteSubject(subject: Subject)

    @Query("DELETE FROM subjects")
    suspend fun clearAll()
}

@Dao
interface McqQuestionDao {
    @Query("SELECT * FROM questions ORDER BY id DESC")
    fun getAllQuestions(): Flow<List<McqQuestion>>

    @Query("SELECT * FROM questions ORDER BY id DESC")
    suspend fun getAllQuestionsList(): List<McqQuestion>

    @Query("SELECT * FROM questions WHERE subjectId = :subjectId ORDER BY id DESC")
    fun getQuestionsBySubject(subjectId: Long): Flow<List<McqQuestion>>

    @Query("SELECT * FROM questions WHERE subjectId = :subjectId")
    suspend fun getQuestionsBySubjectSync(subjectId: Long): List<McqQuestion>

    @Query("SELECT * FROM questions WHERE subjectId = :subjectId AND chapter = :chapter")
    suspend fun getQuestionsBySubjectAndChapterSync(subjectId: Long, chapter: String): List<McqQuestion>

    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getQuestionById(id: Long): McqQuestion?

    @Query("SELECT * FROM questions WHERE questionText LIKE '%' || :query || '%' OR chapter LIKE '%' || :query || '%' OR subjectName LIKE '%' || :query || '%'")
    fun searchQuestions(query: String): Flow<List<McqQuestion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: McqQuestion): Long

    @Update
    suspend fun updateQuestion(question: McqQuestion)

    @Delete
    suspend fun deleteQuestion(question: McqQuestion)

    @Query("DELETE FROM questions")
    suspend fun clearAll()
}

@Dao
interface TestDao {
    @Query("SELECT * FROM tests ORDER BY createdAt DESC")
    fun getAllTests(): Flow<List<Test>>

    @Query("SELECT * FROM tests ORDER BY createdAt DESC")
    suspend fun getAllTestsList(): List<Test>

    @Query("SELECT * FROM tests WHERE id = :id")
    fun getTestById(id: Long): Flow<Test?>

    @Query("SELECT * FROM tests WHERE id = :id")
    suspend fun getTestByIdSync(id: Long): Test?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: Test): Long

    @Delete
    suspend fun deleteTest(test: Test)

    @Query("SELECT COUNT(*) FROM tests")
    fun getTestCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestQuestionXRef(ref: TestQuestionXRef)

    @Query("SELECT questionId FROM test_questions WHERE testId = :testId ORDER BY questionOrder ASC")
    suspend fun getQuestionIdsForTest(testId: Long): List<Long>

    @Query("DELETE FROM tests")
    suspend fun clearAll()

    @Query("DELETE FROM test_questions")
    suspend fun clearAllXRef()
}

@Dao
interface TestResultDao {
    @Query("SELECT * FROM test_results ORDER BY completedAt DESC")
    fun getAllResults(): Flow<List<TestResult>>

    @Query("SELECT * FROM test_results ORDER BY completedAt DESC")
    suspend fun getAllResultsList(): List<TestResult>

    @Query("SELECT * FROM test_results WHERE studentId = :studentId ORDER BY completedAt DESC")
    fun getResultsByStudent(studentId: Long): Flow<List<TestResult>>

    @Query("SELECT * FROM test_results WHERE subjectName = :subjectName ORDER BY completedAt DESC")
    fun getResultsBySubject(subjectName: String): Flow<List<TestResult>>

    @Query("SELECT * FROM test_results WHERE studentName LIKE '%' || :query || '%' OR subjectName LIKE '%' || :query || '%' ORDER BY completedAt DESC")
    fun searchResults(query: String): Flow<List<TestResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: TestResult): Long

    @Query("DELETE FROM test_results")
    suspend fun clearAll()
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceByDate(date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance WHERE date = :date")
    suspend fun getAttendanceByDateSync(date: String): List<AttendanceRecord>

    @Query("SELECT * FROM attendance ORDER BY date DESC")
    suspend fun getAllAttendanceList(): List<AttendanceRecord>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceByStudent(studentId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance WHERE date LIKE :monthPrefix || '%' ORDER BY date DESC")
    fun getAttendanceByMonth(monthPrefix: String): Flow<List<AttendanceRecord>>

    @Query("SELECT COUNT(*) FROM attendance WHERE date = :todayDate AND status = 'PRESENT'")
    fun getTodayPresentCount(todayDate: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttendance(records: List<AttendanceRecord>)

    @Query("DELETE FROM attendance")
    suspend fun clearAll()
}

@Dao
interface FeeDao {
    @Query("SELECT * FROM fee_payments ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<FeePayment>>

    @Query("SELECT * FROM fee_payments ORDER BY paymentDate DESC")
    suspend fun getAllPaymentsList(): List<FeePayment>

    @Query("SELECT * FROM fee_payments WHERE studentId = :studentId ORDER BY paymentDate DESC")
    fun getPaymentsByStudent(studentId: Long): Flow<List<FeePayment>>

    @Query("SELECT * FROM fee_payments WHERE studentId = :studentId ORDER BY paymentDate DESC")
    suspend fun getPaymentsByStudentSync(studentId: Long): List<FeePayment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: FeePayment): Long

    @Delete
    suspend fun deletePayment(payment: FeePayment)

    @Query("SELECT SUM(amountPaid) FROM fee_payments")
    fun getTotalFeeCollected(): Flow<Double?>

    @Query("DELETE FROM fee_payments")
    suspend fun clearAll()
}

@Dao
interface RecentActivityDao {
    @Query("SELECT * FROM activities ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentActivities(limit: Int = 20): Flow<List<RecentActivity>>

    @Query("SELECT * FROM activities ORDER BY timestamp DESC")
    suspend fun getAllActivitiesList(): List<RecentActivity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: RecentActivity)

    @Query("DELETE FROM activities")
    suspend fun clearAll()
}
