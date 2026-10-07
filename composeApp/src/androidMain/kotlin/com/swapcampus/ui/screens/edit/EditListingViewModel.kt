package com.isep.composeapp.ui.screens.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isep.shared.domain.entities.Annonce
import com.isep.shared.domain.entities.Categorie
import com.isep.shared.repository.AuthRepository
import com.isep.shared.repository.ListingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface EditUiState {
    data object Loading : EditUiState
    data class Ready(val annonce: Annonce) : EditUiState
    data class Error(val message: String) : EditUiState
}

class EditListingViewModel(
    private val listingRepository: ListingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditUiState>(EditUiState.Loading)
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    private val _submitError = MutableStateFlow<String?>(null)
    val submitError: StateFlow<String?> = _submitError.asStateFlow()

    fun load(listingId: Int) {
        viewModelScope.launch {
            _uiState.value = EditUiState.Loading

            val user = authRepository.currentUser.value
            if (user == null) {
                _uiState.value = EditUiState.Error("You must be logged in")
                return@launch
            }

            val annonce = listingRepository.allListings()
                .let { flow ->
                    var found: Annonce? = null
                    flow.collect { list ->
                        found = list.firstOrNull { it.id == listingId }
                        return@collect
                    }
                    found
                }

            if (annonce == null) {
                _uiState.value = EditUiState.Error("Listing not found")
                return@launch
            }

            if (annonce.auteur.id != user.id) {
                _uiState.value = EditUiState.Error("You are not the owner of this listing")
                return@launch
            }

            _uiState.value = EditUiState.Ready(annonce)
        }
    }

    fun update(
        titre: String,
        description: String,
        prix: Double,
        categorie: Categorie,
        photoUrl: String?,
        onSuccess: () -> Unit
    ) {
        val current = _uiState.value
        if (current !is EditUiState.Ready) return

        viewModelScope.launch {
            listingRepository.updateListing(
                id = current.annonce.id,
                titre = titre,
                description = description,
                prix = prix,
                categorie = categorie,
                photoUrl = photoUrl
            ).onSuccess {
                _submitError.value = null
                onSuccess()
            }.onFailure {
                _submitError.value = it.message ?: "Unknown error"
            }
        }
    }
}