package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presentation.ui.AppNavigation
import com.example.presentation.viewmodel.WealthViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Proactively schedule daily reminder alarm at 7 PM and EMI due dates checks
    try {
      com.example.receiver.NotificationReceiver().scheduleDailyReminder(this)
    } catch (e: Exception) {
      e.printStackTrace()
    }

    // Request notification permission for Android 13+ devices
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
      if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
        requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
      }
    }

    setContent {
      val wealthViewModel: WealthViewModel = viewModel()
      val isDarkTheme by wealthViewModel.isDarkMode.collectAsState()
      val colorTheme by wealthViewModel.colorTheme.collectAsState()
      MyApplicationTheme(darkTheme = isDarkTheme, colorTheme = colorTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
          AppNavigation(viewModel = wealthViewModel)
        }
      }
    }
  }
}