package com.swapcampus.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swapcampus.di.AppContainer
import com.swapcampus.domain.Annonce
import com.swapcampus.domain.Disponible
import com.swapcampus.domain.EtatAnnonce
import com.swapcampus.domain.Reservee
import com.swapcampus.domain.Vendue
import com.swapcampus.repository.AuthRepository
import com.swapcampus.repository.ListingRepository
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

/**
 * Fiche détaillée d'une annonce : changement d'état (UC5), suppression (UC6)
 * et vérification de propriété (UC4).
 */
class ListingDetailViewModel(
    private val listingRepository: ListingRepository = AppContainer.listingRepository,
    private val authRepository: AuthRepository = AppContainer.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    /** Utilisateur connecté, `null` si la fiche est consultée en anonyme. */
    val utilisateurCourant: StateFlow<com.swapcampus.domain.CompteUtilisateur?> =
        authRepository.currentUser

    fun load(listingId: Int) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            val annonce = listingRepository.getListingById(listingId)
            _uiState.value = if (annonce == null) {
                DetailUiState.Error("Cette annonce n'existe plus.")
            } else {
                DetailUiState.Ready(annonce)
            }
        }
    }

    /** Réserve l'annonce au nom de l'acheteur saisi par le vendeur (UC5). */
    fun reserver(listingId: Int, acheteur: String) {
        val nom = acheteur.trim()
        if (nom.isEmpty()) {
            _actionError.value = "Indiquez le nom de l'acheteur."
            return
        }
        changerEtat(listingId, Reservee(nom))
    }

    fun marquerVendue(listingId: Int) {
        val aujourdHui = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
        changerEtat(listingId, Vendue(aujourdHui))
    }

    fun annulerReservation(listingId: Int) {
        changerEtat(listingId, Disponible)
    }

    fun supprimer(listingId: Int, onDeleted: () -> Unit) {
        viewModelScope.launch {
            listingRepository.removeListing(listingId)
                .onSuccess {
                    _actionError.value = null
                    onDeleted()
                }
                .onFailure { _actionError.value = it.message ?: "Suppression impossible." }
        }
    }

    fun clearActionError() {
        _actionError.value = null
    }

    private fun changerEtat(listingId: Int, nouvelEtat: EtatAnnonce) {
        viewModelScope.launch {
            listingRepository.changeStatus(listingId, nouvelEtat)
                .onSuccess {
                    _actionError.value = null
                    load(listingId)
                }
                .onFailure { _actionError.value = it.message ?: "Changement d'état impossible." }
        }
    }
}
