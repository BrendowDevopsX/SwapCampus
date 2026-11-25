package com.swapcampus.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Favoris (« mis de côté ») d'un utilisateur.
 *
 * Comme le reste du TP1, l'ensemble est gardé en mémoire : rien n'est persisté
 * (§8.3). L'API est asynchrone pour que le remplacement par un stockage local
 * au TP2 ne change pas les appelants.
 */
interface FavorisRepository {

    /** Identifiants des annonces mises de côté par [utilisateurId]. */
    fun favorisDe(utilisateurId: Int): Flow<Set<Int>>

    /** Vrai si [annonceId] est déjà dans les favoris de [utilisateurId]. */
    fun estFavori(utilisateurId: Int, annonceId: Int): Boolean

    /**
     * Ajoute ou retire [annonceId] des favoris de [utilisateurId].
     *
     * @return le nouvel ensemble de favoris.
     */
    suspend fun basculer(utilisateurId: Int, annonceId: Int): Set<Int>

    /** Vide les favoris de [utilisateurId] (déconnexion). */
    fun vider(utilisateurId: Int)
}

/** Implémentation en mémoire, une entrée par utilisateur. */
class FavorisRepositoryImpl : FavorisRepository {

    private val parUtilisateur = mutableMapOf<Int, MutableStateFlow<Set<Int>>>()

    private fun fluxDe(utilisateurId: Int): MutableStateFlow<Set<Int>> =
        parUtilisateur.getOrPut(utilisateurId) { MutableStateFlow(emptySet()) }

    override fun favorisDe(utilisateurId: Int): Flow<Set<Int>> =
        fluxDe(utilisateurId).map { it }

    override fun estFavori(utilisateurId: Int, annonceId: Int): Boolean =
        annonceId in fluxDe(utilisateurId).value

    override suspend fun basculer(utilisateurId: Int, annonceId: Int): Set<Int> {
        val flux = fluxDe(utilisateurId)
        val nouveaux = if (annonceId in flux.value) {
            flux.value - annonceId
        } else {
            flux.value + annonceId
        }
        flux.value = nouveaux
        return nouveaux
    }

    override fun vider(utilisateurId: Int) {
        fluxDe(utilisateurId).value = emptySet()
    }
}
