package com.swapcampus.domain

import com.swapcampus.data.EchantillonDonnees
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Tests des règles portées par le catalogue et les extensions de domaine. */
class CatalogueAnnoncesTest {

    private fun annonce(
        id: Int,
        titre: String = "Objet $id",
        prix: Double = 10.0,
        categorie: Categorie = Categorie.AUTRE,
        etat: EtatAnnonce = Disponible,
        auteur: CompteUtilisateur = EchantillonDonnees.alice
    ) = Annonce(
        id = id,
        titre = titre,
        description = "Description de $titre",
        prix = prix,
        categorie = categorie,
        photoUrl = null,
        auteur = auteur,
        etat = etat,
        dateCreation = aujourdHui()
    )

    @Test
    fun `les identifiants des nouvelles annonces s incremented sans collision`() {
        val catalogue = CatalogueAnnonces(listOf(annonce(7), annonce(3)))

        val premiere = catalogue.publierAnnonce("A", "a", 1.0, Categorie.AUTRE, EchantillonDonnees.alice)
        val seconde = catalogue.publierAnnonce("B", "b", 2.0, Categorie.AUTRE, EchantillonDonnees.bob)

        assertEquals(8, premiere.id)
        assertEquals(9, seconde.id)
        assertEquals(4, catalogue.toutesLesAnnonces().size)
    }

    @Test
    fun `retirer une annonce renvoie vrai seulement si elle existait`() {
        val catalogue = CatalogueAnnonces(listOf(annonce(1)))

        assertTrue(catalogue.retirerAnnonce(1))
        assertFalse(catalogue.retirerAnnonce(1))
        assertTrue(catalogue.toutesLesAnnonces().isEmpty())
    }

    @Test
    fun `changer l etat d une annonce inconnue renvoie null`() {
        val catalogue = CatalogueAnnonces()

        assertNull(catalogue.changerEtat(42, Reservee("Personne")))
    }

    @Test
    fun `modifier une annonce inconnue renvoie null`() {
        val catalogue = CatalogueAnnonces()

        assertNull(catalogue.modifierAnnonce(42, titre = "Fantôme"))
    }

    @Test
    fun `la recherche du catalogue filtre les annonces vendues`() {
        val catalogue = CatalogueAnnonces(EchantillonDonnees.annoncesDemo())

        val resultat = catalogue.rechercherAnnonces()

        assertEquals(5, resultat.size)
        assertTrue(resultat.all { it.etat is Disponible })
    }

    @Test
    fun `les statistiques d une liste vide sont nulles`() {
        val stats = emptyList<Annonce>().statistiquesDe(EchantillonDonnees.alice.id)

        assertEquals(0, stats.publiees)
        assertEquals(0, stats.enLigne)
    }

    @Test
    fun `les statistiques ignorent les annonces des autres vendeurs`() {
        val annonces = listOf(
            annonce(1, auteur = EchantillonDonnees.alice),
            annonce(2, auteur = EchantillonDonnees.bob),
            annonce(3, auteur = EchantillonDonnees.alice, etat = Vendue(aujourdHui()))
        )

        val stats = annonces.statistiquesDe(EchantillonDonnees.alice.id)

        assertEquals(2, stats.publiees)
        assertEquals(1, stats.disponibles)
        assertEquals(1, stats.vendues)
        assertEquals(1, stats.enLigne)
    }

    @Test
    fun `resume tronque les descriptions trop longues`() {
        val longue = annonce(1).copy(description = "a".repeat(400))

        assertTrue(longue.resume().length < 130)
        assertTrue(longue.resume().endsWith("…"))
    }
}
