package com.swapcampus.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swapcampus.R
import com.swapcampus.domain.Categorie
import com.swapcampus.ui.text.libelle
import com.swapcampus.ui.theme.style

/**
 * Puce de filtre réutilisable (catégories du catalogue, filtres d'état).
 *
 * @param glyphe pictogramme optionnel affiché à gauche du libellé.
 * @param testTag étiquette de test, utile pour les tests d'interface.
 */
@Composable
fun PuceFiltre(
    libelle: String,
    selectionne: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glyphe: String? = null,
    testTag: String? = null
) {
    Surface(
        shape = RoundedCornerShape(percent = 50),
        color = if (selectionne) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface
        },
        contentColor = if (selectionne) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        border = if (selectionne) {
            null
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
                .clickable(onClick = onClick)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            if (glyphe != null) {
                Text(text = glyphe, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = libelle,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1
            )
        }
    }
}

/**
 * Barre horizontale de filtres par catégorie (UC3) : « Tout » puis les
 * sept catégories du catalogue.
 */
@Composable
fun CategoryFilterRow(
    selection: Categorie?,
    onSelection: (Categorie?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        item(key = "toutes") {
            PuceFiltre(
                libelle = stringResource(R.string.filtre_tout),
                selectionne = selection == null,
                onClick = { onSelection(null) },
                glyphe = "🛍️",
                testTag = "filtre_TOUT"
            )
        }
        items(items = Categorie.entries.toList(), key = { it.name }) { categorie ->
            PuceFiltre(
                libelle = categorie.libelle(),
                selectionne = selection == categorie,
                onClick = { onSelection(if (selection == categorie) null else categorie) },
                glyphe = categorie.style().glyphe,
                testTag = "filtre_${categorie.name}"
            )
        }
    }
}
