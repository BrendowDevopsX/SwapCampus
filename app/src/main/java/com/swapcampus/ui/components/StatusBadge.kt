package com.swapcampus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.swapcampus.domain.Disponible
import com.swapcampus.domain.EtatAnnonce
import com.swapcampus.domain.Reservee
import com.swapcampus.domain.Vendue
import com.swapcampus.ui.text.libelleCourt
import com.swapcampus.ui.theme.DisponibleFond
import com.swapcampus.ui.theme.DisponibleTexte
import com.swapcampus.ui.theme.ReserveeFond
import com.swapcampus.ui.theme.ReserveeTexte
import com.swapcampus.ui.theme.VendueFond
import com.swapcampus.ui.theme.VendueTexte

private fun EtatAnnonce.couleurs(): Pair<Color, Color> = when (this) {
    is Disponible -> DisponibleFond to DisponibleTexte
    is Reservee -> ReserveeFond to ReserveeTexte
    is Vendue -> VendueFond to VendueTexte
}

/**
 * Pastille colorée qui rend l'état d'une annonce lisible immédiatement :
 * menthe pour disponible, ambre pour réservée, gris pour vendue.
 */
@Composable
fun StatusBadge(
    etat: EtatAnnonce,
    modifier: Modifier = Modifier
) {
    val (fond, texte) = etat.couleurs()
    Surface(
        color = fond,
        contentColor = texte,
        shape = RoundedCornerShape(percent = 50),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Box(
                Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(texte)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = etat.libelleCourt(),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
