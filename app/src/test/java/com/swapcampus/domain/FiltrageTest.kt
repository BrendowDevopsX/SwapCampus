package com.swapcampus.domain

import com.swapcampus.data.EchantillonDonnees
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Filtres de recherche avancés (UC3) et sélection des favoris, testés sur le
 * jeu de démonstration.
 */
class FiltrageTest {

    private val annonces = EchantillonDonnees.annoncesDemo()

    @Test
    fun `la fourchette de prix est inclusive`() {
        // Prix des annonces disponibles : 18,50 / 20 / 25 / 35 / 45.
        assertEquals(
            listOf(20.0, 25.0, 35.0),
            annonces.filtrerPourRecherche(null, null, 20.0, 35.0)
                .map { it.prix }
                .sorted()
        )
    }

    @Test
    fun `une borne minimale seule filtre les annonces trop bon marche`() {
        val resultat = annonces.filtrerPourRecherche(null, null, prixMin = 30.0)

        assertTrue(resultat.all { it.prix >= 30.0 })
        assertEquals(listOf(35.0, 45.0), resultat.map { it.prix }.sorted())
    }

    @Test
    fun `une borne maximale seule filtre les annonces trop cheres`() {
        val resultat = annonces.filtrerPourRecherche(null, null, prixMax = 20.0)

        assertTrue(resultat.all { it.prix <= 20.0 })
        assertEquals(listOf(18.5, 20.0), resultat.map { it.prix }.sorted())
    }

    @Test
    fun `le filtre de prix s'applique aussi aux annonces vendues exclues du catalogue`() {
        // Le casque vendu vaut 30 € : il ne doit jamais ressortir.
        val resultat = annonces.filtrerPourRecherche(null, null, 30.0, 30.0)

        assertTrue(resultat.isEmpty())
    }

    @Test
    fun `le filtre de prix se combine au mot cle et a la categorie`() {
        val resultat = annonces.filtrerPourRecherche(
            motCle = "kotlin",
            categorie = Categorie.LIVRES,
            prixMin = 10.0,
            prixMax = 20.0
        )

        assertEquals(listOf(1), resultat.map { it.id })
    }

    @Test
    fun `une fourchette incoherente ne renvoie rien`() {
        assertTrue(annonces.filtrerPourRecherche(null, null, 50.0, 10.0).isEmpty())
    }

    @Test
    fun `les favoris conservent les annonces vendues`() {
        // 7 et 8 sont vendues : elles doivent rester visibles dans les favoris.
        val favoris = annonces.favorisParmi(setOf(7, 8))

        assertEquals(listOf(7, 8), favoris.map { it.id }.sorted())
    }

    @Test
    fun `les favoris ignorent les identifiants inconnus et gardent le tri par date`() {
        val favoris = annonces.favorisParmi(setOf(1, 2, 999))

        assertEquals(listOf(1, 2), favoris.map { it.id }.sorted())
        assertEquals(
            favoris.sortedWith(
                compareByDescending<Annonce> { it.dateCreation }.thenByDescending { it.id }
            ),
            favoris
        )
    }

    @Test
    fun `sans favori la liste est vide`() {
        assertTrue(annonces.favorisParmi(emptySet()).isEmpty())
    }

    @Test
    fun `le resume tronque les descriptions longues`() {
        val longue = annonces.first().copy(description = "a".repeat(300))

        assertEquals(111, longue.resume().length)
        assertTrue(longue.resume().endsWith("…"))
    }

    @Test
    fun `le jeu de demonstration couvre les trois etats`() {
        assertEquals(5, annonces.disponibles().size)
        assertEquals(1, annonces.count { it.etat is Reservee })
        assertEquals(2, annonces.count { it.etat is Vendue })
    }
}
