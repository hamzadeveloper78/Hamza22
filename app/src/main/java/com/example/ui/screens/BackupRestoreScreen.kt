package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Screen
import com.example.ui.StudentViewModel
import com.example.util.BackupManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    viewModel: StudentViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val students by viewModel.allStudents.collectAsState()

    var showClearConfirmation by remember { mutableStateOf(false) }
    var localSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Launcher for selecting JSON backup file
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importBackup(uri) { recordsCount ->
                localSuccessMessage = "تم استعادة $recordsCount سجل طلاب وتحديث قاعدة البيانات بنجاح!"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "التقارير وصيانة البيانات",
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
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Success banner
            val displaySuccess = localSuccessMessage ?: viewModel.operationSuccessMessage
            AnimatedVisibility(
                visible = displaySuccess != null,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                displaySuccess?.let { msg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = msg,
                                color = Color(0xFF065F46),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                localSuccessMessage = null
                                viewModel.operationSuccessMessage = null
                            }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = Color(0xFF065F46), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Error banner
            AnimatedVisibility(
                visible = viewModel.operationError != null,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                viewModel.operationError?.let { err ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(0xFFEF4444)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = err,
                                color = Color(0xFF991B1B),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // PDF Reporting Section Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SectionTitle("التقارير والمخرجات")
                    Text(
                        text = "يمكنك تصدير تقرير أكاديمي شامل بجميع الطلاب المسجلين بصيغة PDF ومشاركته أو طباعته فوراً وبدون إنترنت.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    Button(
                        onClick = {
                            viewModel.exportPdfReport { uri ->
                                BackupManager.shareFile(
                                    context = context,
                                    uri = uri,
                                    mimeType = "application/pdf",
                                    title = "مشاركة تقرير الطلاب الشامل"
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !viewModel.isOperating
                    ) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تصدير ومشاركة ملف PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Backup & Restore Section Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SectionTitle("صيانة البيانات والنسخ الاحتياطي")
                    Text(
                        text = "يقوم النظام بحفظ جميع البيانات محلياً داخل جهازك. لضمان عدم ضياعها، قم بتصدير نسخة احتياطية وحفظها بأمان، أو استعد البيانات من نسخة سابقة.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Export backup button
                        Button(
                            onClick = {
                                viewModel.exportBackup { uri ->
                                    BackupManager.shareFile(
                                        context = context,
                                        uri = uri,
                                        mimeType = "application/json",
                                        title = "مشاركة ملف النسخة الاحتياطية"
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !viewModel.isOperating
                        ) {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تصدير نسخة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Import backup button
                        Button(
                            onClick = {
                                filePickerLauncher.launch("application/json")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !viewModel.isOperating
                        ) {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("استعادة نسخة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Seeding / Diagnostics Section Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SectionTitle("أدوات الفحص والتسريع والتشخيص")
                    Text(
                        text = "لتجربة أداء محرك البحث والفرز مع أحجام بيانات حقيقية وكبيرة، يمكنك توليد 3000 طالب بشكل فوري وتلقائي لفحص استهلاك الذاكرة وسرعة الأداء الكلية.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    Button(
                        onClick = {
                            viewModel.seedSampleData {
                                localSuccessMessage = "تم تعبئة قاعدة البيانات بـ 3000 سجل طالب بنجاح! جرب البحث الآن."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !viewModel.isOperating
                    ) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("توليد 3000 طالب عشوائي للفحص", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Reset all database records button
                    Button(
                        onClick = { showClearConfirmation = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !viewModel.isOperating
                    ) {
                        Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFB91C1C))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مسح قاعدة البيانات بالكامل", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                    }
                }
            }
        }
    }

    // Clear confirmation dialog
    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = {
                Text(
                    text = "تحذير: حذف قاعدة البيانات بأكملها!",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF991B1B)
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من رغبتك في مسح كافة بيانات الطلاب المسجلين بالكامل؟ هذا العمل خطير وسيفقدك كافة البيانات الحالية نهائياً.",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("نعم، امسح كل البيانات", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("إلغاء", fontSize = 11.sp, color = Color(0xFF64748B))
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1E293B)
    )
}
