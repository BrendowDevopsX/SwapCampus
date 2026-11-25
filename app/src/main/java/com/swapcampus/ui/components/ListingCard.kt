package com.swapcampus.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swapcampus.R
import com.swapcampus.domain.Annonce
import com.swapcampus.ui.text.libelle

/**
 * Carte d'annonce du catalogue (UC2) : vignette, titre, prix, catégorie, état
 * et, si l'utilisateur est connecté, un cœur pour l'ajouter aux favoris.
 *
 * La vignette occupe 92 dp à gauche, les informations s'empilent à droite.
 *
 * @param favori `true`/`false` pour afficher le bouton favori, `null` pour le masquer.
 * @param onFavoriChange appelé quand l'utilisateur touche le cœur.
 */
@Composable
fun ListingCard(
    annonce: Annonce,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    favori: Boolean? = null,
    onFavoriChange: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("annonce_${annonce.id}")
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                ListingThumbnail(
                    categorie = annonce.categorie,
                    photoUrl = annonce.photoUrl,
                    modifier = Modifier.size(92.dp),
                    corner = 16.dp,
                    glypheTaille = 34.sp
                )

                if (favori != null && onFavoriChange != null) {
                    BoutonFavori(
                        annonceId = annonce.id,
                        favori = favori,
                        onFavoriChange = onFavoriChange,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = annonce.categorie.libelle(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.weight(1f))
                    StatusBadge(annonce.etat)
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = annonce.titre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = formaterPrix(annonce.prix),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = stringResource(
                        R.string.carte_vendeur_date,
                        annonce.auteur.nom,
                        formaterDate(annonce.dateCreation)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Petit cœur posé sur la vignette : plein quand l'annonce est en favori. */
@Composable
private fun BoutonFavori(
    annonceId: Int,
    favori: Boolean,
    onFavoriChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    val description = stringResource(
        if (favori) R.string.carte_favori_retirer else R.string.carte_favori_ajouter
    )

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        shape = MaterialTheme.shapes.extraSmall,
        modifier = modifier
            .size(28.dp)
            .testTag("favori_$annonceId")
            .semantics { contentDescription = description }
            .clickable(onClick = onFavoriChange)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = if (favori) "♥" else "♡",
                style = MaterialTheme.typography.titleSmall,
                color = if (favori) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
