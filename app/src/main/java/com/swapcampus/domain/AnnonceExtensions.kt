package com.isep.shared.domain

import com.isep.shared.domain.entities.Annonce
import com.isep.shared.domain.entities.Disponible
import com.isep.shared.domain.entities.libelle


fun List<Annonce>.disponibles(): List<Annonce> =
    filter { it.etat is Disponible }

fun Annonce.urlAfficheeOuDefaut(): String =
    photoUrl ?: "https://swapcampus.isep.fr/img/placeholder.png"

fun Annonce.decrirePhoto(): String =
    photoUrl?.let { url -> "Photo disponible : $url" } ?: "Aucune photo"

fun Annonce.resume(): String = StringBuilder().apply {
    append(titre)
    append(" — ")
    append(prix)
    append(" € (")
    append(etat.libelle())
    append(")")
}.toString()
