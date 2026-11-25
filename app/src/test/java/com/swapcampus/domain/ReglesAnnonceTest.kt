package com.swapcampus.domain

import com.swapcampus.data.EchantillonDonnees
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Règles métier de l'annonce, testées hors de toute UI (cahier des charges
 * §10.5 : la validation vit dans le domaine).
 */
class ReglesAnnonceTest {

    private val alice = EchantillonDonnees.alice

    // --- Validation d'une publication (UC1) -----------------------------------

    @Test
    fun `un titre vide ou blanc est refuse`() {
        assertEquals(
            ErreurMetier.TITRE_VIDE,
            ReglesAnnonce.validerAnnonce("   ", 10.0)
        )
        assertEquals(
            ErreurMetier.TITRE_VIDE,
            ReglesAnnonce.validerAnnonce("", 10.0)
        )
    }

    @Test
    fun `un champ non fourni n'est pas verifie`() {
        // `null` signifie « champ inchangé » lors d'une modification (UC4).
        assertNull(ReglesAnnonce.validerAnnonce(null, null, null))
    }

    @Test
    fun `un titre de plus de 80 caracteres est refuse`() {
        val titre = "a".repeat(ReglesAnnonce.TITRE_MAX + 1)

        assertEquals(
            ErreurMetier.TITRE_TROP_LONG,
            ReglesAnnonce.validerAnnonce(titre, 10.0)
        )
        assertNull(
            ReglesAnnonce.validerAnnonce("a".repeat(ReglesAnnonce.TITRE_MAX), 10.0)
        )
    }

    @Test
    fun `une description de plus de 1000 caracteres est refusee`() {
        val description = "a".repeat(ReglesAnnonce.DESCRIPTION_MAX + 1)

        assertEquals(
            ErreurMetier.DESCRIPTION_TROP_LONGUE,
            ReglesAnnonce.validerAnnonce("Vélo", 10.0, description)
        )
    }

    @Test
    fun `un prix nul ou negatif est refuse`() {
        assertEquals(ErreurMetier.PRIX_INVALIDE, ReglesAnnonce.validerAnnonce("Vélo", 0.0))
        assertEquals(ErreurMetier.PRIX_INVALIDE, ReglesAnnonce.validerAnnonce("Vélo", -3.0))
    }

    @Test
    fun `une annonce valide ne remonte aucune erreur`() {
        assertNull(ReglesAnnonce.validerAnnonce("Vélo de ville", 45.5, "Bon état"))
    }

    // --- Transitions d'état (UC5) --------------------------------------------

    @Test
    fun `disponible ne peut devenir que reservee`() {
        assertTrue(ReglesAnnonce.transitionAutorisee(Disponible, Reservee("Chloé")))
        assertFalse(ReglesAnnonce.transitionAutorisee(Disponible, Vendue(LocalDate(2026, 1, 2))))
        assertFalse(ReglesAnnonce.transitionAutorisee(Disponible, Disponible))
    }

    @Test
    fun `reservee peut devenir vendue ou revenir a disponible`() {
        val reservee = Reservee("Chloé")

        assertTrue(ReglesAnnonce.transitionAutorisee(reservee, Vendue(LocalDate(2026, 1, 2))))
        assertTrue(ReglesAnnonce.transitionAutorisee(reservee, Disponible))
        assertFalse(ReglesAnnonce.transitionAutorisee(reservee, Reservee("Bob")))
    }

    @Test
    fun `vendue ne peut que revenir a disponible`() {
        val vendue = Vendue(LocalDate(2026, 1, 2))

        assertTrue(ReglesAnnonce.transitionAutorisee(vendue, Disponible))
        assertFalse(ReglesAnnonce.transitionAutorisee(vendue, Reservee("Chloé")))
        assertFalse(ReglesAnnonce.transitionAutorisee(vendue, vendue))
    }

    // --- Réservation et modération -------------------------------------------

    @Test
    fun `une reservation exige le nom de l'acheteur`() {
        assertEquals(ErreurMetier.ACHETEUR_OBLIGATOIRE, ReglesAnnonce.validerReservation(null))
        assertEquals(ErreurMetier.ACHETEUR_OBLIGATOIRE, ReglesAnnonce.validerReservation("  "))
        assertNull(ReglesAnnonce.validerReservation("Chloé B."))
    }

    @Test
    fun `une suppression administrative exige un motif`() {
        assertEquals(ErreurMetier.MOTIF_OBLIGATOIRE, ReglesAnnonce.validerModeration(null))
        assertEquals(ErreurMetier.MOTIF_OBLIGATOIRE, ReglesAnnonce.validerModeration(" "))
        assertNull(ReglesAnnonce.validerModeration("Vendeur injoignable"))
    }

    @Test
    fun `un motif de plus de 200 caracteres est refuse`() {
        assertEquals(
            ErreurMetier.MOTIF_TROP_LONG,
            ReglesAnnonce.validerModeration("a".repeat(ReglesAnnonce.MOTIF_MAX + 1))
        )
    }

    @Test
    fun `une erreur porte un message et une exception`() {
        assertEquals("Titre vide", ErreurMetier.TITRE_VIDE.message)
        assertEquals(
            ErreurMetier.TITRE_VIDE,
            ErreurMetier.TITRE_VIDE.exception().erreur
        )
    }

    @Test
    fun `un compte de demonstration existe`() {
        assertEquals(1, alice.id)
    }
}
