package com.isep.shared.repository

import com.isep.shared.domain.RoleUtilisateur
import com.isep.shared.domain.entities.CompteUtilisateur
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate

class AuthRepositoryImpl : AuthRepository {

    private val accounts: List<CompteUtilisateur> = listOf(
        CompteUtilisateur(
            id = 1,
            nom = "Alice",
            email = "alice@etu.fr",
            motDePasseHash = "alice123",
            dateCreation = LocalDate(2025, 9, 1),
            role = RoleUtilisateur.ETUDIANT,
            actif = true
        ),
        CompteUtilisateur(
            id = 2,
            nom = "Bob",
            email = "bob@etu.fr",
            motDePasseHash = "bob123",
            dateCreation = LocalDate(2025, 9, 1),
            role = RoleUtilisateur.ETUDIANT,
            actif = true
        ),
        CompteUtilisateur(
            id = 3,
            nom = "Admin",
            email = "admin@swapcampus.fr",
            motDePasseHash = "admin123",
            dateCreation = LocalDate(2025, 9, 1),
            role = RoleUtilisateur.ADMINISTRATEUR,
            actif = true
        )
    )

    private val _currentUser = MutableStateFlow<CompteUtilisateur?>(null)
    override val currentUser: StateFlow<CompteUtilisateur?> = _currentUser.asStateFlow()

    override fun authenticate(email: String, password: String): CompteUtilisateur? {
        val compte = accounts.firstOrNull {
            it.email.equals(email.trim(), ignoreCase = true) &&
                it.motDePasseHash == password &&
                it.actif
        }
        if (compte != null) _currentUser.value = compte
        return compte
    }

    override fun logout() {
        _currentUser.value = null
    }
}