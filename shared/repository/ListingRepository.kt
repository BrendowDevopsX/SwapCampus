package com.isep.shared.repository

import com.isep.shared.domain.entities.Annonce
import com.isep.shared.domain.entities.Categorie
import com.isep.shared.domain.entities.CompteUtilisateur
import com.isep.shared.domain.entities.EtatAnnonce

interface ListingRepository {

    fun allListings(): List<Annonce>

    fun searchListings(motCle: String?, categorie: Categorie?): List<Annonce>

    fun postListing(
        titre: String,
        description: String,
        prix: Double,
        categorie: Categorie,
        auteur: CompteUtilisateur,
        photoUrl: String? = null
    ): Annonce

    fun updateListing(
        id: Int,
        titre: String? = null,
        description: String? = null,
        prix: Double? = null,
        categorie: Categorie? = null,
        photoUrl: String? = null
    ): Annonce?

    fun changeStatus(id: Int, nouvelEtat: EtatAnnonce): Annonce?

    fun removeListing(id: Int): Boolean
}