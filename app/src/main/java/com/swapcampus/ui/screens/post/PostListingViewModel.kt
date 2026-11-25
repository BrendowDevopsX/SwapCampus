package com.swapcampus.ui.screens.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swapcampus.di.AppContainer
import com.swapcampus.domain.Categorie
import com.swapcampus.repository.AuthRepository
import com.swapcampus.repository.ListingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Création d'annonce (UC1). Les règles métier (titre non vide, prix > 0) sont
 * vérifiées dans le domaine ; l'écran ne fait que remonter l'erreur obtenue.
 */
class PostListingViewModel(
    private val listingRepository: ListingRepository = AppContainer.listingRepository,
    private val authRepository: AuthRepository = AppContainer.authRepository
) : ViewModel() {

    private val _erreur = MutableStateFlow<Throwable?>(null)
    val erreur: StateFlow<Throwable?> = _erreur.asStateFlow()

    private val _enregistrement = MutableStateFlow(false)
    val enregistrement: StateFlow<Boolean> = _enregistrement.asStateFlow()

    fun submit(
        titre: String,
        description: String,
        prix: Double,
        categorie: Categorie,
        photoUrl: String?,
        onSuccess: () -> Unit
    ) {
        val utilisateur = authRepository.currentUser.value
        if (utilisateur == null) {
            _erreur.value = IllegalStateException("connexion requise")
            return
        }

        _enregistrement.value = true
        viewModelScope.launch {
            listingRepository.postListing(
                titre = titre,
                description = description,
                prix = prix,
                categorie = categorie,
                auteur = utilisateur,
                photoUrl = photoUrl
            ).onSuccess {
                _erreur.value = null
                onSuccess()
            }.onFailure {
                _erreur.value = it
            }
            _enregistrement.value = false
        }
    }

    fun clearError() {
        _erreur.value = null
    }
}
