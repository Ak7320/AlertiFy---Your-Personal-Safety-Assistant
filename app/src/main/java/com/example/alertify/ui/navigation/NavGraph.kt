package com.example.alertify.ui.navigation

import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.alertify.service.RecordingService
import com.example.alertify.ui.screens.contacts.ContactManagementScreen
import com.example.alertify.ui.screens.contacts.ContactViewModel
import com.example.alertify.ui.screens.hospitals.NearbyHospitalsScreen
import com.example.alertify.ui.screens.login.LoginScreen
import com.example.alertify.ui.screens.main.MainMenuScreen
import com.example.alertify.ui.screens.signup.SignupScreen
import com.example.alertify.ui.screens.sos.SosScreen
import com.example.alertify.utils.SessionManager
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(startWithSos: Boolean = false) {

    val navController = rememberNavController()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    // Collect login state — null means still loading
    val isLoggedIn by sessionManager.isLoggedIn.collectAsState(initial = null)

    // Navigate to SOS if triggered by shake/volume
    LaunchedEffect(startWithSos) {
        if (startWithSos) {
            navController.navigate(Routes.SOS)
        }
    }

    // Show loading screen until DataStore responds
    if (isLoggedIn == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F2027)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.Red)
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn == true) Routes.MAIN else Routes.LOGIN
    ) {

        // LOGIN
        composable(Routes.LOGIN) {
            val scope = rememberCoroutineScope()
            LoginScreen(
                onLoginSuccess = {
                    scope.launch {
                        sessionManager.setLoggedIn(true)
                    }
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onSignupClick = {
                    navController.navigate(Routes.SIGNUP)
                }
            )
        }

        // SIGNUP
        composable(Routes.SIGNUP) {
            val scope = rememberCoroutineScope()
            SignupScreen(
                onSignupSuccess = {
                    scope.launch {
                        sessionManager.setLoggedIn(true)
                    }
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.SIGNUP) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        // MAIN MENU
        composable(Routes.MAIN) {
            val scope = rememberCoroutineScope()
            MainMenuScreen(
                onContactsClick = {
                    navController.navigate(Routes.CONTACT_MGT)
                },
                onSosClick = {
                    navController.navigate(Routes.SOS)
                },
                onHospitalClick = {
                    navController.navigate(Routes.NEARBY_HOSPITALS)
                },
                onLogout = {
                    scope.launch {
                        sessionManager.setLoggedIn(false)
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        // CONTACT MANAGEMENT
        composable(Routes.CONTACT_MGT) { backStackEntry ->
            val contactViewModel: ContactViewModel = viewModel(backStackEntry)
            ContactManagementScreen(
                viewModel = contactViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // SOS SCREEN
        composable(Routes.SOS) {
            val context = LocalContext.current
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                if (granted) {
                    val intent = Intent(context, RecordingService::class.java)
                    context.startForegroundService(intent)
                }
            }

            SosScreen(
                onSosClick = {
                    launcher.launch(android.Manifest.permission.RECORD_AUDIO)
                },
                onCancelSos = {
                    context.stopService(Intent(context, RecordingService::class.java))
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }
            )
        }

        // NEARBY HOSPITALS
        composable(Routes.NEARBY_HOSPITALS) {
            NearbyHospitalsScreen(onBack = { navController.popBackStack() })
        }
    }
}