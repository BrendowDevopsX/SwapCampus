package com.swapcampus.domain

import kotlinx.datetime.LocalDate

/**
 * État d'une annonce (UC5). Les états sont des classes de premier niveau du
 * package `com.swapcampus.domain` : `Disponible`, `Reservee`, `Vendue`.
 */
sealed class EtatAnnonce

/** Annonce visible dans le catalogue. */
data object Disponible : EtatAnnonce()

/** Annonce réservée par [acheteur] ; elle quitte le catalogue public. */
data class Reservee(val acheteur: String) : EtatAnnonce()

/** Annonce vendue le [dateVente]. */
data class Vendue(val dateVente: LocalDate) : EtatAnnonce()
