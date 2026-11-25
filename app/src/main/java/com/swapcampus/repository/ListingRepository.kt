package com.swapcampus.repository

import com.swapcampus.domain.ActionModeration
import com.swapcampus.domain.Annonce
import com.swapcampus.domain.Categorie
import com.swapcampus.domain.CompteUtilisateur
import com.swapcampus.domain.EtatAnnonce
import com.swapcampus.domain.StatistiquesVendeur
import kotlinx.coroutines.flow.Flow

/**
 * Accès aux annonces. Les erreurs métier sont remontées sous forme de
 * [com.swapcampus.domain.ErreurMetierException] pour que la couche UI puisse
 * les traduire.
 */
interface ListingRepository {

    /** Toutes les annonces, tous états confondus (écran « Mes annonces »). */
    fun allListings(): Flow<List<Annonce>>

    /**
     * Annonces du catalogue public : uniquement les annonces disponibles,
     * filtrées par mot-clé, catégorie et fourchette de prix (UC2 et UC3).
     *
     * @param prixMin prix minimal inclus, `null` pour ne pas borner.
     * @param prixMax prix maximal inclus, `null` pour ne pas borner.
     */
    fun searchListings(
        motCle: String?,
        categorie: Categorie?,
        prixMin: Double? = null,
        prixMax: Double? = null
    ): Flow<List<Annonce>>

    /** Annonces favorites de l'utilisateur, tous états confondus. */
    fun favoriteListings(favoris: Set<Int>): Flow<List<Annonce>>

    /** Statistiques d'un vendeur (UC7). */
    fun sellerStats(auteurId: Int): Flow<StatistiquesVendeur>

    /** Journal des suppressions administratives (UC6, §3.2). */
    fun moderationActions(): Flow<List<ActionModeration>>

    suspend fun getListingById(id: Int): Annonce?

    /**
     * @return l'annonce créée, ou un échec si le titre est vide, trop long,
     *     ou si le prix est ≤ 0.
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

    /** Suppression par le propriétaire (UC6). */
    suspend fun removeListing(id: Int): Result<Unit>

    /**
     * Suppression par un administrateur, avec motif obligatoire (UC6, §3.2).
     *
     * @return un échec [com.swapcampus.domain.ErreurMetier.NON_ADMINISTRATEUR]
     *     si [moderateur] n'a pas le rôle attendu, ou
     *     [com.swapcampus.domain.ErreurMetier.MOTIF_OBLIGATOIRE] si le motif est vide.
     */
    suspend fun removeListingAsAdmin(
        id: Int,
        moderateur: CompteUtilisateur,
        motif: String
    ): Result<Unit>

    /**
     * Republie une annonce vendue en copie (UC4) : nouvel identifiant, état
     * [com.swapcampus.domain.Disponible] et date du jour.
     */
    suspend fun republishListing(id: Int, auteur: CompteUtilisateur): Result<Annonce>
}
