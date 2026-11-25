package com.swapcampus.domain

import com.swapcampus.domain.RoleUtilisateur
import kotlinx.datetime.LocalDate

data class CompteUtilisateur(
    val id: Int,
    val nom: String,
    val email: String,
    val motDePasseHash: String,
    val dateCreation: LocalDate,
    val role: RoleUtilisateur,
    val actif: Boolean = true
)

/** Libellé affichable du rôle, utilisé dans le profil et l'écran de connexion. */
fun RoleUtilisateur.libelle(): String = when (this) {
    RoleUtilisateur.ETUDIANT -> "Étudiant"
    RoleUtilisateur.ADMINISTRATEUR -> "Administrateur"
}