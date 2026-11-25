package com.swapcampus.ui.screens.favoris

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swapcampus.R
import com.swapcampus.ui.components.EmptyState
import com.swapcampus.ui.components.ErrorMessage
import com.swapcampus.ui.components.ListingCard
import com.swapcampus.ui.components.LoadingIndicator
import com.swapcampus.ui.text.messageErreur

/**
 * Onglet « Favoris » : les annonces gardées de côté par l'utilisateur.
 *
 * La liste montre tous les états (une annonce vendue reste consultable), et le
 * cœur plein de chaque carte permet de retirer l'annonce des favoris.
 */
@Composable
fun FavorisScreen(
    onListingClick: (Int) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavorisViewModel = viewModel()
) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()

    when (val courant = etat) {
        is FavorisUiState.Chargement -> LoadingIndicator(modifier = modifier)

        is FavorisUiState.NonConnecte -> Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            EmptyState(
                titre = stringResource(R.string.favoris_titre),
                message = stringResource(R.string.favoris_connexion_message),
                glyphe = "❤️",
                actionLabel = stringResource(R.string.profil_se_connecter),
                onAction = onLoginClick
            )
        }

        is FavorisUiState.Erreur -> ErrorMessage(
            message = messageErreur(courant.cause, R.string.favoris_erreur),
            modifier = modifier.padding(24.dp)
        )

        is FavorisUiState.Pret -> Column(modifier = modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp)) {
                Text(
                    text = stringResource(R.string.favoris_titre),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.favoris_sous_titre),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (courant.annonces.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    EmptyState(
                        titre = stringResource(R.string.favoris_vide_titre),
                        message = stringResource(R.string.favoris_vide_message),
                        glyphe = "🤍"
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = courant.annonces, key = { it.id }) { annonce ->
                        ListingCard(
                            annonce = annonce,
                            onClick = { onListingClick(annonce.id) },
                            favori = true,
                            onFavoriChange = { viewModel.retirer(annonce.id) }
                        )
                    }
                }
            }
        }
    }
}
