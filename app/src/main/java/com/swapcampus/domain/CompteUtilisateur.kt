package com.swapcampus.domain

import kotlinx.datetime.LocalDate

/**
 * Compte utilisateur simplifié du TP1 : pas de hachage réel, les comptes sont
 * fournis par le jeu de démonstration (§8.2).
 */
data class CompteUtilisateur(
    val id: Int,
    val nom: String,
    val email: String,
    val motDePasseHash: String,
    val dateCreation: LocalDate,
    val role: RoleUtilisateur,
    val actif: Boolean = true
)
