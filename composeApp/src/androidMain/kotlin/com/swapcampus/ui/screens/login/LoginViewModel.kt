package com.isep.composeapp.ui.screens.login

import androidx.lifecycle.ViewModel
import com.isep.shared.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _error.value = "Email and password must not be empty"
            return
        }

        val user = authRepository.authenticate(email, password)
        if (user == null) {
            _error.value = "Invalid email or password"
        } else {
            _error.value = null
            onSuccess()
        }
    }

    fun clearError() {
        _error.value = null
    }
}