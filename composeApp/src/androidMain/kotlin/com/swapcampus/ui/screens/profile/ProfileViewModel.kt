package com.isep.composeapp.ui.screens.profile

import androidx.lifecycle.ViewModel
import com.isep.shared.repository.AuthRepository

class ProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser = authRepository.currentUser

    fun logout() {
        authRepository.logout()
    }
}