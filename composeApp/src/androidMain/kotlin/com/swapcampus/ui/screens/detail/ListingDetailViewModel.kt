package com.isep.composeapp.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isep.shared.domain.entities.Annonce
import com.isep.shared.domain.entities.EtatAnnonce
import com.isep.shared.repository.AuthRepository
import com.isep.shared.repository.ListingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Ready(val annonce: Annonce) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

class ListingDetailViewModel(
    private val listingRepository: ListingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    fun load(listingId: Int) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            val annonce = listingRepository.getListingById(listingId)
            _uiState.value = if (annonce == null) {
                DetailUiState.Error("Listing not found")
            } else {
                DetailUiState.Ready(annonce)
            }
        }
    }

    fun reserve(listingId: Int) {
        val user = authRepository.currentUser.value
        if (user == null) {
            _actionError.value = "You must be logged in"
            return
        }
        changeStatus(listingId, com.isep.shared.domain.entities.Reservee(user.email))
    }

    fun markAsSold(listingId: Int) {
        changeStatus(listingId, com.isep.shared.domain.entities.Vendue(kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date))
    }

    fun cancel(listingId: Int) {
        changeStatus(listingId, com.isep.shared.domain.entities.Disponible)
    }

    private fun changeStatus(listingId: Int, newStatus: EtatAnnonce) {
        viewModelScope.launch {
            listingRepository.changeStatus(listingId, newStatus)
                .onSuccess {
                    _actionError.value = null
                    load(listingId)
                }
                .onFailure {
                    _actionError.value = it.message ?: "Unknown error"
                }
        }
    }
}