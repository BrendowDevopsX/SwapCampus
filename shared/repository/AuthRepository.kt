package com.isep.shared.repository

import com.isep.shared.domain.entities.CompteUtilisateur
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {

    val currentUser: StateFlow<CompteUtilisateur?>

    fun authenticate(email: String, password: String): CompteUtilisateur?

    fun logout()
}