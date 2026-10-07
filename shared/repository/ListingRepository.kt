package com.isep.shared.repository

import com.isep.shared.domain.entities.Annonce
import com.isep.shared.domain.entities.Categorie
import com.isep.shared.domain.entities.CompteUtilisateur
import com.isep.shared.domain.entities.EtatAnnonce
import kotlinx.coroutines.flow.Flow

interface ListingRepository {

    fun allListings(): Flow<List<Annonce>>

    fun searchListings(motCle: String?, categorie: Categorie?): Flow<List<Annonce>>

    suspend fun getListingById(id: Int): Annonce?

    suspend fun postListing(
        titre: String,
        description: String,
        prix: Double,
        categorie: Categorie,
        auteur: CompteUtilisateur,
        photoUrl: String? = null
    ): Result<Annonce>

    suspend fun updateListing(
        id: Int,
        titre: String? = null,
        description: String? = null,
        prix: Double? = null,
        categorie: Categorie? = null,
        photoUrl: String? = null
    ): Result<Annonce?>

    suspend fun changeStatus(id: Int, nouvelEtat: EtatAnnonce): Result<Annonce>

    suspend fun removeListing(id: Int): Result<Unit>
}