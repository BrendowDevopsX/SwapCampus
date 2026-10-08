package com.isep.composeapp.ui.screens.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.isep.composeapp.di.AppContainer
import com.isep.composeapp.ui.components.ErrorMessage
import com.isep.composeapp.ui.components.LoadingIndicator
import com.isep.shared.domain.entities.Categorie
import com.isep.shared.domain.entities.libelle

@Composable
fun EditListingScreen(
    listingId: Int,
    onSuccess: () -> Unit,
    viewModel: EditListingViewModel = viewModel {
        EditListingViewModel(
            listingRepository = AppContainer.listingRepository,
            authRepository = AppContainer.authRepository
        )
    }
) {
    LaunchedEffect(listingId) {
        viewModel.load(listingId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val submitError by viewModel.submitError.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is EditUiState.Loading -> LoadingIndicator()

        is EditUiState.Error -> ErrorMessage(state.message)

        is EditUiState.Ready -> {
            val annonce = state.annonce

            var titre by remember(annonce.id) { mutableStateOf(annonce.titre) }
            var description by remember(annonce.id) { mutableStateOf(annonce.description) }
            var prix by remember(annonce.id) { mutableStateOf(annonce.prix.toString()) }
            var photoUrl by remember(annonce.id) { mutableStateOf(annonce.photoUrl ?: "") }
            var categorie by remember(annonce.id) { mutableStateOf(annonce.categorie) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = titre,
                    onValueChange = { titre = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = prix,
                    onValueChange = { prix = it },
                    label = { Text("Price (€)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = photoUrl,
                    onValueChange = { photoUrl = it },
                    label = { Text("Photo URL (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Text("Category", style = MaterialTheme.typography.titleSmall)
                Categorie.entries.forEach { cat ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        RadioButton(
                            selected = categorie == cat,
                            onClick = { categorie = cat }
                        )
                        Text(cat.libelle())
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.update(
                            titre = titre,
                            description = description,
                            prix = prix.toDoubleOrNull() ?: 0.0,
                            categorie = categorie,
                            photoUrl = photoUrl.ifBlank { null },
                            onSuccess = onSuccess
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }

                submitError?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}