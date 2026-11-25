package com.swapcampus.repository

import com.swapcampus.data.EchantillonDonnees
import com.swapcampus.domain.CatalogueAnnonces
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Favoris (P1) : mémoire par utilisateur, bascule idempotente et accès aux
 * annonces favorites quel que soit leur état.
 */
class FavorisRepositoryTest {

    @Test
    fun `basculer ajoute puis retire un favori`() = runTest {
        val depot = FavorisRepositoryImpl()

        assertEquals(setOf(4), depot.basculer(1, 4))
        assertTrue(depot.estFavori(1, 4))
        assertEquals(setOf(4), depot.favorisDe(1).first())

        assertEquals(emptySet(), depot.basculer(1, 4))
        assertFalse(depot.estFavori(1, 4))
    }

    @Test
    fun `les favoris sont propres a chaque utilisateur`() = runTest {
        val depot = FavorisRepositoryImpl()

        depot.basculer(1, 4)
        depot.basculer(2, 2)

        assertEquals(setOf(4), depot.favorisDe(1).first())
        assertEquals(setOf(2), depot.favorisDe(2).first())
    }

    @Test
    fun `vider efface les favoris d'un seul utilisateur`() = runTest {
        val depot = FavorisRepositoryImpl()
        depot.basculer(1, 4)
        depot.basculer(2, 2)

        depot.vider(1)

        assertTrue(depot.favorisDe(1).first().isEmpty())
        assertEquals(setOf(2), depot.favorisDe(2).first())
    }

    @Test
    fun `le depot d'annonces expose les favoris, y compris les annonces vendues`() = runTest {
        val depot = ListingRepositoryImpl(CatalogueAnnonces(EchantillonDonnees.annoncesDemo()))

        val favoris = depot.favoriteListings(setOf(7, 1)).first()

        assertEquals(listOf(1, 7), favoris.map { it.id }.sorted())
    }

    @Test
    fun `sans favori la liste est vide`() = runTest {
        val depot = ListingRepositoryImpl(CatalogueAnnonces(EchantillonDonnees.annoncesDemo()))

        assertTrue(depot.favoriteListings(emptySet()).first().isEmpty())
    }
}
