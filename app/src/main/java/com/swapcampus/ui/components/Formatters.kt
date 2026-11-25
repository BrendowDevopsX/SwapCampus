package com.swapcampus.ui.components

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Formatage des prix et des dates selon la locale de l'appareil
 * (cahier des charges §8.7 : prix et dates localisés).
 *
 * Le séparateur et la position du symbole suivent la langue active :
 * « 45,00 € » en français, « €45.00 » en anglais. La devise reste en euros :
 * le campus est en Europe, seule la présentation change avec la langue.
 * Les formateurs sont mis en cache par locale, car [NumberFormat] n'est pas
 * thread-safe.
 */
private val DEVISE = java.util.Currency.getInstance("EUR")

private val formatsMonnaie = HashMap<Locale, NumberFormat>()

private val formatsDate = HashMap<Locale, DateTimeFormatter>()

private fun formatMonnaie(locale: Locale): NumberFormat =
    formatsMonnaie.getOrPut(locale) {
        NumberFormat.getCurrencyInstance(locale).apply { currency = DEVISE }
    }

private fun formatDate(locale: Locale): DateTimeFormatter =
    formatsDate.getOrPut(locale) { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

/**
 * Formate un prix selon la locale courante : `45,00 €` (fr) ou `€45.00` (en).
 *
 * @param locale locale d'affichage, celle de l'utilisateur par défaut.
 */
fun formaterPrix(prix: Double, locale: Locale = Locale.getDefault()): String {
    val format = formatMonnaie(locale)
    return synchronized(format) { format.format(prix) }
}

/**
 * Formate une date courte selon la locale courante : `12 nov. 2026` (fr) ou
 * `12 Nov 2026` (en).
 */
fun formaterDate(date: LocalDate, locale: Locale = Locale.getDefault()): String =
    formatDate(locale).withLocale(locale).format(date.toJavaLocalDate())

/** Initiales d'un nom, utilisées dans les avatars : `Alice Martin` → `AM`. */
fun initiales(nom: String): String = nom
    .trim()
    .split(" ")
    .filter { it.isNotBlank() }
    .take(2)
    .joinToString("") { it.first().uppercase() }
    .ifBlank { "?" }
