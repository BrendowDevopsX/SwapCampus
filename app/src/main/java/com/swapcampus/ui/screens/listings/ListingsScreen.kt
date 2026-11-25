package com.swapcampus.ui.screens.listings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swapcampus.R
import com.swapcampus.ui.components.CategoryFilterRow
import com.swapcampus.ui.components.EmptyState
import com.swapcampus.ui.components.ErrorMessage
import com.swapcampus.ui.components.FiltrePrix
import com.swapcampus.ui.components.ListingCard
import com.swapcampus.ui.components.LoadingIndicator
import com.swapcampus.ui.components.SearchField
import com.swapcampus.ui.components.SectionTitle
import com.swapcampus.ui.screens.listings.ListingsViewModel.ListingsUiState
import com.swapcampus.ui.text.messageErreur

/**
 * Écran d'accueil : catalogue des annonces disponibles, recherche par mot-clé,
 * filtre par catégorie et fourchette de prix (UC2 et UC3).
 */
@Composable
fun ListingsScreen(
    onListingClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ListingsViewModel = viewModel()
) {
    val keyword by viewModel.keyword.collectAsStateWithLifecycle()
    val categorie by viewModel.categorie.collectAsStateWithLifecycle()
    val prixMin by viewModel.prixMin.collectAsStateWithLifecycle()
    val prixMax by viewModel.prixMax.collectAsStateWithLifecycle()
    val filtreActif by viewModel.filtreActif.collectAsStateWithLifecycle()
    val connecte by viewModel.connecte.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        EnteteCatalogue()

        SearchField(
            value = keyword,
            onValueChange = viewModel::onKeywordChange,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(12.dp))

        CategoryFilterRow(
            selection = categorie,
            onSelection = viewModel::onCategorieChange,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(10.dp))

        FiltrePrix(
            prixMin = prixMin,
            prixMax = prixMax,
            onFiltre = viewModel::onPrixChange,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(16.dp))

        when (val etat = uiState) {
            ListingsUiState.Loading -> LoadingIndicator()

            is ListingsUiState.Error -> ErrorMessage(
                message = messageErreur(etat.cause, R.string.listings_erreur)
            )

            is ListingsUiState.Success -> {
                val annonces = etat.annonces
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("liste_annonces"),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "titre_section") {
                        SectionTitle(
                            titre = stringResource(R.string.listings_section),
                            secondaire = if (annonces.isEmpty()) {
                                null
                            } else {
                                pluralStringResource(
                                    R.plurals.listings_nb_annonces,
                                    annonces.size,
                                    annonces.size
                                )
                            }
                        )
                    }

                    if (annonces.isEmpty()) {
                        item(key = "vide") {
                            if (filtreActif) {
                                EmptyState(
                                    titre = stringResource(R.string.listings_vide_titre),
                                    message = stringResource(R.string.listings_vide_message),
                                    glyphe = "🔍",
                                    actionLabel = stringResource(R.string.listings_vide_action),
                                    onAction = viewModel::reinitialiserFiltres
                                )
                            } else {
                                EmptyState(
                                    titre = stringResource(R.string.listings_catalogue_vide_titre),
                                    message = stringResource(R.string.listings_catalogue_vide_message),
                                    glyphe = "🛍️"
                                )
                            }
                        }
                    }

                    items(items = annonces, key = { it.id }) { annonce ->
                        ListingCard(
                            annonce = annonce,
                            onClick = { onListingClick(annonce.id) },
                            favori = if (connecte) annonce.id in etat.favoris else null,
                            onFavoriChange = if (connecte) {
                                { viewModel.basculerFavori(annonce.id) }
                            } else {
                                null
                            }
                        )
                    }
                }
            }
        }
    }
}

/** Bandeau de titre du catalogue : nom de l'app et son sous-titre. */
@Composable
private fun EnteteCatalogue(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.listings_sous_titre),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
