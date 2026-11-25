package com.swapcampus.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swapcampus.di.AppContainer
import com.swapcampus.domain.CompteUtilisateur
import com.swapcampus.domain.StatistiquesVendeur
import com.swapcampus.repository.AuthRepository
import com.swapcampus.repository.ListingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

/**
 * Profil de l'utilisateur connecté et récapitulatif de son activité (UC7).
 */
class ProfileViewModel(
    private val authRepository: AuthRepository = AppContainer.authRepository,
    listingRepository: ListingRepository = AppContainer.listingRepository
) : ViewModel() {

    val currentUser: StateFlow<CompteUtilisateur?> = authRepository.currentUser

    @OptIn(ExperimentalCoroutinesApi::class)
    val statistiques: StateFlow<StatistiquesVendeur> = authRepository.currentUser
        .flatMapLatest { utilisateur ->
            if (utilisateur == null) {
                flowOf(StatistiquesVendeur())
            } else {
                listingRepository.sellerStats(utilisateur.id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatistiquesVendeur()
        )

    fun logout() {
        authRepository.logout()
    }
}
