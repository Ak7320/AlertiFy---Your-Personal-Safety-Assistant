package com.example.alertify.data.model


data class DeviceContact(
    val id: String,
    val name: String,
    val phone: String,
    val isSelected: Boolean = false
)