package com.isep.shared.repository

import com.isep.shared.domain.CatalogueAnnonces
import com.isep.shared.domain.entities.Annonce
import com.isep.shared.domain.entities.Categorie
import com.isep.shared.domain.entities.CompteUtilisateur
import com.isep.shared.domain.entities.Disponible
import com.isep.shared.domain.entities.EtatAnnonce
import com.isep.shared.domain.entities.Reservee
import com.isep.shared.domain.entities.Vendue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class ListingRepositoryImpl(
    private val catalogue: CatalogueAnnonces = CatalogueAnnonces()
) : ListingRepository {

    private val state = MutableStateFlow(catalogue.toutesLesAnnonces().toList())

    override fun allListings(): Flow<List<Annonce>> = state

    override fun searchListings(motCle: String?, categorie: Categorie?): Flow<List<Annonce>> =
        state.map { list ->
            list.filter { annonce ->
                val matchesKeyword = motCle.isNullOrBlank() ||
                    annonce.titre.contains(motCle, ignoreCase = true) ||
                    annonce.description.contains(motCle, ignoreCase = true)
                val matchesCategory = categorie == null || annonce.categorie == categorie
                matchesKeyword && matchesCategory
            }
        }

    override suspend fun getListingById(id: Int): Annonce? =
        state.value.firstOrNull { it.id == id }

    override suspend fun postListing(
        titre: String,
        description: String,
        prix: Double,
        categorie: Categorie,
        auteur: CompteUtilisateur,
        photoUrl: String?
    ): Result<Annonce> {
        val error = validate(titre, prix)
        if (error != null) return Result.failure(IllegalArgumentException(error))

        val created = catalogue.publierAnnonce(
            titre = titre.trim(),
            description = description.trim(),
            prix = prix,
            categorie = categorie,
            auteur = auteur,
            photoUrl = photoUrl
        )
        syncState()
        return Result.success(created)
    }

    override suspend fun updateListing(
        id: Int,
        titre: String?,
        description: String?,
        prix: Double?,
        categorie: Categorie?,
        photoUrl: String?
    ): Result<Annonce?> {
        val error = validate(titre, prix)
        if (error != null) return Result.failure(IllegalArgumentException(error))

        val updated = catalogue.modifierAnnonce(
            id, titre, description, prix, categorie, photoUrl
        )
        syncState()
        return Result.success(updated)
    }

    override suspend fun changeStatus(id: Int, nouvelEtat: EtatAnnonce): Result<Annonce> {
        val current = state.value.firstOrNull { it.id == id }
            ?: return Result.failure(NoSuchElementException("Listing not found: id=$id"))

        if (!isValidTransition(current.etat, nouvelEtat)) {
            return Result.failure(
                IllegalStateException("Invalid transition: ${current.etat} -> $nouvelEtat")
            )
        }

        val updated = catalogue.changerEtat(id, nouvelEtat)
            ?: return Result.failure(NoSuchElementException("Listing not found: id=$id"))

        syncState()
        return Result.success(updated)
    }

    override suspend fun removeListing(id: Int): Result<Unit> {
        val removed = catalogue.retirerAnnonce(id)
        return if (removed) {
            syncState()
            Result.success(Unit)
        } else {
            Result.failure(NoSuchElementException("Listing not found: id=$id"))
        }
    }

    private fun syncState() {
        state.value = catalogue.toutesLesAnnonces().toList()
    }

    private fun validate(titre: String?, prix: Double?): String? {
        if (titre != null && titre.isBlank()) return "Title must not be empty"
        if (prix != null && prix <= 0.0) return "Price must be greater than 0"
        return null
    }

    private fun isValidTransition(from: EtatAnnonce, to: EtatAnnonce): Boolean = when (from) {
        is Disponible -> to is Reservee
        is Reservee   -> to is Vendue || to is Disponible
        is Vendue     -> to is Disponible
    }
}