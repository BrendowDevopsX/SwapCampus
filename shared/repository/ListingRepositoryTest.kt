package com.isep.shared.repository

import com.isep.shared.domain.RoleUtilisateur
import com.isep.shared.domain.entities.Categorie
import com.isep.shared.domain.entities.CompteUtilisateur
import com.isep.shared.domain.entities.Disponible
import com.isep.shared.domain.entities.Reservee
import com.isep.shared.domain.entities.Vendue
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
        assertTrue(repo.postListing("", "desc", 10.0, Categorie.LIVRES, auteur).isFailure)
    }

    @Test
    fun postListing_rejects_blank_title() = runTest {
        val repo = ListingRepositoryImpl()
        assertTrue(repo.postListing("   ", "desc", 10.0, Categorie.LIVRES, auteur).isFailure)
    }

    @Test
    fun postListing_rejects_zero_price() = runTest {
        val repo = ListingRepositoryImpl()
        assertTrue(repo.postListing("Book", "desc", 0.0, Categorie.LIVRES, auteur).isFailure)
    }

    @Test
    fun postListing_rejects_negative_price() = runTest {
        val repo = ListingRepositoryImpl()
        assertTrue(repo.postListing("Book", "desc", -5.0, Categorie.LIVRES, auteur).isFailure)
    }

    @Test
    fun postListing_accepts_valid_listing() = runTest {
        val repo = ListingRepositoryImpl()
        assertTrue(repo.postListing("Kotlin Book", "desc", 15.0, Categorie.LIVRES, auteur).isSuccess)
    }

    @Test
    fun changeStatus_allows_Disponible_to_Reservee() = runTest {
        val repo = ListingRepositoryImpl()
        val annonce = repo.postListing("Book", "d", 15.0, Categorie.LIVRES, auteur).getOrThrow()
        val result = repo.changeStatus(annonce.id, Reservee("bob@etu.fr"))
        assertTrue(result.isSuccess)
    }

    @Test
    fun changeStatus_rejects_Disponible_to_Vendue() = runTest {
        val repo = ListingRepositoryImpl()
        val annonce = repo.postListing("Book", "d", 15.0, Categorie.LIVRES, auteur).getOrThrow()
        val result = repo.changeStatus(annonce.id, Vendue(LocalDate(2025, 10, 1)))
        assertTrue(result.isFailure)
    }

    @Test
    fun changeStatus_allows_Reservee_to_Vendue() = runTest {
        val repo = ListingRepositoryImpl()
        val annonce = repo.postListing("Book", "d", 15.0, Categorie.LIVRES, auteur).getOrThrow()
        repo.changeStatus(annonce.id, Reservee("bob@etu.fr"))
        val result = repo.changeStatus(annonce.id, Vendue(LocalDate(2025, 10, 1)))
        assertTrue(result.isSuccess)
    }

    @Test
    fun changeStatus_allows_Reservee_to_Disponible() = runTest {
        val repo = ListingRepositoryImpl()
        val annonce = repo.postListing("Book", "d", 15.0, Categorie.LIVRES, auteur).getOrThrow()
        repo.changeStatus(annonce.id, Reservee("bob@etu.fr"))
        val result = repo.changeStatus(annonce.id, Disponible)
        assertTrue(result.isSuccess)
    }

    @Test
    fun changeStatus_allows_Vendue_to_Disponible() = runTest {
        val repo = ListingRepositoryImpl()
        val annonce = repo.postListing("Book", "d", 15.0, Categorie.LIVRES, auteur).getOrThrow()
        repo.changeStatus(annonce.id, Reservee("bob@etu.fr"))
        repo.changeStatus(annonce.id, Vendue(LocalDate(2025, 10, 1)))
        val result = repo.changeStatus(annonce.id, Disponible)
        assertTrue(result.isSuccess)
    }

    @Test
    fun changeStatus_rejects_Vendue_to_Reservee() = runTest {
        val repo = ListingRepositoryImpl()
        val annonce = repo.postListing("Book", "d", 15.0, Categorie.LIVRES, auteur).getOrThrow()
        repo.changeStatus(annonce.id, Reservee("bob@etu.fr"))
        repo.changeStatus(annonce.id, Vendue(LocalDate(2025, 10, 1)))
        val result = repo.changeStatus(annonce.id, Reservee("carol@etu.fr"))
        assertTrue(result.isFailure)
    }
}