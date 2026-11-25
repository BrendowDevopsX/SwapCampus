package com.swapcampus.ui.screens.favoris

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swapcampus.di.AppContainer
import com.swapcampus.domain.Annonce
import com.swapcampus.domain.CompteUtilisateur
import com.swapcampus.domain.favorisParmi
import com.swapcampus.repository.AuthRepository
import com.swapcampus.repository.FavorisRepository
import com.swapcampus.repository.ListingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** États possibles de l'écran des favoris. */
sealed interface FavorisUiState {
    data object Chargement : FavorisUiState
    data object NonConnecte : FavorisUiState
    data class Pret(val annonces: List<Annonce>) : FavorisUiState
    data class Erreur(val cause: Throwable) : FavorisUiState
}

/**
 * ViewModel de l'onglet « Favoris » : la liste des annonces mises de côté par
 * l'utilisateur connecté, tous états confondus (un favori vendu reste visible
 * pour que l'utilisateur comprenne pourquoi il ne peut plus l'acheter).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FavorisViewModel(
    private val listingRepository: ListingRepository = AppContainer.listingRepository,
    private val authRepository: AuthRepository = AppContainer.authRepository,
    private val favorisRepository: FavorisRepository = AppContainer.favorisRepository
) : ViewModel() {

    val utilisateurCourant: StateFlow<CompteUtilisateur?> = authRepository.currentUser

    val uiState: StateFlow<FavorisUiState> = authRepository.currentUser
        .flatMapLatest { utilisateur ->
            if (utilisateur == null) {
                flowOf(FavorisUiState.NonConnecte)
            } else {
                combine(
                    favorisRepository.favorisDe(utilisateur.id),
                    listingRepository.allListings()
                ) { identifiants, annonces ->
                    FavorisUiState.Pret(annonces.favorisParmi(identifiants))
                }
            }
        }
        .catch { emit(FavorisUiState.Erreur(it)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavorisUiState.Chargement
        )

    /** Retire une annonce des favoris (cœur déjà plein dans la liste). */
    fun retirer(annonceId: Int) {
        val utilisateur = utilisateurCourant.value ?: return
        viewModelScope.launch { favorisRepository.basculer(utilisateur.id, annonceId) }
    }
}
