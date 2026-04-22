package com.example.alertify.ui.screens.sos

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alertify.data.repository.ContactRepository
import com.example.alertify.utils.LocationHelper
import com.example.alertify.utils.SosManager
import kotlinx.coroutines.launch

class SosViewModel(application: Application) : AndroidViewModel(application) {

    private val contactRepository = ContactRepository()
    private val locationHelper = LocationHelper(application)
    private val sosManager = SosManager(application)

    fun triggerSOS(context: Context) {

        viewModelScope.launch {

            val contacts = contactRepository.getContacts()
            if (contacts.isEmpty()) return@launch

            val location = locationHelper.getLocation()

            val link =
                "https://maps.google.com/?q=${location?.latitude},${location?.longitude}"

            val message = "EMERGENCY! I need help. My location: $link"

            contacts.forEach { contact ->
                sosManager.sendSms(contact.phoneNumber, message) // Use phoneNumber
            }

            // Call first contact (primary guardian)
            sosManager.call(contacts.first().phoneNumber)
        }
    }
    // Inside SosViewModel.kt

    fun callEmergencyService(serviceNumber: String) {
        viewModelScope.launch {
            // You could also log this or send an SMS that you've contacted official services
            sosManager.call(serviceNumber)
        }
    }
}