package com.isep.shared.domain.entities

import kotlinx.datetime.LocalDate

sealed class EtatAnnonce

data object Disponible : EtatAnnonce()
data class Reservee(val acheteur: String) : EtatAnnonce()
data class Vendue(val dateVente: LocalDate) : EtatAnnonce()

fun EtatAnnonce.libelle(): String = when (this) {
    is Disponible -> "Disponible"
    is Reservee -> "Réservée par ${acheteur}"
    is Vendue -> "Vendue le ${dateVente}"
}
