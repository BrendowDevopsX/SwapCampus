package com.isep.shared.domain.entities

import kotlinx.datetime.LocalDateTime

sealed class CibleModeration
data class CibleAnnonce(val annonce: Annonce) : CibleModeration()
data class CibleCompte(val compte: CompteUtilisateur) : CibleModeration()


data class ActionModeration(
    val id: Int,
    val cible: CibleModeration,
    val type: TypeActionModeration,
    val motif: String,
    val administrateur: CompteUtilisateur,
    val date: LocalDateTime
)
