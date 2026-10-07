package com.isep.composeapp.ui.screens.listings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isep.shared.domain.entities.Annonce
import com.isep.shared.repository.ListingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ListingsUiState {
    data object Loading : ListingsUiState
    data class Success(val listings: List<Annonce>) : ListingsUiState
    data class Error(val message: String) : ListingsUiState
}

class ListingsViewModel(
    private val repository: ListingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ListingsUiState>(ListingsUiState.Loading)
    val uiState: StateFlow<ListingsUiState> = _uiState.asStateFlow()

    init {
        loadListings()
    }

    fun loadListings() {
        viewModelScope.launch {
            _uiState.value = ListingsUiState.Loading
            try {
                repository.allListings().collect { listings ->
                    _uiState.value = ListingsUiState.Success(listings)
                }
            } catch (e: Exception) {
                _uiState.value = ListingsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}