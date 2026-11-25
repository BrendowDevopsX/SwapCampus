package com.swapcampus.ui.screens.mylistings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swapcampus.di.AppContainer
import com.swapcampus.domain.Annonce
import com.swapcampus.domain.CompteUtilisateur
import com.swapcampus.domain.Disponible
import com.swapcampus.domain.EtatAnnonce
import com.swapcampus.domain.Reservee
import com.swapcampus.domain.StatistiquesVendeur
import com.swapcampus.domain.Vendue
import com.swapcampus.domain.statistiquesDe
import com.swapcampus.repository.AuthRepository
import com.swapcampus.repository.ListingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

/** Onglets de l'écran « Mes annonces ». */
enum class FiltreMesAnnonces(val libelle: String) {
    TOUTES("Toutes"),
    EN_LIGNE("En ligne"),
    VENDUES("Vendues")
}

/**
 * Espace vendeur : ses annonces, filtrées par état, et ses compteurs (UC7).
 */
class MyListingsViewModel(
    private val listingRepository: ListingRepository = AppContainer.listingRepository,
    private val authRepository: AuthRepository = AppContainer.authRepository
) : ViewModel() {

    sealed interface MesAnnoncesUiState {
        data object Chargement : MesAnnoncesUiState

        data class Pret(
            val annonces: List<Annonce>,
            val statistiques: StatistiquesVendeur
        ) : MesAnnoncesUiState

        data class Erreur(val message: String) : MesAnnoncesUiState
    }

    val utilisateurCourant: StateFlow<CompteUtilisateur?> = authRepository.currentUser

    private val _filtre = MutableStateFlow(FiltreMesAnnonces.TOUTES)
    val filtre: StateFlow<FiltreMesAnnonces> = _filtre.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MesAnnoncesUiState> = combine(utilisateurCourant, _filtre) { utilisateur, filtre ->
        utilisateur to filtre
    }
        .flatMapLatest { (utilisateur, filtre) -> fluxEspaceVendeur(utilisateur, filtre) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MesAnnoncesUiState.Chargement
        )

    fun onFiltreChange(nouveau: FiltreMesAnnonces) {
        _filtre.value = nouveau
    }

    private fun fluxEspaceVendeur(
        utilisateur: CompteUtilisateur?,
        filtre: FiltreMesAnnonces
    ): Flow<MesAnnoncesUiState> {
        if (utilisateur == null) {
            return flowOf(MesAnnoncesUiState.Pret(emptyList(), StatistiquesVendeur()) as MesAnnoncesUiState)
        }

        return combine(
            listingRepository.allListings(),
            listingRepository.sellerStats(utilisateur.id)
        ) { toutes, statistiques ->
            val mesAnnonces = toutes.filter { it.auteur.id == utilisateur.id }
            val visibles = when (filtre) {
                FiltreMesAnnonces.TOUTES -> mesAnnonces
                FiltreMesAnnonces.EN_LIGNE -> mesAnnonces.filter {
                    it.etat is Disponible || it.etat is Reservee
                }

                FiltreMesAnnonces.VENDUES -> mesAnnonces.filter { it.etat is Vendue }
            }
            MesAnnoncesUiState.Pret(
                annonces = visibles,
                statistiques = toutes.statistiquesDe(utilisateur.id)
            ) as MesAnnoncesUiState
        }
    }
}
