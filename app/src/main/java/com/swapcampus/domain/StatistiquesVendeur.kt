package com.swapcampus.domain

/**
 * Compteurs du vendeur affichés dans son profil et dans « Mes annonces » (UC7).
 */
data class StatistiquesVendeur(
    val publiees: Int = 0,
    val disponibles: Int = 0,
    val reservees: Int = 0,
    val vendues: Int = 0
) {
    /** Annonces encore en jeu : disponibles + réservées. */
    val enLigne: Int get() = disponibles + reservees
}

/**
 * Calcule les statistiques d'un vendeur à partir de toutes ses annonces.
 *
 * @param auteurId identifiant du compte vendeur.
 */
fun List<Annonce>.statistiquesDe(auteurId: Int): StatistiquesVendeur {
    val siennes = filter { it.auteur.id == auteurId }
    return StatistiquesVendeur(
        publiees = siennes.size,
        disponibles = siennes.count { it.etat is Disponible },
        reservees = siennes.count { it.etat is Reservee },
        vendues = siennes.count { it.etat is Vendue }
    )
}
