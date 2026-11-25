package com.swapcampus.ui.screens.login

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swapcampus.R
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

    /** Message d'erreur, porté par sa ressource pour rester traduisible. */
    private val _erreur: MutableStateFlow<Int?> = MutableStateFlow(null)
    @get:StringRes
    val erreur: StateFlow<Int?> = _erreur.asStateFlow()

    private val _chargement = MutableStateFlow(false)
    val chargement: StateFlow<Boolean> = _chargement.asStateFlow()

    fun onEmailChange(valeur: String) {
        _email.value = valeur
        _erreur.value = null
    }

    fun onMotDePasseChange(valeur: String) {
        _motDePasse.value = valeur
        _erreur.value = null
    }

    /** Pré-remplit le formulaire avec un compte de démonstration. */
    fun remplirAvec(compte: CompteUtilisateur) {
        _email.value = compte.email
        _motDePasse.value = compte.motDePasseHash
        _erreur.value = null
    }

    fun clearError() {
        _erreur.value = null
    }

    fun login(onSuccess: () -> Unit) {
        val email = _email.value.trim()
        val motDePasse = _motDePasse.value

        if (email.isEmpty() || motDePasse.isEmpty()) {
            _erreur.value = R.string.login_erreur_champs
            return
        }

        _chargement.value = true
        viewModelScope.launch {
            val compte = authRepository.authenticate(email, motDePasse)
            _chargement.value = false
            if (compte == null) {
                _erreur.value = R.string.login_erreur_identifiants
            } else {
                onSuccess()
            }
        }
    }
}
