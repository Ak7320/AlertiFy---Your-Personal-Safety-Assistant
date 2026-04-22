package com.example.alertify

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import com.example.alertify.service.ShakeService
import com.example.alertify.service.VolumeButtonService
import com.example.alertify.ui.navigation.AppNavGraph
import com.example.alertify.ui.theme.AlertiFyTheme

class MainActivity : ComponentActivity() {

    private var startWithSos = mutableStateOf(false)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkIntent(intent)
        startServices()
        requestBatteryOptimizationExemption()

        setContent {
            AlertiFyTheme {
                AppNavGraph(startWithSos = startWithSos.value)
                startWithSos.value = false
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        checkIntent(intent)
    }

    private fun checkIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("trigger_sos", false) == true) {
            startWithSos.value = true
        }
    }

    private fun startServices() {
        listOf(
            ShakeService::class.java,
            VolumeButtonService::class.java
        ).forEach { serviceClass ->
            val intent = Intent(this, serviceClass)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }

    private fun requestBatteryOptimizationExemption() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        }
    }
}