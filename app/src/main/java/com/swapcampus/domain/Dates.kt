package com.swapcampus.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Date du jour, en [LocalDate] (portable Android / iOS).
 *
 * Utilisée pour horodater les annonces et les ventes.
 */
fun aujourdHui(): LocalDate =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
