package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.EarnPulseViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: EarnPulseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val currentUser by viewModel.currentUser.collectAsState()

                // Decide start destination based on whether user is already logged in
                val startDest = if (currentUser != null) "dashboard" else "welcome"

                NavHost(
                    navController = navController,
                    startDestination = startDest,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("welcome") {
                        WelcomeScreen(
                            onNavigateToLogin = { navController.navigate("login") },
                            onNavigateToRegister = { navController.navigate("register") }
                        )
                    }

                    composable("login") {
                        LoginScreen(
                            viewModel = viewModel,
                            onNavigateToRegister = { navController.navigate("register") },
                            onLoginSuccess = {
                                navController.navigate("dashboard") {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("register") {
                        RegisterScreen(
                            viewModel = viewModel,
                            onNavigateToLogin = { navController.navigate("login") },
                            onRegisterSuccess = {
                                navController.navigate("pin_setup") {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("pin_setup") {
                        PinSetupScreen(
                            viewModel = viewModel,
                            onPinLogged = {
                                navController.navigate("dashboard") {
                                    popUpTo("pin_setup") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("dashboard") {
                        MainDashboard(
                            viewModel = viewModel,
                            onNavigateToAdmin = { navController.navigate("admin_console") },
                            onNavigateToPinSetup = { navController.navigate("pin_setup") },
                            onLogout = {
                                navController.navigate("welcome") {
                                    popUpTo("dashboard") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("admin_console") {
                        AdminConsole(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
