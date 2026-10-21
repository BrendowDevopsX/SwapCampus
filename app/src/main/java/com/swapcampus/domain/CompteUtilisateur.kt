package com.isep.shared.domain.entities

import com.isep.shared.domain.RoleUtilisateur
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