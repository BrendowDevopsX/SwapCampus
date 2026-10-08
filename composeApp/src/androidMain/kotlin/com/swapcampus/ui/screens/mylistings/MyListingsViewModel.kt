package com.isep.composeapp.ui.screens.mylistings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isep.shared.domain.entities.Annonce
import com.isep.shared.repository.AuthRepository
import com.isep.shared.repository.ListingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

sealed interface MyListingsUiState {
    data object Loading : MyListingsUiState
    data class Success(val listings: List<Annonce>) : MyListingsUiState
    data class Error(val message: String) : MyListingsUiState
}

class MyListingsViewModel(
    private val listingRepository: ListingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyListingsUiState>(MyListingsUiState.Loading)
    val uiState: StateFlow<MyListingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                listingRepository.allListings(),
                authRepository.currentUser
            ) { listings, user ->
                if (user == null) emptyList()
                else listings.filter { it.auteur.id == user.id }
            }.collect { mine ->
                _uiState.value = MyListingsUiState.Success(mine)
            }
        }
    }
}