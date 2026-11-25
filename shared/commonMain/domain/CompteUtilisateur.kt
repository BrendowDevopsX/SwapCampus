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