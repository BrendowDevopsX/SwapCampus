package com.swapcampus.ui.screens.post

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swapcampus.R
import com.swapcampus.domain.Categorie
import com.swapcampus.ui.components.ListingFormFields
import com.swapcampus.ui.text.messageErreur

/**
 * Création d'annonce (UC1). Le formulaire est validé côté domaine ; ici on
 * empêche simplement l'envoi d'un brouillon manifestement incomplet.
 */
@Composable
fun PostListingScreen(
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostListingViewModel = viewModel()
) {
    val erreur by viewModel.erreur.collectAsStateWithLifecycle()
    val enregistrement by viewModel.enregistrement.collectAsStateWithLifecycle()

    var titre by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var prix by remember { mutableStateOf("") }
    var categorie by remember { mutableStateOf<Categorie?>(Categorie.LIVRES) }
    var photoUrl by remember { mutableStateOf("") }
    var tentativeEnvoi by remember { mutableStateOf(false) }

    val titreInvalide = tentativeEnvoi && titre.isBlank()
    val prixInvalide = tentativeEnvoi && (prix.replace(',', '.').toDoubleOrNull() ?: 0.0) <= 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 20.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Text(text = "←", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.size(4.dp))
            Text(
                text = stringResource(R.string.post_titre),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text(
                text = stringResource(R.string.post_sous_titre),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))

            ListingFormFields(
                titre = titre,
                onTitreChange = { titre = it },
                description = description,
                onDescriptionChange = { description = it },
                prix = prix,
                onPrixChange = { prix = it },
                categorie = categorie,
                onCategorieChange = { categorie = it },
                photoUrl = photoUrl,
                onPhotoUrlChange = { photoUrl = it },
                erreurTitre = if (titreInvalide) {
                    stringResource(R.string.erreur_titre_vide)
                } else {
                    null
                },
                erreurPrix = if (prixInvalide) {
                    stringResource(R.string.erreur_prix_invalide)
                } else {
                    null
                }
            )

            if (erreur != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = messageErreur(erreur, R.string.post_erreur),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    tentativeEnvoi = true
                    val prixSaisi = prix.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val categorieChoisie = categorie
                    if (titre.isNotBlank() && prixSaisi > 0.0 && categorieChoisie != null) {
                        viewModel.submit(
                            titre = titre,
                            description = description,
                            prix = prixSaisi,
                            categorie = categorieChoisie,
                            photoUrl = photoUrl.ifBlank { null },
                            onSuccess = onSuccess
                        )
                    }
                },
                enabled = !enregistrement,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("bouton_publier_annonce"),
                shape = MaterialTheme.shapes.medium
            ) {
                if (enregistrement) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = stringResource(R.string.post_action),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
