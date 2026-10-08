package com.isep.composeapp.ui.screens.listings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.isep.composeapp.di.AppContainer
import com.isep.composeapp.ui.components.EmptyState
import com.isep.composeapp.ui.components.ErrorMessage
import com.isep.composeapp.ui.components.ListingCard
import com.isep.composeapp.ui.components.LoadingIndicator
import com.isep.shared.domain.entities.Categorie
import com.isep.shared.domain.entities.libelle

@Composable
fun ListingsScreen(
    onListingClick: (Int) -> Unit,
    viewModel: ListingsViewModel = viewModel {
        ListingsViewModel(AppContainer.listingRepository)
    }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyword by viewModel.keyword.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {

        OutlinedTextField(
            value = keyword,
            onValueChange = viewModel::onKeywordChange,
            label = { Text("Search...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = category == null,
                    onClick = { viewModel.onCategoryChange(null) },
                    label = { Text("All") }
                )
            }
            items(Categorie.entries.toList()) { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = { viewModel.onCategoryChange(cat) },
                    label = { Text(cat.libelle()) }
                )
            }
        }

        when (val state = uiState) {
            is ListingsUiState.Loading -> LoadingIndicator()

            is ListingsUiState.Error -> ErrorMessage(state.message)

            is ListingsUiState.Success -> {
                if (state.listings.isEmpty()) {
                    EmptyState("No listings match your search.")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.listings, key = { it.id }) { annonce ->
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
}