package com.isep.shared.repository

import com.isep.shared.domain.CatalogueAnnonces
import com.isep.shared.domain.entities.Annonce
import com.isep.shared.domain.entities.Categorie
import com.isep.shared.domain.entities.CompteUtilisateur
import com.isep.shared.domain.entities.EtatAnnonce

class ListingRepositoryImpl(
    private val catalogue: CatalogueAnnonces = CatalogueAnnonces()
) : ListingRepository {

    override fun allListings(): List<Annonce> =
        catalogue.toutesLesAnnonces()

    override fun searchListings(motCle: String?, categorie: Categorie?): List<Annonce> =
        catalogue.rechercherAnnonces(motCle, categorie)

    override fun postListing(
        titre: String,
        description: String,
        prix: Double,
        categorie: Categorie,
        auteur: CompteUtilisateur,
        photoUrl: String?
    ): Annonce =
        catalogue.publierAnnonce(titre, description, prix, categorie, auteur, photoUrl)

    override fun updateListing(
        id: Int,
        titre: String?,
        description: String?,
        prix: Double?,
        categorie: Categorie?,
        photoUrl: String?
    ): Annonce? =
        catalogue.modifierAnnonce(id, titre, description, prix, categorie, photoUrl)

    override fun changeStatus(id: Int, nouvelEtat: EtatAnnonce): Annonce? =
        catalogue.changerEtat(id, nouvelEtat)

    override fun removeListing(id: Int): Boolean =
        catalogue.retirerAnnonce(id)
}