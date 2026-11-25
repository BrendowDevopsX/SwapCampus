package com.swapcampus.repository

import com.swapcampus.domain.Annonce
import com.swapcampus.domain.Categorie
import com.swapcampus.domain.CompteUtilisateur
import com.swapcampus.domain.EtatAnnonce
import com.swapcampus.domain.StatistiquesVendeur
import kotlinx.coroutines.flow.Flow

interface ListingRepository {

    /** Toutes les annonces, tous états confondus (écran « Mes annonces »). */
    fun allListings(): Flow<List<Annonce>>

    /**
     * Annonces du catalogue public : uniquement les annonces disponibles,
     * filtrées par mot-clé et/ou catégorie (UC2 et UC3).
     */
    fun searchListings(motCle: String?, categorie: Categorie?): Flow<List<Annonce>>

    /** Statistiques d'un vendeur (UC7). */
    fun sellerStats(auteurId: Int): Flow<StatistiquesVendeur>

    suspend fun getListingById(id: Int): Annonce?

    /**
     * @return l'annonce créée, ou un échec si le titre est vide ou le prix ≤ 0.
     */
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
