package com.example.alertify.utils

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(private val onShake: () -> Unit) : SensorEventListener {

    private val SHAKE_THRESHOLD_GRAVITY = 2.7f   // Raised: avoids false positives
    private val SHAKE_SLOP_TIME_MS = 500L         // Min gap between individual shakes
    private val SHAKE_COUNT_RESET_TIME_MS = 3000L // Reset count if no shake in 3s
    private val MIN_SHAKE_COUNT = 3               // Require 3 shakes to trigger

    private var mShakeTimestamp: Long = 0
    private var mShakeCount: Int = 0

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)

        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            val now = System.currentTimeMillis()

            // Reset count if too much time passed since last shake
            if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                mShakeCount = 0
            }

            // Debounce: ignore if this shake is too soon after the last
            if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) return

            mShakeTimestamp = now
            mShakeCount++

            if (mShakeCount >= MIN_SHAKE_COUNT) {
                mShakeCount = 0 // Reset after triggering
                onShake()
            }
        }
    }
}