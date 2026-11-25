package com.swapcampus.repository

import com.swapcampus.domain.CompteUtilisateur
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {

    val currentUser: StateFlow<CompteUtilisateur?>

    /** Comptes fictifs du TP1, utilisés pour les raccourcis de démonstration. */
    val comptesDisponibles: List<CompteUtilisateur>

    /**
     * @return le compte authentifié, ou `null` si les identifiants sont invalides.
     */
    fun authenticate(email: String, password: String): CompteUtilisateur?

    fun logout()
}
