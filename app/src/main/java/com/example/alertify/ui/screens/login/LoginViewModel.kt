package com.example.alertify.ui.screens.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alertify.data.repository.AuthRepository
import com.example.alertify.utils.SessionManager
import com.example.alertify.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    application: Application,
    private val repository: AuthRepository = AuthRepository()
) : AndroidViewModel(application) {  // ← changed from ViewModel to AndroidViewModel

    private val sessionManager = SessionManager(application)  // ← needs Context

    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state: StateFlow<UiState> = _state

    fun login(email: String, password: String) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            val result = repository.login(email, password)
            if (result.isSuccess) {
                sessionManager.setLoggedIn(true)  // ← save session on success
                _state.value = UiState.Success
            } else {
                _state.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Login failed"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.setLoggedIn(false)  // ← clear session on logout
        }
    }
}