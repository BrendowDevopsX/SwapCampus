package com.isep.composeapp.ui.screens.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.isep.composeapp.di.AppContainer
import com.isep.composeapp.ui.components.ErrorMessage
import com.isep.composeapp.ui.components.LoadingIndicator
import com.isep.shared.domain.entities.Disponible
import com.isep.shared.domain.entities.Reservee
import com.isep.shared.domain.entities.Vendue
import com.isep.shared.domain.entities.libelle

@Composable
fun ListingDetailScreen(
    listingId: Int,
    onEdit: (Int) -> Unit,
    viewModel: ListingDetailViewModel = viewModel {
        ListingDetailViewModel(
            listingRepository = AppContainer.listingRepository,
            authRepository = AppContainer.authRepository
        )
    }
) {
    LaunchedEffect(listingId) {
        viewModel.load(listingId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actionError by viewModel.actionError.collectAsStateWithLifecycle()
    val currentUser by AppContainer.authRepository.currentUser.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is DetailUiState.Loading -> LoadingIndicator()

        is DetailUiState.Error -> ErrorMessage(state.message)

        is DetailUiState.Ready -> {
            val annonce = state.annonce
            val isOwner = currentUser?.id == annonce.auteur.id

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(annonce.titre, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text("${annonce.prix} €", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(annonce.description)
                Spacer(Modifier.height(8.dp))
                Text("Category: ${annonce.categorie.libelle()}")
                Spacer(Modifier.height(8.dp))
                Text("Status: ${annonce.etat.libelle()}")
                Spacer(Modifier.height(8.dp))
                Text("Owner: ${annonce.auteur.nom}")

                if (isOwner) {
                    Spacer(Modifier.height(24.dp))
                    Text("Actions", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    when (annonce.etat) {
                        is Disponible -> {
                            Button(
                                onClick = { viewModel.reserve(annonce.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Reserve")
                            }
                        }
                        is Reservee -> {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { viewModel.markAsSold(annonce.id) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Mark as sold")
                                }
                                Spacer(Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = { viewModel.cancel(annonce.id) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Cancel")
                                }
                            }
                        }
                        is Vendue -> {
                            OutlinedButton(
                                onClick = { viewModel.cancel(annonce.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cancel")
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { onEdit(annonce.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Edit")
                    }
                }

                actionError?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}