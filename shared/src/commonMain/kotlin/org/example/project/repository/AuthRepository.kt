package org.example.project.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.project.domain.CompteUtilisateur
import org.example.project.domain.RoleUtilisateur

class AuthRepository {

    // Lista de contas fictícias (2 estudantes, 1 administrador)
    private val mockAccounts = listOf(
        CompteUtilisateur(id = "1", email = "etudiant1@swapcampus.com", motDePasse = "pass123", role = RoleUtilisateur.ETUDIANT),
        CompteUtilisateur(id = "2", email = "etudiant2@swapcampus.com", motDePasse = "pass123", role = RoleUtilisateur.ETUDIANT),
        CompteUtilisateur(id = "3", email = "admin@swapcampus.com", motDePasse = "admin123", role = RoleUtilisateur.ADMIN)
    )

    private val _currentUser = MutableStateFlow<CompteUtilisateur?>(null)
    val currentUser: StateFlow<CompteUtilisateur?> = _currentUser.asStateFlow()

    fun authenticate(email: String, motDePasse: String): CompteUtilisateur? {
        val user = mockAccounts.find { it.email == email && it.motDePasse == motDePasse }
        _currentUser.value = user
        return user
    }

    fun logout() {
        _currentUser.value = null
    }
}