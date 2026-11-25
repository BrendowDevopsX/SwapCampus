package com.swapcampus.repository

import com.swapcampus.domain.ActionModeration
import com.swapcampus.domain.Annonce
import com.swapcampus.domain.CatalogueAnnonces
import com.swapcampus.domain.Categorie
import com.swapcampus.domain.CompteUtilisateur
import com.swapcampus.domain.ErreurMetier
import com.swapcampus.domain.EtatAnnonce
import com.swapcampus.domain.ReglesAnnonce
import com.swapcampus.domain.RoleUtilisateur
import com.swapcampus.domain.StatistiquesVendeur
import com.swapcampus.domain.favorisParmi
import com.swapcampus.domain.filtrerPourRecherche
import com.swapcampus.domain.statistiquesDe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Implémentation en mémoire du catalogue d'annonces.
 *
 * Expose l'état sous forme de [Flow] pour que les ViewModels observent les
 * changements sans se soucier du stockage (cahier des charges §8.5). Toutes les
 * décisions métier sont déléguées au domaine ([ReglesAnnonce],
 * [CatalogueAnnonces]) : ce dépôt ne fait que traduire en `Result`.
 *
 * @param catalogue catalogue source, injectable pour les tests.
 */
class ListingRepositoryImpl(
    private val catalogue: CatalogueAnnonces = CatalogueAnnonces()
) : ListingRepository {

    private val state = MutableStateFlow(catalogue.toutesLesAnnonces())
    private val moderations = MutableStateFlow(catalogue.actionsModeration())

    override fun allListings(): Flow<List<Annonce>> = state

    override fun searchListings(
        motCle: String?,
        categorie: Categorie?,
        prixMin: Double?,
        prixMax: Double?
    ): Flow<List<Annonce>> =
        state.map { annonces -> annonces.filtrerPourRecherche(motCle, categorie, prixMin, prixMax) }

    override fun favoriteListings(favoris: Set<Int>): Flow<List<Annonce>> =
        state.map { annonces -> annonces.favorisParmi(favoris) }

    override fun sellerStats(auteurId: Int): Flow<StatistiquesVendeur> =
        state.map { annonces -> annonces.statistiquesDe(auteurId) }

    override fun moderationActions(): Flow<List<ActionModeration>> = moderations

    override suspend fun getListingById(id: Int): Annonce? =
        state.value.firstOrNull { it.id == id }

    override suspend fun postListing(
        titre: String,
        description: String,
        prix: Double,
        categorie: Categorie,
        auteur: CompteUtilisateur,
        photoUrl: String?
    ): Result<Annonce> = encadrer {
        catalogue.publierAnnonce(
            titre = titre,
            description = description,
            prix = prix,
            categorie = categorie,
            auteur = auteur,
            photoUrl = photoUrl?.trim()?.ifBlank { null }
        )
    }

    override suspend fun updateListing(
        id: Int,
        titre: String?,
        description: String?,
        prix: Double?,
        categorie: Categorie?,
        photoUrl: String?
    ): Result<Annonce?> = encadrer {
        ReglesAnnonce.validerAnnonce(titre, prix, description)?.let { throw it.exception() }
        catalogue.modifierAnnonce(
            id = id,
            titre = titre,
            description = description,
            prix = prix,
            categorie = categorie,
            photoUrl = photoUrl?.trim()?.ifBlank { null }
        )
    }

    override suspend fun changeStatus(id: Int, nouvelEtat: EtatAnnonce): Result<Annonce> {
        val courante = state.value.firstOrNull { it.id == id }
            ?: return Result.failure(ErreurMetier.ANNONCE_INTROUVABLE.exception())

        if (!ReglesAnnonce.transitionAutorisee(courante.etat, nouvelEtat)) {
            return Result.failure(ErreurMetier.TRANSITION_INTERDITE.exception())
        }

        val miseAJour = catalogue.changerEtat(id, nouvelEtat)
            ?: return Result.failure(ErreurMetier.ANNONCE_INTROUVABLE.exception())

        synchroniser()
        return Result.success(miseAJour)
    }

    override suspend fun removeListing(id: Int): Result<Unit> =
        if (catalogue.retirerAnnonce(id)) {
            synchroniser()
            Result.success(Unit)
        } else {
            Result.failure(ErreurMetier.ANNONCE_INTROUVABLE.exception())
        }

    override suspend fun removeListingAsAdmin(
        id: Int,
        moderateur: CompteUtilisateur,
        motif: String
    ): Result<Unit> = encadrer {
        if (moderateur.role != RoleUtilisateur.ADMINISTRATEUR) {
            throw ErreurMetier.NON_ADMINISTRATEUR.exception()
        }
        ReglesAnnonce.validerModeration(motif)?.let { throw it.exception() }
        if (!catalogue.retirerAnnonceParAdmin(id, moderateur, motif)) {
            throw ErreurMetier.ANNONCE_INTROUVABLE.exception()
        }
    }

    override suspend fun republishListing(id: Int, auteur: CompteUtilisateur): Result<Annonce> {
        val originale = state.value.firstOrNull { it.id == id }
            ?: return Result.failure(ErreurMetier.ANNONCE_INTROUVABLE.exception())

        if (originale.auteur.id != auteur.id) {
            return Result.failure(ErreurMetier.NON_PROPRIETAIRE.exception())
        }

        return encadrer {
            catalogue.republierAnnonce(id, auteur)
                ?: throw ErreurMetier.ANNONCE_INTROUVABLE.exception()
        }
    }

    private fun synchroniser() {
        state.value = catalogue.toutesLesAnnonces()
        moderations.value = catalogue.actionsModeration()
    }

    /**
     * Exécute [bloc] puis rafraîchit les flux.
     *
     * Les erreurs métier levées par le domaine sont converties en échec de
     * `Result`, sans perdre le type ([com.swapcampus.domain.ErreurMetier]) : la
     * couche UI peut ainsi choisir le message traduit.
     */
    private inline fun <T> encadrer(bloc: () -> T): Result<T> = try {
        val valeur = bloc()
        synchroniser()
        Result.success(valeur)
    } catch (erreur: com.swapcampus.domain.ErreurMetierException) {
        Result.failure(erreur)
    }
}
