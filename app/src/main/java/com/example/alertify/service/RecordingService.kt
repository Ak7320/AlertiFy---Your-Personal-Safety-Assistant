package com.example.alertify.service


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.alertify.R
import com.example.alertify.utils.AudioRecorder

class RecordingService : Service() {

    private lateinit var recorder: AudioRecorder

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        recorder = AudioRecorder(this)
        startForeground(1, createNotification())
        recorder.startRecording()
    }

    override fun onDestroy() {
        recorder.stopRecording()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(): Notification {
        val channelId = "recording_channel"

        val channel = NotificationChannel(
            channelId,
            "SOS Recording",
            NotificationManager.IMPORTANCE_LOW
        )

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("AlertiFy")
            .setContentText("Recording for safety...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }
}