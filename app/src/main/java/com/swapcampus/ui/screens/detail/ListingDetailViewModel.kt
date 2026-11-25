package com.swapcampus.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swapcampus.di.AppContainer
import com.swapcampus.domain.Annonce
import com.swapcampus.domain.CompteUtilisateur
import com.swapcampus.domain.Disponible
import com.swapcampus.domain.EtatAnnonce
import com.swapcampus.domain.ReglesAnnonce
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

/** États de la fiche détaillée. */
sealed interface DetailUiState {
    data object Loading : DetailUiState

    data class Ready(val annonce: Annonce) : DetailUiState

    /** [cause] est conservée brute : l'écran la traduit via `messageErreur`. */
    data class Error(val cause: Throwable? = null) : DetailUiState
}

/**
 * Fiche détaillée d'une annonce : changement d'état (UC5), suppression (UC6),
 * republication d'une annonce vendue (UC4) et vérification de propriété.
 */
class ListingDetailViewModel(
    private val listingRepository: ListingRepository = AppContainer.listingRepository,
    private val authRepository: AuthRepository = AppContainer.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _actionError = MutableStateFlow<Throwable?>(null)
    val actionError: StateFlow<Throwable?> = _actionError.asStateFlow()

    /** Utilisateur connecté, `null` si la fiche est consultée en anonyme. */
    val utilisateurCourant: StateFlow<CompteUtilisateur?> = authRepository.currentUser

    fun load(listingId: Int) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            val annonce = listingRepository.getListingById(listingId)
            _uiState.value = if (annonce == null) {
                DetailUiState.Error()
            } else {
                DetailUiState.Ready(annonce)
            }
        }
    }

    /** Réserve l'annonce au nom de l'acheteur saisi par le vendeur (UC5). */
    fun reserver(listingId: Int, acheteur: String) {
        val erreur = ReglesAnnonce.validerReservation(acheteur)
        if (erreur != null) {
            _actionError.value = erreur.exception()
            return
        }
        changerEtat(listingId, Reservee(acheteur.trim()))
    }

    fun marquerVendue(listingId: Int) {
        val aujourdHui = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
        changerEtat(listingId, Vendue(aujourdHui))
    }

    /** Remet une annonce réservée ou vendue en vente. */
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
                .onFailure { _actionError.value = it }
        }
    }

    /** Suppression par un administrateur : un motif est obligatoire (UC6, §3.2). */
    fun supprimerEnAdministrateur(listingId: Int, motif: String, onDeleted: () -> Unit) {
        val moderateur = utilisateurCourant.value ?: return
        viewModelScope.launch {
            listingRepository.removeListingAsAdmin(listingId, moderateur, motif)
                .onSuccess {
                    _actionError.value = null
                    onDeleted()
                }
                .onFailure { _actionError.value = it }
        }
    }

    /**
     * Republie une annonce vendue : une copie est créée à la place de l'annonce
     * d'origine, conformément à UC4 (« une annonce vendue se republie, elle ne
     * se modifie pas »).
     */
    fun republier(listingId: Int, onPublie: (Int) -> Unit) {
        val auteur = utilisateurCourant.value ?: return
        viewModelScope.launch {
            listingRepository.republishListing(listingId, auteur)
                .onSuccess { copie ->
                    _actionError.value = null
                    onPublie(copie.id)
                }
                .onFailure { _actionError.value = it }
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
                .onFailure { _actionError.value = it }
        }
    }
}
