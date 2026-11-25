package com.swapcampus.repository

import com.swapcampus.data.EchantillonDonnees
import com.swapcampus.domain.CatalogueAnnonces
import com.swapcampus.domain.Disponible
import com.swapcampus.domain.ErreurMetier
import com.swapcampus.domain.ErreurMetierException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Suppression administrative motivée (UC6, §3.2), journal de modération et
 * republication d'une annonce vendue (UC4).
 */
class ModerationTest {

    private fun depot() =
        ListingRepositoryImpl(CatalogueAnnonces(EchantillonDonnees.annoncesDemo()))

    private fun erreur(resultat: Result<*>): ErreurMetier =
        (resultat.exceptionOrNull() as ErreurMetierException).erreur

    // --- Suppression par un administrateur -----------------------------------

    @Test
    fun `un administrateur supprime une annonce avec un motif et l'action est journalisee`() =
        runTest {
            val depot = depot()
            val admin = EchantillonDonnees.admin

            val resultat = depot.removeListingAsAdmin(1, admin, "Contenu interdit")

            assertTrue(resultat.isSuccess)
            assertNull(depot.getListingById(1))

            val journal = depot.moderationActions().first()
            assertEquals(1, journal.size)
            assertEquals(1, journal.first().annonceId)
            assertEquals("Manuel de Kotlin, 3e édition", journal.first().annonceTitre)
            assertEquals("Contenu interdit", journal.first().motif)
            assertEquals(admin.id, journal.first().moderateur.id)
        }

    @Test
    fun `une suppression administrative sans motif est refusee`() = runTest {
        val depot = depot()

        val resultat = depot.removeListingAsAdmin(1, EchantillonDonnees.admin, "   ")

        assertEquals(ErreurMetier.MOTIF_OBLIGATOIRE, erreur(resultat))
        assertNotNull(depot.getListingById(1))
        assertTrue(depot.moderationActions().first().isEmpty())
    }

    @Test
    fun `un etudiant ne peut pas supprimer l'annnonce d'un autre`() = runTest {
        val depot = depot()

        val resultat = depot.removeListingAsAdmin(1, EchantillonDonnees.bob, "Je préfère")

        assertEquals(ErreurMetier.NON_ADMINISTRATEUR, erreur(resultat))
        assertNotNull(depot.getListingById(1))
    }

    @Test
    fun `supprimer une annonce inconnue echoue sans rien journaliser`() = runTest {
        val depot = depot()

        val resultat = depot.removeListingAsAdmin(999, EchantillonDonnees.admin, "Introuvable")

        assertTrue(resultat.isFailure)
        assertTrue(depot.moderationActions().first().isEmpty())
    }

    // --- Republication (UC4) --------------------------------------------------

    @Test
    fun `le proprietaire republie une annonce vendue sous une nouvelle identite`() = runTest {
        val depot = depot()
        val alice = EchantillonDonnees.alice
        val vendue = assertNotNull(depot.getListingById(7))
        assertIs<com.swapcampus.domain.Vendue>(vendue.etat)

        val resultat = depot.republishListing(7, alice)

        val copie = assertNotNull(resultat.getOrNull())
        assertEquals(9, copie.id)
        assertEquals(vendue.titre, copie.titre)
        assertEquals(vendue.prix, copie.prix)
        assertEquals(alice.id, copie.auteur.id)
        assertIs<Disponible>(copie.etat)
        assertNotNull(depot.getListingById(9))
        // L'annonce d'origine reste vendue : on ne réécrit pas l'historique.
        assertIs<com.swapcampus.domain.Vendue>(depot.getListingById(7)?.etat)
    }

    @Test
    fun `republier l'annonce d'un autre est refuse`() = runTest {
        val depot = depot()

        val resultat = depot.republishListing(7, EchantillonDonnees.bob)

        assertEquals(ErreurMetier.NON_PROPRIETAIRE, erreur(resultat))
        assertNull(depot.getListingById(9))
    }

    @Test
    fun `republier une annonce inconnue echoue`() = runTest {
        val depot = depot()

        val resultat = depot.republishListing(999, EchantillonDonnees.alice)

        assertEquals(ErreurMetier.ANNONCE_INTROUVABLE, erreur(resultat))
    }
}
