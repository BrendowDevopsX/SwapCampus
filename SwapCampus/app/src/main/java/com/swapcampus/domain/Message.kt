package com.isep.shared.domain.entities

import kotlinx.datetime.LocalDateTime


data class Message(
    val id: Int,
    val annonce: Annonce,
    val expediteur: CompteUtilisateur,
    val destinataire: CompteUtilisateur,
    val contenu: String,
    val dateEnvoi: LocalDateTime
)
