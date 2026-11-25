package com.swapcampus.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.swapcampus.R
import com.swapcampus.domain.Categorie
import com.swapcampus.ui.text.libelle
import com.swapcampus.ui.theme.style

/**
 * Champs partagés par les formulaires de création (UC1) et de modification
 * (UC4) d'annonce, afin que les deux écrans restent strictement identiques.
 *
 * La validation métier (titre non vide, prix > 0) est faite dans le domaine ;
 * ici on ne fait que contraindre la saisie au clavier.
 */
@Composable
fun ListingFormFields(
    titre: String,
    onTitreChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    prix: String,
    onPrixChange: (String) -> Unit,
    categorie: Categorie?,
    onCategorieChange: (Categorie) -> Unit,
    photoUrl: String,
    onPhotoUrlChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    erreurTitre: String? = null,
    erreurPrix: String? = null
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = titre,
            onValueChange = onTitreChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("champ_titre"),
            label = { Text(stringResource(R.string.champ_titre)) },
            placeholder = { Text(stringResource(R.string.champ_titre_placeholder)) },
            singleLine = true,
            isError = erreurTitre != null,
            supportingText = erreurTitre?.let { { Text(it) } },
            shape = MaterialTheme.shapes.medium
        )

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("champ_description"),
            label = { Text(stringResource(R.string.champ_description)) },
            placeholder = { Text(stringResource(R.string.champ_description_placeholder)) },
            minLines = 3,
            maxLines = 6,
            shape = MaterialTheme.shapes.medium
        )

        OutlinedTextField(
            value = prix,
            onValueChange = { saisie ->
                if (saisie.isEmpty() || saisie.matches(REGEX_PRIX)) onPrixChange(saisie)
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("champ_prix"),
            label = { Text(stringResource(R.string.champ_prix)) },
            placeholder = { Text("0,00") },
            singleLine = true,
            isError = erreurPrix != null,
            supportingText = erreurPrix?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            shape = MaterialTheme.shapes.medium
        )

        Text(
            text = stringResource(R.string.champ_categorie),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Categorie.entries.toList().chunked(2).forEach { paire ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                paire.forEach { option ->
                    PuceFiltre(
                        libelle = option.libelle(),
                        selectionne = categorie == option,
                        onClick = { onCategorieChange(option) },
                        glyphe = option.style().glyphe,
                        testTag = "cat_${option.name}",
                        modifier = Modifier.weight(1f)
                    )
                }
                if (paire.size == 1) {
                    // Garde l'alignement à gauche quand la dernière ligne n'a qu'une puce.
                    Row(modifier = Modifier.weight(1f)) {}
                }
            }
        }

        ChampPhoto(
            photoUrl = photoUrl,
            onPhotoUrlChange = onPhotoUrlChange,
            categorie = categorie,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
        )
    }
}

private val REGEX_PRIX = Regex("^\\d{0,6}([.,]\\d{0,2})?$")
