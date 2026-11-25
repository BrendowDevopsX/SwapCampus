package com.swapcampus.ui.screens.mylistings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swapcampus.R
import com.swapcampus.domain.StatistiquesVendeur
import com.swapcampus.ui.components.EmptyState
import com.swapcampus.ui.components.ListingCard
import com.swapcampus.ui.components.LoadingIndicator
import com.swapcampus.ui.components.PuceFiltre
import com.swapcampus.ui.components.StatCard
import com.swapcampus.ui.text.messageErreur
import com.swapcampus.ui.theme.Amber
import com.swapcampus.ui.theme.Indigo
import com.swapcampus.ui.theme.InkSoft
import com.swapcampus.ui.theme.Teal

/**
 * Espace vendeur (UC7) : compteurs de son activité, filtre par état et liste
 * de ses propres annonces.
 */
@Composable
fun MyListingsScreen(
    onListingClick: (Int) -> Unit,
    onPostClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyListingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filtre by viewModel.filtre.collectAsStateWithLifecycle()
    val utilisateur by viewModel.utilisateurCourant.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        val prenom = utilisateur?.nom?.substringBefore(' ')
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.mes_titre),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (prenom.isNullOrBlank()) {
                        stringResource(R.string.mes_bonjour_sans_nom)
                    } else {
                        stringResource(R.string.mes_bonjour, prenom)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onPostClick,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .size(44.dp)
                    .testTag("bouton_publier")
            ) {
                Text(
                    text = "+",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        when (val etat = uiState) {
            MyListingsViewModel.MesAnnoncesUiState.Chargement -> LoadingIndicator()

            is MyListingsViewModel.MesAnnoncesUiState.Erreur -> EmptyState(
                titre = stringResource(R.string.mes_erreur_titre),
                message = messageErreur(etat.cause, R.string.mes_erreur),
                glyphe = "⚠️"
            )

            is MyListingsViewModel.MesAnnoncesUiState.Pret -> {
                TableauBord(statistiques = etat.statistiques)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FiltreMesAnnonces.entries.forEach { option ->
                        PuceFiltre(
                            libelle = stringResource(option.libelle),
                            selectionne = filtre == option,
                            onClick = { viewModel.onFiltreChange(option) },
                            testTag = "filtre_mes_${option.name}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (etat.annonces.isEmpty()) {
                        item(key = "vide") {
                            EmptyState(
                                titre = stringResource(
                                    when (filtre) {
                                        FiltreMesAnnonces.TOUTES -> R.string.mes_vide_toutes
                                        FiltreMesAnnonces.EN_LIGNE -> R.string.mes_vide_en_ligne
                                        FiltreMesAnnonces.VENDUES -> R.string.mes_vide_vendues
                                    }
                                ),
                                message = stringResource(
                                    when (filtre) {
                                        FiltreMesAnnonces.TOUTES -> R.string.mes_vide_toutes_message
                                        FiltreMesAnnonces.EN_LIGNE -> R.string.mes_vide_en_ligne_message
                                        FiltreMesAnnonces.VENDUES -> R.string.mes_vide_vendues_message
                                    }
                                ),
                                glyphe = "📦",
                                actionLabel = stringResource(R.string.post_titre),
                                onAction = onPostClick
                            )
                        }
                    }

                    items(items = etat.annonces, key = { it.id }) { annonce ->
                        ListingCard(
                            annonce = annonce,
                            onClick = { onListingClick(annonce.id) }
                        )
                    }
                }
            }
        }
    }
}

/** Les quatre compteurs du tableau de bord vendeur (UC7). */
@Composable
private fun TableauBord(statistiques: StatistiquesVendeur, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                valeur = statistiques.publiees,
                libelle = stringResource(R.string.mes_stat_publiees),
                accent = Indigo,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                valeur = statistiques.enLigne,
                libelle = stringResource(R.string.mes_stat_en_ligne),
                accent = Teal,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                valeur = statistiques.reservees,
                libelle = stringResource(R.string.mes_stat_reservees),
                accent = Amber,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                valeur = statistiques.vendues,
                libelle = stringResource(R.string.mes_stat_vendues),
                accent = InkSoft,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}
