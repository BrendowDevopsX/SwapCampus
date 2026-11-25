package com.swapcampus.ui.theme

import androidx.compose.ui.graphics.Color
import com.swapcampus.domain.Categorie

/**
 * Identité visuelle d'une catégorie : un dégradé et un pictogramme.
 *
 * Chaque annonce reçoit ainsi une vignette colorée stable, même sans photo :
 * la liste reste lisible et reconnaissable d'un coup d'œil.
 */
data class StyleCategorie(
    val glyphe: String,
    val couleurs: List<Color>
)

fun Categorie.style(): StyleCategorie = when (this) {
    Categorie.LIVRES -> StyleCategorie("📚", listOf(Color(0xFF4F46E5), Color(0xFF8B85FF)))
    Categorie.COURS -> StyleCategorie("🎓", listOf(Color(0xFF0F766E), Color(0xFF2DD4BF)))
    Categorie.ELECTRONIQUE -> StyleCategorie("💻", listOf(Color(0xFF1D4ED8), Color(0xFF60A5FA)))
    Categorie.SPORT_LOISIRS -> StyleCategorie("🏀", listOf(Color(0xFFB45309), Color(0xFFF59E0B)))
    Categorie.MOBILIER -> StyleCategorie("🪑", listOf(Color(0xFF6D28D9), Color(0xFFC084FC)))
    Categorie.VETEMENTS -> StyleCategorie("👕", listOf(Color(0xFFBE123C), Color(0xFFFB7185)))
    Categorie.AUTRE -> StyleCategorie("✨", listOf(Color(0xFF475569), Color(0xFF94A3B8)))
}
