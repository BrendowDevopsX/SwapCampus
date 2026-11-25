package com.swapcampus.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swapcampus.di.AppContainer
import com.swapcampus.domain.ActionModeration
import com.swapcampus.domain.CompteUtilisateur
import com.swapcampus.domain.RoleUtilisateur
import com.swapcampus.domain.StatistiquesVendeur
import com.swapcampus.repository.AuthRepository
import com.swapcampus.repository.ListingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Profil de l'utilisateur connecté, récapitulatif de son activité (UC7) et,
 * pour un administrateur, journal des suppressions motivées (§3.2).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val authRepository: AuthRepository = AppContainer.authRepository,
    private val listingRepository: ListingRepository = AppContainer.listingRepository
) : ViewModel() {

    val currentUser: StateFlow<CompteUtilisateur?> = authRepository.currentUser

    val estAdministrateur: StateFlow<Boolean> = authRepository.currentUser
        .map { it?.role == RoleUtilisateur.ADMINISTRATEUR }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

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

    /** Journal de modération, affiché uniquement aux administrateurs. */
    val moderations: StateFlow<List<ActionModeration>> = authRepository.currentUser
        .flatMapLatest { utilisateur ->
            if (utilisateur?.role == RoleUtilisateur.ADMINISTRATEUR) {
                listingRepository.moderationActions()
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun logout() {
        authRepository.logout()
    }
}
