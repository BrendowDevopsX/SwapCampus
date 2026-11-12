package com.swapcampus.ui.screens.profile

import androidx.lifecycle.ViewModel
import com.swapcampus.repository.AuthRepository

class ProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser = authRepository.currentUser

    fun logout() {
        authRepository.logout()
    }
}