package com.swapcampus.ui.components

import kotlinx.datetime.LocalDate
import java.text.NumberFormat
import java.util.Locale

private val localeFrancais = Locale.FRANCE
private val formatMonnaie: NumberFormat = NumberFormat.getCurrencyInstance(localeFrancais)

private val moisCourts = listOf(
    "janv.", "févr.", "mars", "avr.", "mai", "juin",
    "juil.", "août", "sept.", "oct.", "nov.", "déc."
)

/**
 * Formate un prix en euros avec la convention française : `45,00 €`.
 *
 * Le formatage est centralisé ici pour qu'un futur passage à une application
 * multilingue (TP2) n'ait qu'un seul endroit à modifier.
 */
fun formaterPrix(prix: Double): String = synchronized(formatMonnaie) {
    formatMonnaie.format(prix)
}

/** Formate une date courte lisible : `12 nov. 2026`. */
fun formaterDate(date: LocalDate): String =
    "${date.dayOfMonth} ${moisCourts[date.monthNumber - 1]} ${date.year}"

/** Initiales d'un nom, utilisées dans les avatars : `Alice Martin` → `AM`. */
fun initiales(nom: String): String = nom
    .trim()
    .split(" ")
    .filter { it.isNotBlank() }
    .take(2)
    .joinToString("") { it.first().uppercase() }
    .ifBlank { "?" }
