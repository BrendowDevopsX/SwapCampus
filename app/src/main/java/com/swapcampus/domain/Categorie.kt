package com.swapcampus.domain

/**
 * Catégories d'annonces. Le libellé affiché est fourni par la couche UI
 * (`com.swapcampus.ui.text.resCategorie`) : le domaine ne contient aucun texte
 * traduisible (§10.2).
 */
enum class Categorie {
    LIVRES,
    COURS,
    ELECTRONIQUE,
    SPORT_LOISIRS,
    MOBILIER,
    VETEMENTS,
    AUTRE
}
