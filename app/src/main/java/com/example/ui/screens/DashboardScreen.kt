package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Student
import com.example.ui.Screen
import com.example.ui.StudentViewModel
import com.example.ui.components.GraduationCapIcon

@Composable
fun DashboardScreen(
    viewModel: StudentViewModel,
    modifier: Modifier = Modifier
) {
    val students by viewModel.allStudents.collectAsState()
    val recentStudents = students.take(5)

    var showRoomLedgerDialog by remember { mutableStateOf(false) }

    // Derived statistics
    val activeStudents = students.filter { it.isActiveInRoom }
    val activeCount = activeStudents.size
    val vacatedCount = students.size - activeCount

    var totalOutstandingActive = 0.0
    var totalOutstandingVacated = 0.0
    students.forEach { student ->
        val months = if (student.isActiveInRoom) {
            viewModel.calculateMonthsElapsed(student.rentStartDate)
        } else {
            viewModel.calculateMonthsElapsed(student.rentStartDate, student.rentEndDate ?: System.currentTimeMillis())
        }
        val totalRent = months * student.roomRent
        val remaining = totalRent - student.totalPaid
        if (remaining > 0) {
            if (student.isActiveInRoom) {
                totalOutstandingActive += remaining
            } else {
                totalOutstandingVacated += remaining
            }
        }
    }

    val totalStudents = students.size
    val specializationCount = students.map { it.specialization.lowercase().trim() }.distinct().size
    val levelCounts = students.groupBy { it.level }.mapValues { it.value.size }

    // Color definitions
    val primaryGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF0D9488), Color(0xFF059669)) // Teal and Emerald Gradient
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Soft Slate BG
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Branding Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(primaryGradient)
                    .padding(20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "نظام إدارة الطلاب الذكي",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "لوحة تحكم إدارية متكاملة للبيانات الأكاديمية • غير متصل بالإنترنت",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                    GraduationCapIcon(
                        modifier = Modifier
                            .size(72.dp)
                            .padding(start = 12.dp),
                        tint = Color.White.copy(alpha = 0.95f)
                    )
                }
            }
        }

        // Stats Row (Bento Grid)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "الطلاب النشطين",
                        value = activeCount.toString(),
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFF0D9488), // Brand Teal
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "الطلاب المغادرين",
                        value = vacatedCount.toString(),
                        icon = Icons.Default.ExitToApp,
                        color = Color(0xFFE11D48), // Rose Red
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "مستحقات النشطين",
                        value = "${totalOutstandingActive.toLong()} ريال",
                        icon = Icons.Default.AccountBalanceWallet,
                        color = Color(0xFF0D9488),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "ذمم المغادرين",
                        value = "${totalOutstandingVacated.toLong()} ريال",
                        icon = Icons.Default.Warning,
                        color = Color(0xFFF59E0B), // Amber
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Action Buttons Grid
        item {
            Text(
                text = "العمليات الأساسية",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCard(
                        title = "إضافة طالب جديد",
                        subtitle = "تسجيل بيانات طالب جديد",
                        icon = Icons.Default.PersonAdd,
                        color = Color(0xFF0D9488), // Brand Teal
                        onClick = { viewModel.navigateTo(Screen.AddStudent) },
                        modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        title = "عرض كل الطلاب",
                        subtitle = "كشف وتعديل وتصفية",
                        icon = Icons.Default.ListAlt,
                        color = Color(0xFF10B981),
                        onClick = { viewModel.navigateTo(Screen.AllStudents) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCard(
                        title = "البحث السريع",
                        subtitle = "البحث بالاسم أو الهوية",
                        icon = Icons.Default.Search,
                        color = Color(0xFFF59E0B),
                        onClick = { viewModel.navigateTo(Screen.SearchAndDetails) },
                        modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        title = "النسخ والتقارير",
                        subtitle = "نسخ احتياطي وتقارير PDF",
                        icon = Icons.Default.Settings,
                        color = Color(0xFF0891B2), // Cyan
                        onClick = { viewModel.navigateTo(Screen.BackupAndReport) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                ActionCard(
                    title = "مراقبة إيجارات وحسابات الغرف السكنية",
                    subtitle = "الغرف المشغولة والشركاء، ديون متأخرات المغادرين، وتجنب التضارب",
                    icon = Icons.Default.Home,
                    color = Color(0xFF4F46E5), // Beautiful Indigo
                    onClick = { showRoomLedgerDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Recent Additions List
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "آخر الطلاب المضافين",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                if (students.isNotEmpty()) {
                    TextButton(onClick = { viewModel.navigateTo(Screen.AllStudents) }) {
                        Text("عرض الكل", fontSize = 11.sp, color = Color(0xFF0D9488))
                    }
                }
            }
        }

        if (recentStudents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PeopleOutline,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا يوجد طلاب مسجلين حتى الآن",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "انقر على زر 'إضافة طالب جديد' لبدء ملء السجلات.",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(recentStudents) { student ->
                StudentListItemSmall(
                    student = student,
                    onClick = { viewModel.navigateTo(Screen.SearchAndDetails, student) }
                )
            }
        }
    }

    if (showRoomLedgerDialog) {
        RoomLedgerDialog(
            viewModel = viewModel,
            students = students,
            onDismiss = { showRoomLedgerDialog = false }
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StudentListItemSmall(
    student: Student,
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFEEF2F6), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.name.take(1),
                    color = Color(0xFF0D9488),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
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
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .background(Color(0xFFCBD5E1), CircleShape)
                    )
                    Text(
                        text = student.level,
                        fontSize = 10.sp,
                        color = Color(0xFF4F46E5),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

data class RoomLedgerData(
    val roomNumber: String,
    val activeOccupants: List<Student>,
    val vacatedDebtors: List<Student>,
    val totalActiveRemaining: Double,
    val totalVacatedRemaining: Double,
    val totalRemaining: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomLedgerDialog(
    viewModel: StudentViewModel,
    students: List<Student>,
    onDismiss: () -> Unit
) {
    var filterType by remember { mutableStateOf("الكل") }

    val roomsGrouped = remember(students) {
        students
            .filter { it.roomNumber.trim().isNotEmpty() }
            .groupBy { it.roomNumber.trim() }
    }

    val roomItems = remember(roomsGrouped, filterType) {
        val list = mutableListOf<RoomLedgerData>()
        roomsGrouped.forEach { (roomNumber, studentList) ->
            val active = studentList.filter { it.isActiveInRoom }
            val vacatedDebtors = studentList.filter { student ->
                if (student.isActiveInRoom) false
                else {
                    val months = viewModel.calculateMonthsElapsed(student.rentStartDate, student.rentEndDate ?: System.currentTimeMillis())
                    val totalRent = months * student.roomRent
                    val remaining = totalRent - student.totalPaid
                    remaining > 0
                }
            }

            val totalActiveRemaining = active.sumOf { student ->
                val months = viewModel.calculateMonthsElapsed(student.rentStartDate)
                val totalRent = months * student.roomRent
                maxOf(0.0, totalRent - student.totalPaid)
            }

            val totalVacatedRemaining = vacatedDebtors.sumOf { student ->
                val months = viewModel.calculateMonthsElapsed(student.rentStartDate, student.rentEndDate ?: System.currentTimeMillis())
                val totalRent = months * student.roomRent
                maxOf(0.0, totalRent - student.totalPaid)
            }

            val totalRemaining = totalActiveRemaining + totalVacatedRemaining

            val item = RoomLedgerData(
                roomNumber = roomNumber,
                activeOccupants = active,
                vacatedDebtors = vacatedDebtors,
                totalActiveRemaining = totalActiveRemaining,
                totalVacatedRemaining = totalVacatedRemaining,
                totalRemaining = totalRemaining
            )

            // Apply filters
            val matchesFilter = when (filterType) {
                "غرف متبقي عليها إيجار" -> totalRemaining > 0
                "سكن مشترك" -> active.size > 1
                "متأخرات مغادرين" -> vacatedDebtors.isNotEmpty()
                else -> true
            }

            if (matchesFilter) {
                list.add(item)
            }
        }
        list.sortedBy { it.roomNumber }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "كشف وحسابات الغرف السكنية",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color(0xFF64748B)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Filter chips
                val filters = listOf("الكل", "غرف متبقي عليها إيجار", "سكن مشترك", "متأخرات مغادرين")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    filters.forEach { filter ->
                        val isSelected = filterType == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Color(0xFF4F46E5) else Color(0xFFF1F5F9))
                                .clickable { filterType = filter }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF64748B)
                            )
                        }
                    }
                }

                // Rooms List
                if (roomItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "لا توجد غرف تطابق التصفية الحالية",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(roomItems) { item ->
                            RoomLedgerCard(item = item, viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoomLedgerCard(
    item: RoomLedgerData,
    viewModel: StudentViewModel
) {
    val dateFormatter = remember { java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale("ar")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Room Title & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "غرفة رقم [ ${item.roomNumber} ]",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }

                // Status Badge (Active or Vacated)
                val isActive = item.activeOccupants.isNotEmpty()
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isActive) Color(0xFFE6F4EA) else Color(0xFFFEE2E2))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isActive) "نشطة حالياً" else "شاغرة وبها ذمم معلقة",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) Color(0xFF137333) else Color(0xFFC5221F)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0))

            // 1. Active occupants section
            if (item.activeOccupants.isNotEmpty()) {
                Text(
                    text = "النزلاء النشطين حالياً في الغرفة:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D9488)
                )
                item.activeOccupants.forEach { occupant ->
                    val months = viewModel.calculateMonthsElapsed(occupant.rentStartDate)
                    val totalRent = months * occupant.roomRent
                    val remaining = maxOf(0.0, totalRent - occupant.totalPaid)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = occupant.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "البداية: ${dateFormatter.format(java.util.Date(occupant.rentStartDate))}",
                                fontSize = 9.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "الإيجار: ${occupant.roomRent.toLong()} ريال/شهر",
                                fontSize = 9.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المدة: $months أشهر (إجمالي: ${totalRent.toLong()} ريال)",
                                fontSize = 9.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "المسدد: ${occupant.totalPaid.toLong()} ريال",
                                fontSize = 9.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (remaining > 0) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFEF3C7))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "المتبقي: ${remaining.toLong()} ريال",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Vacated debtors section
            if (item.vacatedDebtors.isNotEmpty()) {
                Text(
                    text = "حسابات معلقة لطلاب غادروا الغرفة سابقاً:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444)
                )
                item.vacatedDebtors.forEach { debtor ->
                    val months = viewModel.calculateMonthsElapsed(debtor.rentStartDate, debtor.rentEndDate ?: System.currentTimeMillis())
                    val totalRent = months * debtor.roomRent
                    val remaining = maxOf(0.0, totalRent - debtor.totalPaid)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF1F2), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = debtor.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9F1239)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFFE4E6))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "غادر",
                                    fontSize = 8.sp,
                                    color = Color(0xFFE11D48),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "الفترة: ${dateFormatter.format(java.util.Date(debtor.rentStartDate))} ➔ ${dateFormatter.format(java.util.Date(debtor.rentEndDate ?: System.currentTimeMillis()))}",
                                fontSize = 8.5.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "الإيجار: ${debtor.roomRent.toLong()} ريال/شهر",
                                fontSize = 9.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المسدد: ${debtor.totalPaid.toLong()} ريال (إجمالي المستحق: ${totalRent.toLong()} ريال)",
                                fontSize = 8.5.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "الدين المعلق: ${remaining.toLong()} ريال",
                                fontSize = 9.sp,
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Room Financial Summary footer
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "إجمالي ديون الغرفة (نشطة + معلقة):",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "${item.totalRemaining.toLong()} ريال",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (item.totalRemaining > 0) Color(0xFFEF4444) else Color(0xFF10B981)
                )
            }
        }
    }
}
