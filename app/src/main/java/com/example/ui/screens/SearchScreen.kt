package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: StudentViewModel,
    modifier: Modifier = Modifier
) {
    val results by viewModel.searchResults.collectAsState()
    val query = viewModel.searchQuery

    // Confirmation delete control
    var studentToDelete by remember { mutableStateOf<Student?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "البحث والتحكم بالسجلات",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Beautiful Search Box
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("ابحث باسم الطالب أو رقم الهوية...", fontSize = 12.sp, color = Color(0xFF94A3B8)) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8)) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "مسح", tint = Color(0xFF64748B))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF0F172A), // Highly readable dark text
                    unfocusedTextColor = Color(0xFF0F172A), // Highly readable dark text
                    focusedBorderColor = Color(0xFF0D9488), // Brand Teal
                    unfocusedBorderColor = Color(0xFFE2E8F0).copy(alpha = 0.5f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth()
            )

            // Results List
            Box(modifier = Modifier.weight(1f)) {
                if (query.isBlank()) {
                    // Empty search state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.YoutubeSearchedFor,
                            contentDescription = null,
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "ابدأ الكتابة للبحث الفوري",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "يمكنك البحث بكتابة اسم الطالب أو رقم هويته الوطنية لحفظ الوقت والجهد.",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (results.isEmpty()) {
                    // No matches
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = Color(0xFFFDA4AF),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "عذراً، لم نجد أي نتائج!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE11D48),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "تأكد من كتابة الاسم بشكل صحيح أو جرب رقم هوية آخر.",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Standard results
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(results) { student ->
                            StudentSearchItem(
                                student = student,
                                onClick = { viewModel.navigateTo(Screen.SearchAndDetails, student) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Full Details Modal Dialog
    viewModel.selectedStudent?.let { student ->
        if (viewModel.currentScreen == Screen.SearchAndDetails) {
            StudentDetailsDialog(
                student = student,
                viewModel = viewModel,
                onEdit = {
                    viewModel.navigateTo(Screen.EditStudent, student)
                },
                onDeleteRequest = {
                    studentToDelete = student
                },
                onDismiss = {
                    viewModel.goBack()
                }
            )
        }
    }

    // Critical Delete Confirmation Dialog
    studentToDelete?.let { student ->
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = {
                Text(
                    text = "تأكيد حذف الطالب كلياً",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF991B1B)
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد تماماً من رغبتك في حذف الطالب [ ${student.name} ] من النظام بالكامل وبشكل نهائي لا يمكن الرجوع عنه؟",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStudent(student) {
                            studentToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("نعم، احذف السجل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) {
                    Text("إلغاء", fontSize = 11.sp, color = Color(0xFF64748B))
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun StudentSearchItem(
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
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
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
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFEEF2F6))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "رقم: ${student.id}",
                        fontSize = 9.sp,
                        color = Color(0xFF0D9488), // Brand Teal
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Divider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                Text(
                    text = "الهوية: ${student.idNumber}",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
fun StudentDetailsDialog(
    student: Student,
    viewModel: StudentViewModel,
    onEdit: () -> Unit,
    onDeleteRequest: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var payAmountStr by remember { mutableStateOf("") }
    
    val months = if (student.isActiveInRoom) {
        viewModel.calculateMonthsElapsed(student.rentStartDate)
    } else {
        viewModel.calculateMonthsElapsed(student.rentStartDate, student.rentEndDate ?: System.currentTimeMillis())
    }
    val totalRent = months * student.roomRent
    val remaining = totalRent - student.totalPaid
    val formattedDate = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale("ar")).format(java.util.Date(student.rentStartDate))
    val formattedEndDate = if (student.rentEndDate != null) {
        java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale("ar")).format(java.util.Date(student.rentEndDate))
    } else null

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with profile icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFEEF2F6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF0D9488) // Brand Teal
                        )
                    }

                    Text(
                        text = "بطاقة بيانات الطالب التفصيلية",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = Color(0xFF64748B))
                    }
                }

                // Student Name Banner
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = student.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (student.isActiveInRoom) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFE6F4EA))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "نشط في السكن",
                                    fontSize = 9.sp,
                                    color = Color(0xFF137333),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFCE8E6))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "غادر الغرفة",
                                    fontSize = 9.sp,
                                    color = Color(0xFFC5221F),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "رقم القيد الأكاديمي: ${student.id}",
                        fontSize = 10.sp,
                        color = Color(0xFF0D9488), // Brand Teal
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Section 1: Academic & Personal details
                Text(
                    text = "البيانات الأكاديمية والشخصية",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow(
                        icon = Icons.Default.School,
                        label = "التخصص والمستوى",
                        value = "${student.specialization}  •  ${student.level}"
                    )

                    DetailRow(
                        icon = Icons.Default.Badge,
                        label = "إثبات الهوية الوطنية",
                        value = "${student.idType} : ${student.idNumber}"
                    )

                    DetailRow(
                        icon = Icons.Default.Phone,
                        label = "رقم الموبايل",
                        value = student.phone
                    )

                    DetailRow(
                        icon = Icons.Default.Map,
                        label = "محل الإقامة والسكن الأصلي",
                        value = "اليمن، محافظة ${student.governorate}، مديرية ${student.district}" +
                                if (student.sector.isNotEmpty() || student.village.isNotEmpty())
                                    " (عزلة ${student.sector}، قرية ${student.village})" else ""
                    )
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Section 2: Dormitory & Rent details
                Text(
                    text = "بيانات السكن الجامعي والمالية",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D9488)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow(
                        icon = Icons.Default.Home,
                        label = if (student.isActiveInRoom) "رقم الغرفة وتاريخ السكن" else "رقم الغرفة وتفاصيل الإخلاء",
                        value = if (student.isActiveInRoom) {
                            "غرفة رقم [ ${student.roomNumber} ]  •  تاريخ السكن: $formattedDate"
                        } else {
                            "غرفة رقم [ ${student.roomNumber} ] (سابقاً)  •  السكن: $formattedDate  •  الإخلاء: $formattedEndDate"
                        }
                    )

                    DetailRow(
                        icon = Icons.Default.CreditCard,
                        label = if (student.isActiveInRoom) "الإيجار والمدفوعات لـ ($months شهور)" else "إجمالي الإيجار والمدفوعات حتى الإخلاء ($months شهور)",
                        value = "الإيجار الشهري: ${student.roomRent.toLong()} ريال\n" +
                                "إجمالي المتراكم: ${totalRent.toLong()} ريال\n" +
                                "إجمالي المسدد: ${student.totalPaid.toLong()} ريال"
                    )

                    // Outstanding Balance highlighting
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (remaining > 0) Color(0xFFFFF1F2) else Color(0xFFECFDF5))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (remaining > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (remaining > 0) Color(0xFFEF4444) else Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "المبلغ المتبقي المستحق",
                                fontSize = 9.sp,
                                color = if (remaining > 0) Color(0xFF991B1B) else Color(0xFF065F46),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${remaining.toLong()} ريال يمني",
                                fontSize = 12.sp,
                                color = if (remaining > 0) Color(0xFFB91C1C) else Color(0xFF047857),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Section 3: Recording a payment (سداد)
                Text(
                    text = "تسجيل سداد دفعة للإيجار",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = payAmountStr,
                        onValueChange = { payAmountStr = it },
                        placeholder = { Text("المبلغ بالريال", fontSize = 11.sp) },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedBorderColor = Color(0xFF0D9488),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp)
                    )

                    Button(
                        onClick = {
                            val amt = payAmountStr.toDoubleOrNull()
                            if (amt != null && amt > 0) {
                                viewModel.recordPayment(student, amt)
                                payAmountStr = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("سداد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Section 4: Communication & Sharing (التواصل والمشاركة)
                Text(
                    text = "التواصل ومشاركة كشف حساب الغرفة",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val message = """
                                *كشف حساب السكن الجامعي* 🏠
                                *الاسم:* ${student.name}
                                *رقم الغرفة:* ${student.roomNumber}
                                *تاريخ السكن:* $formattedDate
                                *الإيجار الشهري:* ${student.roomRent.toLong()} ريال
                                *عدد الأشهر:* $months
                                *الإيجار المتراكم:* ${totalRent.toLong()} ريال
                                *إجمالي المسدد:* ${student.totalPaid.toLong()} ريال
                                *المبلغ المتبقي المترتب عليك:* ${remaining.toLong()} ريال
                                
                                *يرجى سداد المبلغ المتبقي في أقرب وقت. شكراً لتفهمكم.* 🙏
                            """.trimIndent()
                            
                            try {
                                val cleanPhone = student.phone.filter { it.isDigit() }
                                val formattedPhone = if (cleanPhone.startsWith("0")) {
                                    "967" + cleanPhone.substring(1)
                                } else if (!cleanPhone.startsWith("967") && cleanPhone.length == 9) {
                                    "967" + cleanPhone
                                } else {
                                    cleanPhone
                                }
                                val uri = android.net.Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=${android.net.Uri.encode(message)}")
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri).apply {
                                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, message)
                                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "مشاركة كشف الحساب عبر"))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // WhatsApp Green
                        modifier = Modifier
                            .weight(1.3f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاركة واتساب", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:${student.phone}")).apply {
                                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {}
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("اتصال هاتفي", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                if (student.isActiveInRoom) {
                    Button(
                        onClick = { viewModel.vacateStudentFromRoom(student) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "إنهاء السكن وإخلاء الغرفة",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))
                }

                // Edit & Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onEdit,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)), // Brand Teal
                        modifier = Modifier
                            .weight(1.2f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تعديل البيانات", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDeleteRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2)),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حذف السجل", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .padding(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier
                .size(18.dp)
                .padding(top = 1.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = label, fontSize = 9.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 11.sp, color = Color(0xFF334155), fontWeight = FontWeight.Bold)
        }
    }
}
