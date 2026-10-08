package com.isep.shared.domain.entities

import kotlinx.datetime.LocalDate

data class Annonce(
    val id: Int,
    val titre: String,
    val description: String,
    val prix: Double,
    val categorie: Categorie,
    val photoUrl: String? = null,
    val auteur: CompteUtilisateur,
    val etat: EtatAnnonce = Disponible,
    val dateCreation: LocalDate
)
