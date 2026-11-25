package com.swapcampus.ui.screens.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swapcampus.di.AppContainer
import com.swapcampus.domain.Annonce
import com.swapcampus.domain.Categorie
import com.swapcampus.domain.Vendue
import com.swapcampus.repository.AuthRepository
import com.swapcampus.repository.ListingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface EditUiState {
    data object Loading : EditUiState
    data class Ready(val annonce: Annonce) : EditUiState
    data class Error(val message: String) : EditUiState
}

/**
 * Modification d'annonce (UC4) : réservée au propriétaire, et interdite sur une
 * annonce déjà vendue (il faut la republier).
 */
class EditListingViewModel(
    private val listingRepository: ListingRepository = AppContainer.listingRepository,
    private val authRepository: AuthRepository = AppContainer.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditUiState>(EditUiState.Loading)
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    private val _submitError = MutableStateFlow<String?>(null)
    val submitError: StateFlow<String?> = _submitError.asStateFlow()

    fun load(listingId: Int) {
        viewModelScope.launch {
            _uiState.value = EditUiState.Loading

            val utilisateur = authRepository.currentUser.value
            if (utilisateur == null) {
                _uiState.value = EditUiState.Error("Connectez-vous pour modifier une annonce.")
                return@launch
            }

            val annonce = listingRepository.getListingById(listingId)
            if (annonce == null) {
                _uiState.value = EditUiState.Error("Cette annonce n'existe plus.")
                return@launch
            }

            if (annonce.auteur.id != utilisateur.id) {
                _uiState.value = EditUiState.Error("Vous n'êtes pas le propriétaire de cette annonce.")
                return@launch
            }

            if (annonce.etat is Vendue) {
                _uiState.value = EditUiState.Error(
                    "Une annonce vendue ne peut plus être modifiée : republiez-la pour la remettre en vente."
                )
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
        val courant = _uiState.value
        if (courant !is EditUiState.Ready) return

        viewModelScope.launch {
            listingRepository.updateListing(
                id = courant.annonce.id,
                titre = titre,
                description = description,
                prix = prix,
                categorie = categorie,
                photoUrl = photoUrl
            ).onSuccess {
                _submitError.value = null
                onSuccess()
            }.onFailure {
                _submitError.value = it.message ?: "Enregistrement impossible."
            }
        }
    }

    fun clearError() {
        _submitError.value = null
    }
}
