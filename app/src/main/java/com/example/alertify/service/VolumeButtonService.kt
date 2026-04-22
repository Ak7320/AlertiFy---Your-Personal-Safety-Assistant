package com.example.alertify.service


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.alertify.MainActivity

class VolumeButtonService : Service() {

    private val CHANNEL_ID = "VolumeServiceChannel"
    private val NOTIFICATION_ID = 2
    private val PRESS_COUNT_RESET_MS = 1500L  // Reset count after 1.5 seconds
    private val REQUIRED_PRESSES = 3

    private var pressCount = 0
    private val handler = Handler(Looper.getMainLooper())

    private val resetRunnable = Runnable {
        pressCount = 0  // Reset if no press within time window
    }

    private val volumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "android.media.VOLUME_CHANGED_ACTION") {
                val streamType = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1)

                // Only listen to media or ringer volume — ignore others
                if (streamType != AudioManager.STREAM_RING &&
                    streamType != AudioManager.STREAM_MUSIC) return

                handler.removeCallbacks(resetRunnable)
                pressCount++

                if (pressCount >= REQUIRED_PRESSES) {
                    pressCount = 0
                    handler.removeCallbacks(resetRunnable)
                    triggerSOS()
                } else {
                    // Schedule reset if no more presses come in
                    handler.postDelayed(resetRunnable, PRESS_COUNT_RESET_MS)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        registerVolumeReceiver()
    }

    private fun registerVolumeReceiver() {
        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(volumeReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(volumeReceiver, filter)
        }
    }

    private fun triggerSOS() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("trigger_sos", true)
        }
        startActivity(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartIntent = Intent(applicationContext, VolumeButtonService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(restartIntent)
        } else {
            startService(restartIntent)
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        unregisterReceiver(volumeReceiver)
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alertify Active")
            .setContentText("Volume SOS detection is running")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Volume SOS Detection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors volume button for emergency SOS trigger"
            }
            getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}