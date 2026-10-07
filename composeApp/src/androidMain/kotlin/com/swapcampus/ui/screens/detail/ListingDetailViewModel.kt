package com.isep.composeapp.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isep.shared.domain.entities.Annonce
import com.isep.shared.domain.entities.Disponible
import com.isep.shared.domain.entities.EtatAnnonce
import com.isep.shared.domain.entities.Reservee
import com.isep.shared.domain.entities.Vendue
import com.isep.shared.repository.AuthRepository
import com.isep.shared.repository.ListingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
        changeStatus(listingId, Reservee(user.email))
    }

    fun markAsSold(listingId: Int) {
        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
        changeStatus(listingId, Vendue(today))
    }

    fun cancel(listingId: Int) {
        changeStatus(listingId, Disponible)
    }

    fun remove(listingId: Int, onDeleted: () -> Unit) {
        viewModelScope.launch {
            listingRepository.removeListing(listingId)
                .onSuccess {
                    _actionError.value = null
                    onDeleted()
                }
                .onFailure {
                    _actionError.value = it.message ?: "Unknown error"
                }
        }
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