package com.isep.composeapp.ui.screens.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isep.shared.domain.entities.Categorie
import com.isep.shared.repository.AuthRepository
import com.isep.shared.repository.ListingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostListingViewModel(
    private val listingRepository: ListingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun submit(
        titre: String,
        description: String,
        prix: Double,
        categorie: Categorie,
        photoUrl: String?,
        onSuccess: () -> Unit
    ) {
        val user = authRepository.currentUser.value
        if (user == null) {
            _error.value = "You must be logged in"
            return
        }

        viewModelScope.launch {
            listingRepository.postListing(
                titre = titre,
                description = description,
                prix = prix,
                categorie = categorie,
                auteur = user,
                photoUrl = photoUrl
            ).onSuccess {
                _error.value = null
                onSuccess()
            }.onFailure {
                _error.value = it.message ?: "Unknown error"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}