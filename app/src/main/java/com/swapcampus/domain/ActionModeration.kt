package com.swapcampus.domain

import kotlinx.datetime.LocalDate

/**
 * Trace d'une action de modération (§5, UC6 et §3.2).
 *
 * Une suppression effectuée par un administrateur doit être motivée : le motif
 * est conservé pour pouvoir répondre à un vendeur qui demande pourquoi son
 * annonce a disparu.
 *
 * @param annonceId identifiant de l'annonce concernée.
 * @param annonceTitre titre au moment de la suppression (l'annonce n'existe plus).
 * @param moderateur administrateur auteur de l'action.
 * @param motif raison saisie, obligatoire.
 * @param date jour de l'action.
 */
data class ActionModeration(
    val annonceId: Int,
    val annonceTitre: String,
    val moderateur: CompteUtilisateur,
    val motif: String,
    val date: LocalDate
)
