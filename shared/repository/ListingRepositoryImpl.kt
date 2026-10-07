package com.isep.shared.repository

import com.isep.shared.domain.CatalogueAnnonces
import com.isep.shared.domain.entities.Annonce
import com.isep.shared.domain.entities.Categorie
import com.isep.shared.domain.entities.CompteUtilisateur
import com.isep.shared.domain.entities.EtatAnnonce
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

    override suspend fun postListing(
        titre: String,
        description: String,
        prix: Double,
        categorie: Categorie,
        auteur: CompteUtilisateur,
        photoUrl: String?
    ): Annonce {
        val created = catalogue.publierAnnonce(
            titre, description, prix, categorie, auteur, photoUrl
        )
        syncState()
        return created
    }

    override suspend fun updateListing(
        id: Int,
        titre: String?,
        description: String?,
        prix: Double?,
        categorie: Categorie?,
        photoUrl: String?
    ): Annonce? {
        val updated = catalogue.modifierAnnonce(
            id, titre, description, prix, categorie, photoUrl
        )
        syncState()
        return updated
    }

    override suspend fun changeStatus(id: Int, nouvelEtat: EtatAnnonce): Annonce? {
        val updated = catalogue.changerEtat(id, nouvelEtat)
        syncState()
        return updated
    }

    override suspend fun removeListing(id: Int): Boolean {
        val removed = catalogue.retirerAnnonce(id)
        if (removed) syncState()
        return removed
    }

    private fun syncState() {
        state.value = catalogue.toutesLesAnnonces().toList()
    }
}