package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.data.AppDatabase
import com.example.data.GroundingRepositoryImpl
import com.example.ui.GroundingViewModel
import com.example.ui.GroundingViewModelFactory
import com.example.ui.MainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Room Offline-First database & Repository
    val database = AppDatabase.getDatabase(applicationContext)
    val dao = database.groundingSessionDao()
    val repository = GroundingRepositoryImpl(dao)

    // Setup ViewModel mapping with custom factory injection
    val viewModel: GroundingViewModel by viewModels {
      GroundingViewModelFactory(repository)
    }

    setContent {
      // Observe theme overrides (null = system theme, true = dark, false = light)
      val darkThemeSetting by viewModel.darkThemeEnabled.collectAsState()
      val useDarkTheme = darkThemeSetting ?: isSystemInDarkTheme()

      MyApplicationTheme(darkTheme = useDarkTheme) {
        val shieldEnabled by viewModel.isShieldEnabled.collectAsState()

        // Handle runtime notification permissions for Android 13+
        val permissionLauncher = rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
          if (isGranted) {
            Toast.makeText(this, "Overthinking Shield active 🌸", Toast.LENGTH_SHORT).show()
          } else {
            Toast.makeText(this, "Notifications blocked. Shield reminders may not show.", Toast.LENGTH_LONG).show()
            viewModel.isShieldEnabled.value = false
          }
        }

        LaunchedEffect(shieldEnabled) {
          if (shieldEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
              this@MainActivity,
              Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
              permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
          }
        }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          MainScreen(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}
