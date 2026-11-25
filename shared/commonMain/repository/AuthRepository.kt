package com.swapcampus.repository

import com.swapcampus.domain.CompteUtilisateur
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {

    val currentUser: StateFlow<CompteUtilisateur?>

    fun authenticate(email: String, password: String): CompteUtilisateur?

    fun logout()
}