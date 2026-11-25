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
 * vérifiées par le dépôt ; l'écran ne fait que remonter le message d'erreur.
 */
class PostListingViewModel(
    private val listingRepository: ListingRepository = AppContainer.listingRepository,
    private val authRepository: AuthRepository = AppContainer.authRepository
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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
            _error.value = "Connectez-vous pour publier une annonce."
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
                _error.value = null
                onSuccess()
            }.onFailure {
                _error.value = it.message ?: "Publication impossible."
            }
            _enregistrement.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
