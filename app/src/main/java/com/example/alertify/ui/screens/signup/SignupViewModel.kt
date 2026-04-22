package com.example.alertify.ui.screens.signup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alertify.data.repository.AuthRepository
import com.example.alertify.utils.SessionManager
import com.example.alertify.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SignupViewModel(
    application: Application,
    private val repository: AuthRepository = AuthRepository()
) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)

    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state: StateFlow<UiState> = _state

    fun signup(email: String, password: String) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            val result = repository.signup(email, password)
            if (result.isSuccess) {
                sessionManager.setLoggedIn(true)  // ← save session
                _state.value = UiState.Success
            } else {
                _state.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Signup failed"
                )
            }
        }
    }
}