package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Student
import com.example.data.StudentDatabase
import com.example.util.BackupManager
import com.example.util.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Screen {
    Dashboard,
    AddStudent,
    EditStudent,
    SearchAndDetails,
    AllStudents,
    BackupAndReport
}

class StudentViewModel(application: Application) : AndroidViewModel(application) {

    private val db = StudentDatabase.getDatabase(application)
    private val studentDao = db.studentDao()

    // Screen navigation history
    private val navigationHistory = mutableListOf<Screen>()

    // Screen navigation
    var currentScreen by mutableStateOf(Screen.Dashboard)
        private set

    // All students reactive flow
    val allStudents: StateFlow<List<Student>> = studentDao.getAllStudentsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search query & results
    var searchQuery by mutableStateOf("")
        private set

    private val _searchResults = MutableStateFlow<List<Student>>(emptyList())
    val searchResults: StateFlow<List<Student>> = _searchResults.asStateFlow()

    // Form inputs
    var idToEdit by mutableStateOf<Long?>(null)
    var nameInput by mutableStateOf("")
    var idTypeInput by mutableStateOf("بطاقة شخصية") // default
    var idNumberInput by mutableStateOf("")
    var governorateInput by mutableStateOf("")
    var districtInput by mutableStateOf("")
    var villageInput by mutableStateOf("")
    var sectorInput by mutableStateOf("")
    var specializationInput by mutableStateOf("")
    var levelInput by mutableStateOf("المستوى الأول") // default
    var phoneInput by mutableStateOf("")
    var roomNumberInput by mutableStateOf("")
    var roomRentInput by mutableStateOf("")
    var totalPaidInput by mutableStateOf("0.0")
    var rentStartDateInput by mutableStateOf(System.currentTimeMillis())
    var isActiveInRoomInput by mutableStateOf(true)
    var rentEndDateInput by mutableStateOf<Long?>(null)

    // Form validation and messaging status
    var formError by mutableStateOf<String?>(null)
    var operationSuccessMessage by mutableStateOf<String?>(null)

    // General async operation progress
    var isOperating by mutableStateOf(false)
    var operationError by mutableStateOf<String?>(null)

    // Current student details (for view/details screen)
    var selectedStudent by mutableStateOf<Student?>(null)

    init {
        // Automatically search when query changes
        viewModelScope.launch {
            snapshotFlow { searchQuery }
                .debounce(300)
                .collectLatest { query ->
                    performSearch(query)
                }
        }
    }

    fun navigateTo(screen: Screen, student: Student? = null) {
        if (currentScreen != screen) {
            navigationHistory.add(currentScreen)
        }
        currentScreen = screen
        formError = null
        operationSuccessMessage = null
        operationError = null

        if (screen == Screen.EditStudent && student != null) {
            idToEdit = student.id
            nameInput = student.name
            idTypeInput = student.idType
            idNumberInput = student.idNumber
            governorateInput = student.governorate
            districtInput = student.district
            villageInput = student.village
            sectorInput = student.sector
            specializationInput = student.specialization
            levelInput = student.level
            phoneInput = student.phone
            roomNumberInput = student.roomNumber
            roomRentInput = student.roomRent.toString()
            totalPaidInput = student.totalPaid.toString()
            rentStartDateInput = student.rentStartDate
            isActiveInRoomInput = student.isActiveInRoom
            rentEndDateInput = student.rentEndDate
        } else if (screen == Screen.AddStudent) {
            clearForm()
        }

        if (student != null) {
            selectedStudent = student
        }
    }

    fun goBack() {
        if (navigationHistory.isNotEmpty()) {
            val prev = navigationHistory.removeAt(navigationHistory.size - 1)
            currentScreen = prev
            // Clear selected student if leaving details screen
            if (currentScreen != Screen.SearchAndDetails) {
                selectedStudent = null
            }
        } else {
            currentScreen = Screen.Dashboard
            selectedStudent = null
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList()
            } else {
                val formattedQuery = "%$query%"
                studentDao.searchStudents(formattedQuery).let {
                    _searchResults.value = it
                }
            }
        }
    }

    fun clearForm() {
        idToEdit = null
        nameInput = ""
        idTypeInput = "بطاقة شخصية"
        idNumberInput = ""
        governorateInput = ""
        districtInput = ""
        villageInput = ""
        sectorInput = ""
        specializationInput = ""
        levelInput = "المستوى الأول"
        phoneInput = ""
        roomNumberInput = ""
        roomRentInput = ""
        totalPaidInput = "0.0"
        rentStartDateInput = System.currentTimeMillis()
        isActiveInRoomInput = true
        rentEndDateInput = null
        formError = null
    }

    fun saveStudent() {
        // Validation
        if (nameInput.trim().length < 3) {
            formError = "اسم الطالب يجب أن يتكون من 3 أحرف على الأقل."
            return
        }
        if (idNumberInput.trim().isEmpty()) {
            formError = "الرجاء إدخال رقم الهوية."
            return
        }
        if (governorateInput.trim().isEmpty() || districtInput.trim().isEmpty()) {
            formError = "الرجاء تحديد المحافظة والمديرية."
            return
        }
        if (specializationInput.trim().isEmpty()) {
            formError = "الرجاء تحديد التخصص الدراسي."
            return
        }
        if (phoneInput.trim().isEmpty()) {
            formError = "الرجاء إدخال رقم الهاتف."
            return
        }
        if (roomNumberInput.trim().isEmpty()) {
            formError = "الرجاء إدخال رقم الغرفة."
            return
        }
        val roomRentVal = roomRentInput.trim().toDoubleOrNull()
        if (roomRentVal == null || roomRentVal < 0.0) {
            formError = "الرجاء إدخال مبلغ إيجار شهري صحيح وموجب."
            return
        }
        val totalPaidVal = totalPaidInput.trim().toDoubleOrNull() ?: 0.0

        formError = null
        isOperating = true

        viewModelScope.launch {
            try {
                val currentStudents = studentDao.getAllStudents().sortedBy { it.id }
                val nextId = if (currentStudents.isEmpty()) 1L else (currentStudents.last().id + 1)

                val student = Student(
                    id = idToEdit ?: nextId,
                    name = nameInput.trim(),
                    idType = idTypeInput,
                    idNumber = idNumberInput.trim(),
                    governorate = governorateInput.trim(),
                    district = districtInput.trim(),
                    village = villageInput.trim(),
                    sector = sectorInput.trim(),
                    specialization = specializationInput.trim(),
                    level = levelInput,
                    phone = phoneInput.trim(),
                    roomNumber = roomNumberInput.trim(),
                    roomRent = roomRentVal,
                    totalPaid = totalPaidVal,
                    rentStartDate = rentStartDateInput,
                    isActiveInRoom = isActiveInRoomInput,
                    rentEndDate = rentEndDateInput
                )

                if (idToEdit == null) {
                    studentDao.insertStudent(student)
                    operationSuccessMessage = "تم إضافة الطالب بنجاح!"
                } else {
                    studentDao.updateStudent(student)
                    operationSuccessMessage = "تم تحديث بيانات الطالب بنجاح!"
                    selectedStudent = student // Update details view as well
                }

                clearForm()
                // Clear navigation history when saving and return to dashboard
                navigationHistory.clear()
                navigateTo(Screen.Dashboard)
            } catch (e: Exception) {
                formError = "خطأ أثناء الحفظ: ${e.message}"
            } finally {
                isOperating = false
            }
        }
    }

    fun deleteStudent(student: Student, onDone: () -> Unit = {}) {
        isOperating = true
        viewModelScope.launch {
            try {
                // Delete student
                studentDao.deleteStudent(student)

                // Re-index remaining students sequentially from 1 to N
                val remaining = studentDao.getAllStudents().sortedBy { it.id }
                studentDao.deleteAllStudents()
                val reindexed = remaining.mapIndexed { index, s ->
                    s.copy(id = (index + 1).toLong())
                }
                studentDao.insertStudents(reindexed)

                operationSuccessMessage = "تم حذف الطالب وإعادة ترتيب الفهرسة التلقائية بنجاح!"
                if (selectedStudent?.id == student.id) {
                    selectedStudent = null
                }
                onDone()
            } catch (e: Exception) {
                operationError = "فشل حذف الطالب وإعادة الفهرسة: ${e.message}"
            } finally {
                isOperating = false
            }
        }
    }

    // Export PDF Report and trigger share sheet
    fun exportPdfReport(onShareUriReady: (Uri) -> Unit) {
        isOperating = true
        operationError = null
        viewModelScope.launch {
            try {
                val list = studentDao.getAllStudents().sortedBy { it.id }
                if (list.isEmpty()) {
                    operationError = "قاعدة البيانات فارغة، لا يمكن إنشاء تقرير لصفر طلاب."
                    return@launch
                }
                val uri = PdfGenerator.generateStudentReport(getApplication(), list)
                if (uri != null) {
                    onShareUriReady(uri)
                } else {
                    operationError = "فشل في إنشاء ملف PDF"
                }
            } catch (e: Exception) {
                operationError = "خطأ أثناء إنشاء PDF: ${e.message}"
            } finally {
                isOperating = false
            }
        }
    }

    // Export DB to shareable JSON Backup
    fun exportBackup(onShareUriReady: (Uri) -> Unit) {
        isOperating = true
        operationError = null
        viewModelScope.launch {
            try {
                val list = studentDao.getAllStudents().sortedBy { it.id }
                if (list.isEmpty()) {
                    operationError = "قاعدة البيانات فارغة، لا توجد سجلات لتصديرها."
                    return@launch
                }
                val uri = BackupManager.exportBackup(getApplication(), list)
                if (uri != null) {
                    onShareUriReady(uri)
                } else {
                    operationError = "فشل تصدير نسخة احتياطية"
                }
            } catch (e: Exception) {
                operationError = "خطأ أثناء تصدير البيانات: ${e.message}"
            } finally {
                isOperating = false
            }
        }
    }

    // Import DB from JSON Backup file
    fun importBackup(uri: Uri, onSuccess: (Int) -> Unit) {
        isOperating = true
        operationError = null
        viewModelScope.launch {
            try {
                val list = BackupManager.importBackup(getApplication(), uri)
                if (list != null && list.isNotEmpty()) {
                    // Clear and save sequentially
                    studentDao.deleteAllStudents()
                    val reindexed = list.sortedBy { it.id }.mapIndexed { index, s ->
                        s.copy(id = (index + 1).toLong())
                    }
                    studentDao.insertStudents(reindexed)
                    onSuccess(list.size)
                } else {
                    operationError = "ملف النسخة الاحتياطية غير صالح أو فارغ."
                }
            } catch (e: Exception) {
                operationError = "فشل استيراد النسخة الاحتياطية: ${e.message}"
            } finally {
                isOperating = false
            }
        }
    }

    // Seed database with fake Arabic student records for performance testing (3000 records)
    fun seedSampleData(onComplete: () -> Unit) {
        isOperating = true
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val firstNames = listOf("أحمد", "محمد", "علي", "صالح", "خالد", "عبد الرحمن", "عبد الله", "عمر", "سعيد", "حسن", "حسين", "يوسف", "مصطفى", "إبراهيم", "طارق", "فهد", "ماجد", "منصور", "سليمان", "ياسر")
                    val familyNames = listOf("الحداد", "الهمداني", "الرياشي", "المعمري", "الأنسي", "الشيباني", "المطري", "الخولاني", "الحيمي", "الصبري", "القدسي", "الحريبي", "الشامي", "الديلمي", "الرازحي", "اليافعي", "البيحاني", "المرقشي", "التهامي", "الحضرمي")
                    val governorates = listOf("صنعاء", "عدن", "تعز", "حضرموت", "إب", "الحديدة", "ذمار", "أبين", "مأرب", "شبوة", "صعدة", "حجة", "المهرة", "لحج", "الضالع", "عمران")
                    val specializations = listOf("هندسة برمجيات", "علوم حاسوب", "تقنية معلومات", "إدارة أعمال", "محاسبة", "هندسة مدنية", "هندسة معمارية", "ميكانيك", "طب بشري", "صيدلة", "تمريض", "لغة إنجليزية", "قانون")
                    val levels = listOf("المستوى الأول", "المستوى الثاني", "المستوى الثالث", "المستوى الرابع", "المستوى الخامس")
                    val idTypes = listOf("بطاقة شخصية", "جواز سفر", "بطاقة عائلية")

                    val existing = studentDao.getAllStudents()
                    val startId = if (existing.isEmpty()) 1L else (existing.maxOf { it.id } + 1)

                    val studentsToInsert = mutableListOf<Student>()
                    val batchSize = 3000
                    for (i in 1..batchSize) {
                        val firstName = firstNames.random()
                        val middleName = firstNames.random()
                        val lastName = familyNames.random()
                        val fullName = "$firstName $middleName $lastName"

                        val gov = governorates.random()
                        val dist = "المديرية ${i % 3 + 1}"
                        val village = "قرية ${familyNames.random()}"
                        val sector = "عزلة ${i % 5 + 1}"
                        val roomNum = "غرفة ${(i % 50) + 101}"
                        val rentVal = 25000.0 + (i % 6) * 5000.0
                        val paidVal = (i % 4) * 15000.0
                        val monthsAgo = (i % 4)
                        val startDateMs = System.currentTimeMillis() - (monthsAgo * 30L * 24 * 60 * 60 * 1000L)

                        studentsToInsert.add(
                            Student(
                                id = startId + i - 1,
                                name = fullName,
                                idType = idTypes.random(),
                                idNumber = "0${(100000000..999999999).random()}",
                                governorate = gov,
                                district = dist,
                                village = village,
                                sector = sector,
                                specialization = specializations.random(),
                                level = levels.random(),
                                phone = "77${(10000000..99999999).random()}",
                                roomNumber = roomNum,
                                roomRent = rentVal,
                                totalPaid = paidVal,
                                rentStartDate = startDateMs
                            )
                        )
                    }

                    studentDao.insertStudents(studentsToInsert)
                    withContext(Dispatchers.Main) {
                        operationSuccessMessage = "تم توليد 3000 طالب بنجاح للفحص والتجريب مع السكن والمدفوعات!"
                        onComplete()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        operationError = "فشل توليد البيانات: ${e.message}"
                    }
                }
            }
            isOperating = false
        }
    }

    fun recordPayment(student: Student, amount: Double) {
        viewModelScope.launch {
            try {
                val updatedStudent = student.copy(
                    totalPaid = student.totalPaid + amount
                )
                studentDao.updateStudent(updatedStudent)
                if (selectedStudent?.id == student.id) {
                    selectedStudent = updatedStudent
                }
                operationSuccessMessage = "تم تسجيل سداد بقيمة ${amount.toLong()} ريال بنجاح!"
            } catch (e: Exception) {
                operationError = "فشل تسجيل الدفعة: ${e.message}"
            }
        }
    }

    fun vacateStudentFromRoom(student: Student) {
        viewModelScope.launch {
            try {
                val updatedStudent = student.copy(
                    isActiveInRoom = false,
                    rentEndDate = System.currentTimeMillis()
                )
                studentDao.updateStudent(updatedStudent)
                if (selectedStudent?.id == student.id) {
                    selectedStudent = updatedStudent
                }
                operationSuccessMessage = "تم إخلاء الطالب ${student.name} من الغرفة بنجاح مع إبقاء حسابه المالي معلقاً بسجل المستحقات!"
            } catch (e: Exception) {
                operationError = "فشل إخلاء الطالب من الغرفة: ${e.message}"
            }
        }
    }

    fun calculateMonthsElapsed(startDateMs: Long, endDateMs: Long = System.currentTimeMillis()): Int {
        val startCalendar = java.util.Calendar.getInstance().apply { timeInMillis = startDateMs }
        val endCalendar = java.util.Calendar.getInstance().apply { timeInMillis = endDateMs }
        val diffYear = endCalendar.get(java.util.Calendar.YEAR) - startCalendar.get(java.util.Calendar.YEAR)
        val diffMonth = endCalendar.get(java.util.Calendar.MONTH) - startCalendar.get(java.util.Calendar.MONTH)
        val totalMonths = diffYear * 12 + diffMonth + 1 // count current month as well
        return if (totalMonths < 1) 1 else totalMonths
    }

    fun clearAllData() {
        isOperating = true
        viewModelScope.launch {
            try {
                studentDao.deleteAllStudents()
                operationSuccessMessage = "تم مسح جميع البيانات بنجاح!"
                selectedStudent = null
            } catch (e: Exception) {
                operationError = "فشل مسح البيانات: ${e.message}"
            } finally {
                isOperating = false
            }
        }
    }
}
