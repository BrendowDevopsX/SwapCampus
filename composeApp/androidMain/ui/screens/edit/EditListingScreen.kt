package com.swapcampus.ui.screens.edit

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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swapcampus.domain.Categorie
import com.swapcampus.ui.components.EmptyState
import com.swapcampus.ui.components.ListingFormFields
import com.swapcampus.ui.components.LoadingIndicator

/**
 * Modification d'annonce (UC4), réservée au propriétaire. Les champs sont
 * pré-remplis à partir de l'annonce chargée.
 */
@Composable
fun EditListingScreen(
    listingId: Int,
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditListingViewModel = viewModel(key = "edit_$listingId")
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val erreur by viewModel.submitError.collectAsStateWithLifecycle()

    var titre by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var prix by remember { mutableStateOf("") }
    var categorie by remember { mutableStateOf<Categorie?>(null) }
    var photoUrl by remember { mutableStateOf("") }
    var preRempli by remember { mutableStateOf(false) }

    LaunchedEffect(listingId) { viewModel.load(listingId) }

    when (val etat = uiState) {
        EditUiState.Loading -> LoadingIndicator()

        is EditUiState.Error -> EmptyState(
            titre = "Modification impossible",
            message = etat.message,
            glyphe = "🔒",
            actionLabel = "Retour",
            onAction = onBack
        )

        is EditUiState.Ready -> {
            if (!preRempli) {
                LaunchedEffect(etat.annonce.id) {
                    titre = etat.annonce.titre
                    description = etat.annonce.description
                    prix = String.format(java.util.Locale.FRANCE, "%.2f", etat.annonce.prix)
                    categorie = etat.annonce.categorie
                    photoUrl = etat.annonce.photoUrl.orEmpty()
                    preRempli = true
                }
            }

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
                        text = "Modifier l'annonce",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
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
                        onPhotoUrlChange = { photoUrl = it }
                    )

                    if (erreur != null) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = erreur.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val prixSaisi = prix.replace(',', '.').toDoubleOrNull() ?: 0.0
                            val categorieChoisie = categorie
                            if (categorieChoisie != null) {
                                viewModel.update(
                                    titre = titre,
                                    description = description,
                                    prix = prixSaisi,
                                    categorie = categorieChoisie,
                                    photoUrl = photoUrl.ifBlank { null },
                                    onSuccess = onSuccess
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("bouton_enregistrer_annonce"),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Enregistrer", style = MaterialTheme.typography.labelLarge)
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}
