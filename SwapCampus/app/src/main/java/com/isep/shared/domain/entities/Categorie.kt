package com.isep.shared.domain.entities


enum class Categorie {
    LIVRES,
    COURS,
    ELECTRONIQUE,
    SPORT_LOISIRS,
    MOBILIER,
    VETEMENTS,
    AUTRE
}

fun Categorie.libelle(): String = when (this) {
    Categorie.LIVRES -> "Livres"
    Categorie.COURS -> "Cours"
    Categorie.ELECTRONIQUE -> "Électronique"
    Categorie.SPORT_LOISIRS -> "Sport & loisirs"
    Categorie.MOBILIER -> "Mobilier"
    Categorie.VETEMENTS -> "Vêtements"
    Categorie.AUTRE -> "Autre"
}
