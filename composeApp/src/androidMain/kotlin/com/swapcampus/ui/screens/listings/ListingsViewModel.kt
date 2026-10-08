package com.isep.composeapp.ui.screens.listings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isep.shared.domain.entities.Annonce
import com.isep.shared.domain.entities.Categorie
import com.isep.shared.repository.ListingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

    private val _keyword = MutableStateFlow("")
    val keyword: StateFlow<String> = _keyword.asStateFlow()

    private val _category = MutableStateFlow<Categorie?>(null)
    val category: StateFlow<Categorie?> = _category.asStateFlow()

    init {
        viewModelScope.launch {
            combine(_keyword, _category) { kw, cat -> kw to cat }
                .collect { (kw, cat) ->
                    loadListings(kw, cat)
                }
        }
    }

    fun onKeywordChange(value: String) {
        _keyword.value = value
    }

    fun onCategoryChange(value: Categorie?) {
        _category.value = value
    }

    private suspend fun loadListings(keyword: String, category: Categorie?) {
        _uiState.value = ListingsUiState.Loading
        try {
            repository.searchListings(
                motCle = keyword.ifBlank { null },
                categorie = category
            ).collect { listings ->
                _uiState.value = ListingsUiState.Success(listings)
            }
        } catch (e: Exception) {
            _uiState.value = ListingsUiState.Error(e.message ?: "Unknown error")
        }
    }
}