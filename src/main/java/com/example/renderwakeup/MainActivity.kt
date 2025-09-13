package com.example.renderwakeup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.renderwakeup.ui.screens.AddUrlScreen
import com.example.renderwakeup.ui.screens.DashboardScreen
import com.example.renderwakeup.ui.screens.OnboardingScreen
import com.example.renderwakeup.ui.screens.SettingsScreen
import com.example.renderwakeup.ui.theme.RenderWakeUpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RenderWakeUpTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RenderWakeUpApp()
                }
            }
        }
    }
}

@Composable
fun RenderWakeUpApp() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "onboarding") {
        composable("onboarding") {
            OnboardingScreen(
                onNavigateToDashboard = { navController.navigate("dashboard") {
                    popUpTo("onboarding") { inclusive = true }
                }},
                onOpenBatterySettings = { /* 배터리 설정 열기 구현 */ }
            )
        }
        composable("dashboard") {
            DashboardScreen(
                onNavigateToAddUrl = { navController.navigate("add_url") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("add_url") {
            AddUrlScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
