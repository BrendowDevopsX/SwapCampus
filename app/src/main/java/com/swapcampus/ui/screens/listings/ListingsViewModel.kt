package com.swapcampus.ui.screens.listings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swapcampus.di.AppContainer
import com.swapcampus.domain.Annonce
import com.swapcampus.domain.Categorie
import com.swapcampus.repository.AuthRepository
import com.swapcampus.repository.FavorisRepository
import com.swapcampus.repository.ListingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel du catalogue (UC2 + UC3).
 *
 * Trois filtres sont appliqués en continu : le mot-clé, la catégorie et la
 * fourchette de prix. Les identifiants favoris de l'utilisateur connecté sont
 * exposés avec la liste pour que les cartes puissent afficher leur cœur.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ListingsViewModel(
    private val listingRepository: ListingRepository = AppContainer.listingRepository,
    private val authRepository: AuthRepository = AppContainer.authRepository,
    private val favorisRepository: FavorisRepository = AppContainer.favorisRepository
) : ViewModel() {

    sealed interface ListingsUiState {
        data object Loading : ListingsUiState

        data class Success(
            val annonces: List<Annonce>,
            val favoris: Set<Int> = emptySet()
        ) : ListingsUiState

        data class Error(val cause: Throwable) : ListingsUiState
    }

    private data class Filtres(
        val motCle: String?,
        val categorie: Categorie?,
        val prixMin: Double?,
        val prixMax: Double?
    )

    private val _keyword = MutableStateFlow("")
    val keyword: StateFlow<String> = _keyword.asStateFlow()

    private val _categorie = MutableStateFlow<Categorie?>(null)
    val categorie: StateFlow<Categorie?> = _categorie.asStateFlow()

    private val _prixMin = MutableStateFlow<Double?>(null)
    val prixMin: StateFlow<Double?> = _prixMin.asStateFlow()

    private val _prixMax = MutableStateFlow<Double?>(null)
    val prixMax: StateFlow<Double?> = _prixMax.asStateFlow()

    /** `true` dès qu'un filtre est actif, pour afficher « Réinitialiser ». */
    val filtreActif: StateFlow<Boolean> = combine(
        _keyword,
        _categorie,
        _prixMin,
        _prixMax
    ) { motCle, categorie, min, max ->
        motCle.isNotBlank() || categorie != null || min != null || max != null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    val connecte: StateFlow<Boolean> = authRepository.currentUser
        .map { it != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    private val filtres = combine(_keyword, _categorie, _prixMin, _prixMax) { motCle, categorie, min, max ->
        Filtres(
            motCle = motCle.trim().ifEmpty { null },
            categorie = categorie,
            prixMin = min,
            prixMax = max
        )
    }

    private val favoris: kotlinx.coroutines.flow.Flow<Set<Int>> = authRepository.currentUser
        .flatMapLatest { utilisateur ->
            if (utilisateur == null) flowOf(emptySet())
            else favorisRepository.favorisDe(utilisateur.id)
        }

    val uiState: StateFlow<ListingsUiState> = filtres
        .flatMapLatest { filtre ->
            listingRepository.searchListings(
                motCle = filtre.motCle,
                categorie = filtre.categorie,
                prixMin = filtre.prixMin,
                prixMax = filtre.prixMax
            )
        }
        .combine(favoris) { annonces, identifiants ->
            ListingsUiState.Success(annonces, identifiants) as ListingsUiState
        }
        .catch { erreur -> emit(ListingsUiState.Error(erreur)) }
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

    /** Applique la fourchette de prix ; `null` désactive la borne correspondante. */
    fun onPrixChange(min: Double?, max: Double?) {
        _prixMin.value = min
        _prixMax.value = max
    }

    /** Ajoute ou retire une annonce des favoris de l'utilisateur connecté. */
    fun basculerFavori(annonceId: Int) {
        val utilisateur = authRepository.currentUser.value ?: return
        viewModelScope.launch { favorisRepository.basculer(utilisateur.id, annonceId) }
    }

    /** Remet à zéro la recherche et tous les filtres (bouton « Réinitialiser »). */
    fun reinitialiserFiltres() {
        _keyword.value = ""
        _categorie.value = null
        _prixMin.value = null
        _prixMax.value = null
    }
}
