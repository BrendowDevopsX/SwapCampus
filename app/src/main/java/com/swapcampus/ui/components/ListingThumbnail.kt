package com.swapcampus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swapcampus.domain.Categorie
import com.swapcampus.ui.theme.style

/**
 * Vignette d'une annonce.
 *
 * Aucune bibliothèque d'images n'est nécessaire : un dégradé de la couleur de
 * la catégorie et son pictogramme servent d'identité visuelle par défaut, puis
 * la photo (choisie dans la galerie ou liée par URL) vient la remplacer dès
 * qu'elle est chargée. Une image indisponible ne casse donc jamais la mise en
 * page.
 */
@Composable
fun ListingThumbnail(
    categorie: Categorie,
    photoUrl: String?,
    modifier: Modifier = Modifier,
    corner: Dp = 18.dp,
    glypheTaille: TextUnit = 30.sp
) {
    val style = categorie.style()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(corner))
            .background(Brush.linearGradient(style.couleurs))
    ) {
        // Léger halo pour éviter un aplat trop plat.
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.22f), Color.Transparent)
                    )
                )
        )
        Text(
            text = style.glyphe,
            fontSize = glypheTaille,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(2.dp)
        )
        if (!photoUrl.isNullOrBlank()) {
            PhotoAnnonce(
                source = photoUrl,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(corner))
            )
        }
    }
}
