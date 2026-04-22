package com.example.alertify.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager

class SosManager(private val context: Context) {

    fun sendSms(phone: String, message: String) {
        val sms = SmsManager.getDefault()
        sms.sendTextMessage(phone, null, message, null, null)
    }

    fun call(phone: String) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$phone")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}
