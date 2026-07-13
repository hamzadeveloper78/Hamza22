package com.example.ui.screens

import androidx.compose.animation.*
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
                    IconButton(onClick = { viewModel.navigateTo(Screen.Dashboard) }) {
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
                    focusedBorderColor = Color(0xFF4F46E5),
                    unfocusedBorderColor = Color(0xFFE2E8F0).copy(alpha = 0.5f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
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
                onEdit = {
                    viewModel.navigateTo(Screen.EditStudent, student)
                },
                onDeleteRequest = {
                    studentToDelete = student
                },
                onDismiss = {
                    viewModel.navigateTo(Screen.SearchAndDetails, null)
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
                Text(
                    text = student.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
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
                        color = Color(0xFF4F46E5),
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
    onEdit: () -> Unit,
    onDeleteRequest: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            tint = Color(0xFF4F46E5)
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
                    Text(
                        text = student.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "رقم القيد الأكاديمي: ${student.id}",
                        fontSize = 10.sp,
                        color = Color(0xFF4F46E5),
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider(color = Color(0xFFF1F5F9))

                // Details Bento Grid
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        label = "محل الإقامة والسكن",
                        value = "اليمن، محافظة ${student.governorate}، مديرية ${student.district}" +
                                if (student.sector.isNotEmpty() || student.village.isNotEmpty())
                                    " (عزلة ${student.sector}، قرية ${student.village})" else ""
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Dialog Buttons Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onEdit,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
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
