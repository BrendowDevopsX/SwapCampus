package com.swapcampus.repository

import com.swapcampus.data.EchantillonDonnees
import com.swapcampus.domain.CompteUtilisateur
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Authentification simplifiée du TP1 : comptes fictifs en mémoire, mots de
 * passe non hachés (cahier des charges §8.2).
 *
 * @param comptesListe comptes connus, injectable pour les tests.
 */
class AuthRepositoryImpl(
    comptesListe: List<CompteUtilisateur> = EchantillonDonnees.comptes
) : AuthRepository {

    override val comptesDisponibles: List<CompteUtilisateur> = comptesListe

    private val _currentUser = MutableStateFlow<CompteUtilisateur?>(null)
    override val currentUser: StateFlow<CompteUtilisateur?> = _currentUser.asStateFlow()

    override fun authenticate(email: String, password: String): CompteUtilisateur? {
        val compte = comptesDisponibles.firstOrNull {
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
