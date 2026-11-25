package com.swapcampus.repository

import com.swapcampus.domain.Annonce
import com.swapcampus.domain.CatalogueAnnonces
import com.swapcampus.domain.Categorie
import com.swapcampus.domain.CompteUtilisateur
import com.swapcampus.domain.Disponible
import com.swapcampus.domain.EtatAnnonce
import com.swapcampus.domain.Reservee
import com.swapcampus.domain.StatistiquesVendeur
import com.swapcampus.domain.Vendue
import com.swapcampus.domain.filtrerPourRecherche
import com.swapcampus.domain.statistiquesDe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Implémentation en mémoire du catalogue d'annonces.
 *
 * Expose l'état sous forme de [Flow] pour que les ViewModels observent les
 * changements sans se soucier du stockage (cahier des charges §8.5).
 *
 * @param catalogue catalogue source, injectable pour les tests.
 */
class ListingRepositoryImpl(
    private val catalogue: CatalogueAnnonces = CatalogueAnnonces()
) : ListingRepository {

    private val state = MutableStateFlow(catalogue.toutesLesAnnonces())

    override fun allListings(): Flow<List<Annonce>> = state

    override fun searchListings(motCle: String?, categorie: Categorie?): Flow<List<Annonce>> =
        state.map { annonces -> annonces.filtrerPourRecherche(motCle, categorie) }

    override fun sellerStats(auteurId: Int): Flow<StatistiquesVendeur> =
        state.map { annonces -> annonces.statistiquesDe(auteurId) }

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
            photoUrl = photoUrl?.trim()?.ifBlank { null }
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
            id = id,
            titre = titre?.trim(),
            description = description,
            prix = prix,
            categorie = categorie,
            photoUrl = photoUrl?.trim()?.ifBlank { null }
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
        state.value = catalogue.toutesLesAnnonces()
    }

    private fun validate(titre: String?, prix: Double?): String? {
        if (titre != null && titre.isBlank()) return "Le titre ne doit pas être vide."
        if (prix != null && prix <= 0.0) return "Le prix doit être supérieur à 0 €."
        return null
    }

    private fun isValidTransition(from: EtatAnnonce, to: EtatAnnonce): Boolean = when (from) {
        is Disponible -> to is Reservee
        is Reservee -> to is Vendue || to is Disponible
        is Vendue -> to is Disponible
    }
}
