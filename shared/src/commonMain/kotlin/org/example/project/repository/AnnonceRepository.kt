package org.example.project.repository

import org.example.project.domain.Annonce
import org.example.project.domain.EtatAnnonce
import org.example.project.domain.CatalogueAnnonces

interface AnnonceRepository {
    fun allListings(): List<Annonce>
    fun searchListings(keyword: String): List<Annonce>
    fun postListing(annonce: Annonce)
    fun updateListing(annonce: Annonce)
    fun changeStatus(id: String, newStatus: EtatAnnonce)
    fun removeListing(id: String)
}

class AnnonceRepositoryImpl(
    private val catalogue: CatalogueAnnonces
) : AnnonceRepository {

    override fun allListings(): List<Annonce> = catalogue.allListings()

    override fun searchListings(keyword: String): List<Annonce> = catalogue.searchListings(keyword)

    override fun postListing(annonce: Annonce) = catalogue.postListing(annonce)

    override fun updateListing(annonce: Annonce) = catalogue.updateListing(annonce)

    override fun changeStatus(id: String, newStatus: EtatAnnonce) = catalogue.changeStatus(id, newStatus)

    override fun removeListing(id: String) = catalogue.removeListing(id)
}