package com.isep.composeapp.ui.screens.mylistings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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

@Composable
fun MyListingsScreen(
    onListingClick: (Int) -> Unit,
    onPostClick: () -> Unit,
    viewModel: MyListingsViewModel = viewModel {
        MyListingsViewModel(
            listingRepository = AppContainer.listingRepository,
            authRepository = AppContainer.authRepository
        )
    }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = onPostClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Post a new listing")
        }

        when (val state = uiState) {
            is MyListingsUiState.Loading -> LoadingIndicator()

            is MyListingsUiState.Error -> ErrorMessage(state.message)

            is MyListingsUiState.Success -> {
                if (state.listings.isEmpty()) {
                    EmptyState("You have no listings yet.")
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