package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.Screen
import com.example.ui.StudentViewModel
import com.example.ui.screens.AddEditStudentScreen
import com.example.ui.screens.BackupRestoreScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.StudentListScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Ensure absolute RTL alignment for Arabic layouts on all devices
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val viewModel: StudentViewModel = viewModel()

                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        val screenModifier = Modifier.padding(innerPadding)

                        when (viewModel.currentScreen) {
                            Screen.Dashboard -> {
                                DashboardScreen(viewModel = viewModel, modifier = screenModifier)
                            }
                            Screen.AddStudent, Screen.EditStudent -> {
                                AddEditStudentScreen(viewModel = viewModel, modifier = screenModifier)
                            }
                            Screen.SearchAndDetails -> {
                                SearchScreen(viewModel = viewModel, modifier = screenModifier)
                            }
                            Screen.AllStudents -> {
                                StudentListScreen(viewModel = viewModel, modifier = screenModifier)
                            }
                            Screen.BackupAndReport -> {
                                BackupRestoreScreen(viewModel = viewModel, modifier = screenModifier)
                            }
                        }
                    }
                }
            }
        }
    }
}
