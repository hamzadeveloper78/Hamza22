package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.Screen
import com.example.ui.StudentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditStudentScreen(
    viewModel: StudentViewModel,
    modifier: Modifier = Modifier
) {
    val isEditMode = viewModel.idToEdit != null
    val scrollState = rememberScrollState()

    // Dialog control states for custom selectors
    var showIdTypeDialog by remember { mutableStateOf(false) }
    var showLevelDialog by remember { mutableStateOf(false) }

    val idTypesList = listOf("بطاقة شخصية", "جواز سفر", "بطاقة عائلية", "أخرى")
    val academicLevelsList = listOf("المستوى الأول", "المستوى الثاني", "المستوى الثالث", "المستوى الرابع", "المستوى الخامس")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "تعديل بيانات الطالب" else "إضافة طالب جديد",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.Dashboard) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward, // RTL points forward for back action!
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

            // Validation Error Banner
            AnimatedVisibility(
                visible = viewModel.formError != null,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                viewModel.formError?.let { error ->
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
                                text = error,
                                color = Color(0xFFB91C1C),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Student General Information Section Card
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
                    FormHeader("البيانات الأساسية")

                    CustomOutlinedTextField(
                        value = viewModel.nameInput,
                        onValueChange = { viewModel.nameInput = it },
                        label = "اسم الطالب الرباعي *",
                        icon = Icons.Default.Person,
                        placeholder = "مثال: أحمد محمد علي الصبري"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Custom ID Type Selector Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF1F5F9))
                                .clickable { showIdTypeDialog = true }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column {
                                Text("نوع الهوية *", fontSize = 10.sp, color = Color(0xFF64748B))
                                Text(viewModel.idTypeInput, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            }
                        }

                        CustomOutlinedTextField(
                            value = viewModel.idNumberInput,
                            onValueChange = { viewModel.idNumberInput = it },
                            label = "رقم الهوية *",
                            icon = Icons.Default.Badge,
                            placeholder = "أرقام فقط",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1.2f)
                        )
                    }

                    CustomOutlinedTextField(
                        value = viewModel.phoneInput,
                        onValueChange = { viewModel.phoneInput = it },
                        label = "رقم الهاتف *",
                        icon = Icons.Default.Phone,
                        placeholder = "مثال: 777123456",
                        keyboardType = KeyboardType.Phone
                    )
                }
            }

            // Geographic/Address Section Card
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
                    FormHeader("محل الإقامة والسكن")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CustomOutlinedTextField(
                            value = viewModel.governorateInput,
                            onValueChange = { viewModel.governorateInput = it },
                            label = "المحافظة *",
                            icon = Icons.Default.Map,
                            placeholder = "مثال: تعز",
                            modifier = Modifier.weight(1f)
                        )
                        CustomOutlinedTextField(
                            value = viewModel.districtInput,
                            onValueChange = { viewModel.districtInput = it },
                            label = "المديرية *",
                            icon = Icons.Default.LocationCity,
                            placeholder = "مثال: القاهرة",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CustomOutlinedTextField(
                            value = viewModel.villageInput,
                            onValueChange = { viewModel.villageInput = it },
                            label = "القرية",
                            icon = Icons.Default.Home,
                            placeholder = "مثال: الأشعوب",
                            modifier = Modifier.weight(1f)
                        )
                        CustomOutlinedTextField(
                            value = viewModel.sectorInput,
                            onValueChange = { viewModel.sectorInput = it },
                            label = "العزلة",
                            icon = Icons.Default.Explore,
                            placeholder = "مثال: صبر",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Academic Information Section Card
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
                    FormHeader("التخصص الدراسي والمستوى")

                    CustomOutlinedTextField(
                        value = viewModel.specializationInput,
                        onValueChange = { viewModel.specializationInput = it },
                        label = "التخصص الدراسي *",
                        icon = Icons.Default.School,
                        placeholder = "مثال: هندسة برمجيات"
                    )

                    // Custom Academic Level Selector Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9))
                            .clickable { showLevelDialog = true }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("المستوى الدراسي *", fontSize = 10.sp, color = Color(0xFF64748B))
                                Text(viewModel.levelInput, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            }
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF64748B))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Save/Cancel Buttons
            Button(
                onClick = { viewModel.saveStudent() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !viewModel.isOperating
            ) {
                if (viewModel.isOperating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (isEditMode) "تحديث البيانات" else "حفظ البيانات كلياً",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            OutlinedButton(
                onClick = { viewModel.navigateTo(Screen.Dashboard) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                border = ButtonDefaults.outlinedButtonBorder(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B)),
                enabled = !viewModel.isOperating
            ) {
                Text("إلغاء وإغلاق", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }

    // Custom dialog selection for ID Types
    if (showIdTypeDialog) {
        OptionSelectorDialog(
            title = "اختر نوع الهوية",
            options = idTypesList,
            selectedOption = viewModel.idTypeInput,
            onOptionSelected = {
                viewModel.idTypeInput = it
                showIdTypeDialog = false
            },
            onDismiss = { showIdTypeDialog = false }
        )
    }

    // Custom dialog selection for Levels
    if (showLevelDialog) {
        OptionSelectorDialog(
            title = "اختر المستوى الدراسي",
            options = academicLevelsList,
            selectedOption = viewModel.levelInput,
            onOptionSelected = {
                viewModel.levelInput = it
                showLevelDialog = false
            },
            onDismiss = { showLevelDialog = false }
        )
    }
}

@Composable
fun FormHeader(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF4F46E5),
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp) },
        placeholder = { Text(placeholder, fontSize = 11.sp, color = Color(0xFF94A3B8)) },
        leadingIcon = { Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp)) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF4F46E5),
            unfocusedBorderColor = Color(0xFFE2E8F0).copy(alpha = 0.5f),
            focusedLabelColor = Color(0xFF4F46E5),
            unfocusedLabelColor = Color(0xFF64748B),
            focusedContainerColor = Color(0xFFF8FAFC),
            unfocusedContainerColor = Color(0xFFF8FAFC)
        ),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun OptionSelectorDialog(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))

                options.forEach { option ->
                    val isSelected = option == selectedOption
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFFEEF2F6) else Color.Transparent)
                            .clickable { onOptionSelected(option) }
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = option,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF4F46E5) else Color(0xFF475569)
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
