package com.swapcampus.ui.screens.listings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swapcampus.di.AppContainer
import com.swapcampus.domain.Annonce
import com.swapcampus.domain.Categorie
import com.swapcampus.repository.ListingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * État du catalogue (UC2 + UC3) : recherche par mot-clé et filtre par
 * catégorie, appliqués en continu sur le flux du dépôt.
 */
class ListingsViewModel(
    private val repository: ListingRepository = AppContainer.listingRepository
) : ViewModel() {

    sealed interface ListingsUiState {
        data object Loading : ListingsUiState
        data class Success(val annonces: List<Annonce>) : ListingsUiState
        data class Error(val message: String) : ListingsUiState
    }

    private val _keyword = MutableStateFlow("")
    val keyword: StateFlow<String> = _keyword.asStateFlow()

    private val _categorie = MutableStateFlow<Categorie?>(null)
    val categorie: StateFlow<Categorie?> = _categorie.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ListingsUiState> = combine(_keyword, _categorie) { motCle, categorie ->
        motCle.trim().ifEmpty { null } to categorie
    }
        .flatMapLatest { (motCle, categorie) ->
            repository.searchListings(motCle, categorie).map { annonces ->
                ListingsUiState.Success(annonces) as ListingsUiState
            }
        }
        .catch { erreur ->
            emit(ListingsUiState.Error(erreur.message ?: "Impossible de charger les annonces"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ListingsUiState.Loading
        )

    fun onKeywordChange(nouveau: String) {
        _keyword.value = nouveau
    }

    fun onCategorieChange(categorie: Categorie?) {
        _categorie.value = categorie
    }

    /** Remet à zéro la recherche et le filtre (bouton « Réinitialiser »). */
    fun reinitialiserFiltres() {
        _keyword.value = ""
        _categorie.value = null
    }
}
