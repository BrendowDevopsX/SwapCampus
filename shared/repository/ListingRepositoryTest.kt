package com.isep.shared.repository

import com.isep.shared.domain.RoleUtilisateur
import com.isep.shared.domain.entities.Categorie
import com.isep.shared.domain.entities.CompteUtilisateur
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertTrue

class ListingRepositoryTest {

    private val auteur = CompteUtilisateur(
        id = 1,
        nom = "Alice",
        email = "alice@etu.fr",
        motDePasseHash = "x",
        dateCreation = LocalDate(2025, 1, 1),
        role = RoleUtilisateur.ETUDIANT
    )

    @Test
    fun postListing_rejects_empty_title() = runTest {
        val repo = ListingRepositoryImpl()
        val result = repo.postListing("", "desc", 10.0, Categorie.LIVRES, auteur)
        assertTrue(result.isFailure)
    }

    @Test
    fun postListing_rejects_blank_title() = runTest {
        val repo = ListingRepositoryImpl()
        val result = repo.postListing("   ", "desc", 10.0, Categorie.LIVRES, auteur)
        assertTrue(result.isFailure)
    }

    @Test
    fun postListing_rejects_zero_price() = runTest {
        val repo = ListingRepositoryImpl()
        val result = repo.postListing("Book", "desc", 0.0, Categorie.LIVRES, auteur)
        assertTrue(result.isFailure)
    }

    @Test
    fun postListing_rejects_negative_price() = runTest {
        val repo = ListingRepositoryImpl()
        val result = repo.postListing("Book", "desc", -5.0, Categorie.LIVRES, auteur)
        assertTrue(result.isFailure)
    }

    @Test
    fun postListing_accepts_valid_listing() = runTest {
        val repo = ListingRepositoryImpl()
        val result = repo.postListing("Kotlin Book", "desc", 15.0, Categorie.LIVRES, auteur)
        assertTrue(result.isSuccess)
    }
}