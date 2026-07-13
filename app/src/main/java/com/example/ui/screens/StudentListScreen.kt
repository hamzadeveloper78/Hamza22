package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Student
import com.example.ui.Screen
import com.example.ui.StudentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentListScreen(
    viewModel: StudentViewModel,
    modifier: Modifier = Modifier
) {
    val students by viewModel.allStudents.collectAsState()

    // Filtering states
    var selectedLevelFilter by remember { mutableStateOf("الكل") }
    var selectedSpecFilter by remember { mutableStateOf("الكل") }
    var occupancyFilter by remember { mutableStateOf("الكل") }
    var searchText by remember { mutableStateOf("") }

    // Derive list of specializations for filter dropdown
    val specializations = remember(students) {
        listOf("الكل") + students.map { it.specialization.trim() }.distinct().sorted()
    }

    // Filtered students computation
    val filteredStudents = remember(students, selectedLevelFilter, selectedSpecFilter, occupancyFilter, searchText) {
        students.filter { student ->
            val matchesLevel = selectedLevelFilter == "الكل" || student.level == selectedLevelFilter
            val matchesSpec = selectedSpecFilter == "الكل" || student.specialization.trim() == selectedSpecFilter
            val matchesOccupancy = when (occupancyFilter) {
                "نشط حالياً" -> student.isActiveInRoom
                "مغادر الغرفة" -> !student.isActiveInRoom
                else -> true
            }
            val matchesSearch = searchText.isBlank() ||
                    student.name.contains(searchText, ignoreCase = true) ||
                    student.idNumber.contains(searchText) ||
                    student.phone.contains(searchText)
            matchesLevel && matchesSpec && matchesOccupancy && matchesSearch
        }
    }

    // Dropdown visibility
    var showSpecDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "سجل الطلاب العام",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "معاينة وتصفية ${filteredStudents.size} من أصل ${students.size} طالب",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "الرجوع",
                            tint = Color(0xFF1E293B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(innerPadding)
        ) {

            // Search and Advanced Filters Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Inline search bar inside list
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text("بحث سريع بالاسم، رقم الهوية أو الهاتف...", fontSize = 11.sp, color = Color(0xFF94A3B8)) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A), // Highly visible dark text
                            unfocusedTextColor = Color(0xFF0F172A), // Highly visible dark text
                            focusedBorderColor = Color(0xFF0D9488), // Brand Teal
                            unfocusedBorderColor = Color(0xFFE2E8F0).copy(alpha = 0.3f),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = Color(0xFF0F172A)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Specialization dropdown button
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF1F5F9))
                                .clickable { showSpecDropdown = true }
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تصفية حسب التخصص: $selectedSpecFilter",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569)
                            )
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF64748B))
                        }

                        DropdownMenu(
                            expanded = showSpecDropdown,
                            onDismissRequest = { showSpecDropdown = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            specializations.forEach { spec ->
                                DropdownMenuItem(
                                    text = { Text(spec, fontSize = 12.sp) },
                                    onClick = {
                                        selectedSpecFilter = spec
                                        showSpecDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Level Filter Horizontal Chips Row
                    val levelOptions = listOf("الكل", "المستوى الأول", "المستوى الثاني", "المستوى الثالث", "المستوى الرابع", "المستوى الخامس")
                    val rowScrollState = rememberScrollState()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rowScrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        levelOptions.forEach { level ->
                            val isSelected = selectedLevelFilter == level
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) Color(0xFF0D9488) else Color(0xFFF1F5F9))
                                    .clickable { selectedLevelFilter = level }
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = level,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    // Occupancy Filter Horizontal Chips Row
                    val occupancyOptions = listOf("الكل", "نشط حالياً", "مغادر الغرفة")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        occupancyOptions.forEach { opt ->
                            val isSelected = occupancyFilter == opt
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) Color(0xFF0D9488) else Color(0xFFF1F5F9))
                                    .clickable { occupancyFilter = opt }
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = opt,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }

            // Student Records
            if (filteredStudents.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = null,
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "لا توجد سجلات مطابقة للفحص",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "جرب ضبط شروط البحث أو الفلاتر بالأعلى لعرض بيانات الطلاب المناسبة.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // Optimized lazy-loading list item rendering
                    itemsIndexed(filteredStudents, key = { _, s -> s.id }) { index, student ->
                        StudentListCard(
                            student = student,
                            index = index + 1,
                            onClick = { viewModel.navigateTo(Screen.SearchAndDetails, student) }
                        )
                    }
                }
            }
        }
    }

    // Reuse the beautiful StudentDetailsDialog from SearchScreen if a student gets clicked in this list
    viewModel.selectedStudent?.let { student ->
        if (viewModel.currentScreen == Screen.SearchAndDetails) {
            StudentDetailsDialog(
                student = student,
                viewModel = viewModel,
                onEdit = {
                    viewModel.navigateTo(Screen.EditStudent, student)
                },
                onDeleteRequest = {
                    // Redirect to search screen for safe deletes to preserve delete states
                    viewModel.navigateTo(Screen.SearchAndDetails, student)
                },
                onDismiss = {
                    viewModel.navigateTo(Screen.AllStudents, null)
                }
            )
        }
    }
}

@Composable
fun StudentListCard(
    student: Student,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Index Bubble
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(Color(0xFFEEF2F6), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = index.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D9488)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Body info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = student.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (!student.isActiveInRoom) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFEF2F2))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "غادر الغرفة",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = student.specialization,
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                    Box(modifier = Modifier.size(3.dp).background(Color(0xFFCBD5E1), CircleShape))
                    Text(
                        text = student.level,
                        fontSize = 10.sp,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Chevron Icon
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
