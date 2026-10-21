package com.swapcampus.domain
import com.swapcampus.domain.Annonce
import com.swapcampus.domain.Disponible
import com.swapcampus.domain.libelle


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
