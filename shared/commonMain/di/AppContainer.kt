package com.swapcampus.di

import com.swapcampus.data.EchantillonDonnees
import com.swapcampus.domain.CatalogueAnnonces
import com.swapcampus.repository.AuthRepository
import com.swapcampus.repository.AuthRepositoryImpl
import com.swapcampus.repository.ListingRepository
import com.swapcampus.repository.ListingRepositoryImpl

/**
 * Injection de dépendances manuelle : une seule instance par repository pour
 * toute la durée de vie de l'application (TP1, pas de framework DI).
 */
object AppContainer {

    val authRepository: AuthRepository by lazy { AuthRepositoryImpl() }

    /** Catalogue pré-rempli avec le jeu de démonstration, en mémoire. */
    val listingRepository: ListingRepository by lazy {
        ListingRepositoryImpl(CatalogueAnnonces(EchantillonDonnees.annoncesDemo()))
    }
}
