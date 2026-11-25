package com.swapcampus.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swapcampus.di.AppContainer
import com.swapcampus.domain.CompteUtilisateur
import com.swapcampus.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Connexion simplifiée (§8.2) : les comptes sont simulés en mémoire
 * (voir `EchantillonDonnees`), sans hachage de mot de passe.
 */
class LoginViewModel(
    private val authRepository: AuthRepository = AppContainer.authRepository
) : ViewModel() {

    /** Comptes de démonstration affichés sous le formulaire pour aller vite. */
    val comptesDemo: List<CompteUtilisateur> = authRepository.comptesDisponibles

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _motDePasse = MutableStateFlow("")
    val motDePasse: StateFlow<String> = _motDePasse.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _chargement = MutableStateFlow(false)
    val chargement: StateFlow<Boolean> = _chargement.asStateFlow()

    fun onEmailChange(valeur: String) {
        _email.value = valeur
        _error.value = null
    }

    fun onMotDePasseChange(valeur: String) {
        _motDePasse.value = valeur
        _error.value = null
    }

    /** Pré-remplit le formulaire avec un compte de démonstration. */
    fun remplirAvec(compte: CompteUtilisateur) {
        _email.value = compte.email
        _motDePasse.value = compte.motDePasseHash
        _error.value = null
    }

    fun clearError() {
        _error.value = null
    }

    fun login(onSuccess: () -> Unit) {
        val email = _email.value.trim()
        val motDePasse = _motDePasse.value

        if (email.isEmpty() || motDePasse.isEmpty()) {
            _error.value = "Renseignez votre email et votre mot de passe."
            return
        }

        _chargement.value = true
        viewModelScope.launch {
            val compte = authRepository.authenticate(email, motDePasse)
            _chargement.value = false
            if (compte == null) {
                _error.value = "Email ou mot de passe incorrect."
            } else {
                onSuccess()
            }
        }
    }
}
