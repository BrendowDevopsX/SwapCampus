package com.isep.shared.domain

import com.isep.shared.domain.entities.Annonce
import com.isep.shared.domain.entities.Categorie
import com.isep.shared.domain.entities.CompteUtilisateur
import com.isep.shared.domain.entities.Disponible
import com.isep.shared.domain.entities.EtatAnnonce
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class CatalogueAnnonces {

    private val annonces = mutableListOf<Annonce>()
    private var prochainId = 1

    fun toutesLesAnnonces(): List<Annonce> = annonces

    fun publierAnnonce(
        titre: String,
        description: String,
        prix: Double,
        categorie: Categorie,
        auteur: CompteUtilisateur,
        photoUrl: String? = null
    ): Annonce {
        val nouvelleAnnonce = Annonce(
            id = prochainId,
            titre = titre,
            description = description,
            prix = prix,
            categorie = categorie,
            photoUrl = photoUrl,
            auteur = auteur,
            etat = Disponible,
            dateCreation = aujourdHui()
        )

        return nouvelleAnnonce.also {
            annonces.add(it)
            prochainId++
            println("Annonce publiée : ${it.titre} (id=${it.id})")
        }
    }

    fun rechercherAnnonces(
        motCle: String? = null,
        categorie: Categorie? = null
    ): List<Annonce> = annonces
        .disponibles()
        .filter { annonce ->
            (motCle == null ||
                annonce.titre.contains(motCle, ignoreCase = true) ||
                annonce.description.contains(motCle, ignoreCase = true)) &&
                (categorie == null || annonce.categorie == categorie)
        }

    fun modifierAnnonce(
        id: Int,
        titre: String? = null,
        description: String? = null,
        prix: Double? = null,
        categorie: Categorie? = null,
        photoUrl: String? = null
    ): Annonce? {
        val index = annonces.indexOfFirst { it.id == id }
        if (index == -1) return null
        val existante = annonces[index]
        val miseAJour = existante.copy(
            titre = titre ?: existante.titre,
            description = description ?: existante.description,
            prix = prix ?: existante.prix,
            categorie = categorie ?: existante.categorie,
            photoUrl = photoUrl ?: existante.photoUrl
        )
        annonces[index] = miseAJour
        return miseAJour
    }

    fun changerEtat(id: Int, nouvelEtat: EtatAnnonce): Annonce? {
        val index = annonces.indexOfFirst { it.id == id }
        if (index == -1) return null
        return annonces[index].copy(etat = nouvelEtat).also { annonces[index] = it }
    }

    fun retirerAnnonce(id: Int): Boolean = annonces.removeAll { it.id == id }
}

/** Date du jour, en `kotlinx.datetime.LocalDate` (portable Android/iOS). */
private fun aujourdHui() =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
