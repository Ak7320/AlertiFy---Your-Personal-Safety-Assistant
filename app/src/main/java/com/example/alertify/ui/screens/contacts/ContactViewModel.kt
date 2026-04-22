package com.example.alertify.ui.screens.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alertify.data.model.EmergencyContact
import com.example.alertify.data.repository.ContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContactViewModel : ViewModel() {

    private val repository = ContactRepository()

    private val _contacts =
        MutableStateFlow<List<EmergencyContact>>(emptyList())
    val contacts: StateFlow<List<EmergencyContact>> = _contacts

    init {
        repository.listenToContacts { updatedContacts ->
            _contacts.value = updatedContacts
        }
    }

    fun saveContact(name: String, phone: String) {
        viewModelScope.launch {
            repository.addContact(name, phone)
        }
    }

    fun deleteContact(id: String) {
        viewModelScope.launch {
            repository.deleteContact(id)
        }
    }
}