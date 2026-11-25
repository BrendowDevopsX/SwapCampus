package com.swapcampus.repository

import com.swapcampus.data.EchantillonDonnees
import com.swapcampus.domain.CatalogueAnnonces
import com.swapcampus.domain.Categorie
import com.swapcampus.domain.Disponible
import com.swapcampus.domain.Reservee
import com.swapcampus.domain.Vendue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests unitaires du catalogue vendeur (UC1 à UC7, cahier des charges §8.8).
 *
 * Le dépôt est en mémoire : chaque test construit son propre catalogue pour
 * rester indépendant des autres.
 */
class ListingRepositoryTest {

    private fun depot(annonces: List<com.swapcampus.domain.Annonce> = emptyList()) =
        ListingRepositoryImpl(CatalogueAnnonces(annonces))

    private fun depotDemo() = ListingRepositoryImpl(CatalogueAnnonces(EchantillonDonnees.annoncesDemo()))

    // --- UC2 / UC3 : consultation et filtres ---------------------------------

    @Test
    fun `le catalogue ne montre que les annonces disponibles`() = runTest {
        val annonces = depotDemo().searchListings(null, null).first()

        assertEquals(5, annonces.size)
        assertTrue(annonces.all { it.etat is Disponible })
        assertEquals(listOf(1, 2, 3, 4, 6), annonces.map { it.id }.sorted())
    }

    @Test
    fun `une annonce vendue ne reapparait pas dans la recherche par mot cle`() = runTest {
        val annonces = depotDemo().searchListings("casque", null).first()

        assertTrue(annonces.isEmpty(), "Le casque est vendu, il ne doit plus être visible")
    }

    @Test
    fun `la recherche par mot cle ignore la casse et cherche dans le titre et la description`() =
        runTest {
            val depot = depotDemo()

            assertEquals(listOf(1), depot.searchListings("KOTLIN", null).first().map { it.id })
            assertEquals(listOf(1), depot.searchListings("td de kotlin", null).first().map { it.id })
        }

    @Test
    fun `la recherche par categorie ne renvoie que les annonces disponibles de la categorie`() =
        runTest {
            val annonces = depotDemo().searchListings(null, Categorie.ELECTRONIQUE).first()

            assertEquals(listOf(2), annonces.map { it.id })
        }

    @Test
    fun `mot cle et categorie se combinent`() = runTest {
        val depot = depotDemo()

        assertEquals(listOf(2), depot.searchListings("calculatrice", Categorie.ELECTRONIQUE).first().map { it.id })
        assertTrue(depot.searchListings("calculatrice", Categorie.LIVRES).first().isEmpty())
    }

    @Test
    fun `les annonces sont triees de la plus recente a la plus ancienne`() = runTest {
        val annonces = depotDemo().searchListings(null, null).first()

        assertEquals(annonces.map { it.id }.sortedDescending(), annonces.map { it.id })
    }

    // --- UC1 : publication ----------------------------------------------------

    @Test
    fun `publier une annonce valide renvoie une annonce disponible rattachee au vendeur`() = runTest {
        val depot = depot(EchantillonDonnees.annoncesDemo())
        val avant = depot.allListings().first().size

        val resultat = depot.postListing(
            titre = "  Vélo de ville  ",
            description = "Révisé, freins neufs.",
            prix = 80.0,
            categorie = Categorie.SPORT_LOISIRS,
            auteur = EchantillonDonnees.alice
        )

        val annonce = resultat.getOrThrow()
        assertEquals("Vélo de ville", annonce.titre, "Le titre doit être nettoyé des espaces")
        assertIs<Disponible>(annonce.etat, "Une annonce publiée est disponible")
        assertEquals(EchantillonDonnees.alice.id, annonce.auteur.id)
        assertEquals(avant + 1, depot.allListings().first().size)
        assertNotNull(depot.getListingById(annonce.id))
    }

    @Test
    fun `publier avec un titre vide est refuse`() = runTest {
        val resultat = depot().postListing(
            titre = "   ",
            description = "Sans titre",
            prix = 10.0,
            categorie = Categorie.AUTRE,
            auteur = EchantillonDonnees.alice
        )

        assertTrue(resultat.isFailure)
        val erreur = assertIs<com.swapcampus.domain.ErreurMetierException>(resultat.exceptionOrNull())
        assertEquals(com.swapcampus.domain.ErreurMetier.TITRE_VIDE, erreur.erreur)
    }

    @Test
    fun `publier avec un prix nul ou negatif est refuse`() = runTest {
        val depot = depot()

        listOf(0.0, -5.0).forEach { prix ->
            val resultat = depot.postListing(
                titre = "Objet",
                description = "Description",
                prix = prix,
                categorie = Categorie.AUTRE,
                auteur = EchantillonDonnees.alice
            )
            assertTrue(resultat.isFailure, "Le prix $prix doit être refusé")
            val erreur =
                assertIs<com.swapcampus.domain.ErreurMetierException>(resultat.exceptionOrNull())
            assertEquals(com.swapcampus.domain.ErreurMetier.PRIX_INVALIDE, erreur.erreur)
        }
        assertTrue(depot.allListings().first().isEmpty())
    }

    // --- UC4 : modification ---------------------------------------------------

    @Test
    fun `modifier une annonce met a jour le titre et le prix`() = runTest {
        val depot = depot(EchantillonDonnees.annoncesDemo())

        val resultat = depot.updateListing(id = 1, titre = "Manuel Kotlin 4e édition", prix = 22.0)

        val annonce = assertNotNull(resultat.getOrNull())
        assertEquals("Manuel Kotlin 4e édition", annonce.titre)
        assertEquals(22.0, annonce.prix)
        assertEquals("Manuel Kotlin 4e édition", depot.getListingById(1)?.titre)
    }

    @Test
    fun `modifier une annonce inexistante renvoie null`() = runTest {
        val resultat = depot().updateListing(id = 999, titre = "Fantôme")

        assertTrue(resultat.isSuccess)
        assertNull(resultat.getOrNull(), "Aucune annonce ne correspond à cet identifiant")
    }

    // --- UC5 : changement d'état ---------------------------------------------

    @Test
    fun `le cycle disponible - reservee - vendue est accepte`() = runTest {
        val depot = depot(EchantillonDonnees.annoncesDemo())

        val reservee = depot.changeStatus(1, Reservee("Chloé B.")).getOrThrow()
        assertIs<Reservee>(reservee.etat)
        assertEquals("Chloé B.", (reservee.etat as Reservee).acheteur)

        val vendue = depot.changeStatus(1, Vendue(reservee.dateCreation)).getOrThrow()
        assertIs<Vendue>(vendue.etat)
    }

    @Test
    fun `annuler une reservation remet l annonce en disponible`() = runTest {
        val depot = depot(EchantillonDonnees.annoncesDemo())

        val annonce = depot.changeStatus(5, Disponible).getOrThrow()

        assertIs<Disponible>(annonce.etat)
    }

    @Test
    fun `une transition interdite est refusee`() = runTest {
        val depot = depot(EchantillonDonnees.annoncesDemo())

        // L'annonce 7 est vendue : elle ne peut pas redevenir réservée.
        val resultat = depot.changeStatus(7, Reservee("Quelqu'un"))

        assertTrue(resultat.isFailure)
        assertIs<Vendue>(depot.getListingById(7)?.etat)
    }

    @Test
    fun `changer l etat d une annonce inexistante est refuse`() = runTest {
        val resultat = depot().changeStatus(42, Reservee("Personne"))

        assertTrue(resultat.isFailure)
    }

    // --- UC6 : suppression ---------------------------------------------------

    @Test
    fun `supprimer une annonce la retire du catalogue`() = runTest {
        val depot = depot(EchantillonDonnees.annoncesDemo())

        val resultat = depot.removeListing(1)

        assertTrue(resultat.isSuccess)
        assertNull(depot.getListingById(1))
        assertTrue(depot.allListings().first().none { it.id == 1 })
    }

    @Test
    fun `supprimer une annonce inexistante est refuse`() = runTest {
        assertTrue(depot().removeListing(77).isFailure)
    }

    // --- UC7 : statistiques vendeur ------------------------------------------

    @Test
    fun `les statistiques du vendeur comptent les annonces par etat`() = runTest {
        val stats = depotDemo().sellerStats(EchantillonDonnees.alice.id).first()

        assertEquals(4, stats.publiees)
        assertEquals(2, stats.disponibles)
        assertEquals(1, stats.reservees)
        assertEquals(1, stats.vendues)
        assertEquals(3, stats.enLigne)
    }

    @Test
    fun `un vendeur sans annonce a des statistiques nulles`() = runTest {
        val stats = depotDemo().sellerStats(999).first()

        assertEquals(0, stats.publiees)
        assertEquals(0, stats.enLigne)
    }

    @Test
    fun `les statistiques suivent les changements d etat`() = runTest {
        val depot = depot(EchantillonDonnees.annoncesDemo())

        depot.changeStatus(1, Reservee("Sam"))
        val stats = depot.sellerStats(EchantillonDonnees.alice.id).first()

        assertEquals(1, stats.disponibles)
        assertEquals(2, stats.reservees)
    }
}
