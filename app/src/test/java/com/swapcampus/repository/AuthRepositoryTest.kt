package com.swapcampus.repository

import com.swapcampus.data.EchantillonDonnees
import com.swapcampus.domain.RoleUtilisateur
import kotlinx.coroutines.flow.first
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/** Tests de l'authentification simplifiée en mémoire (cahier des charges §8.2). */
class AuthRepositoryTest {

    @Test
    fun `trois comptes de demonstration sont disponibles`() {
        val depot = AuthRepositoryImpl()

        assertEquals(3, depot.comptesDisponibles.size)
        assertEquals(
            listOf("alice@etu.fr", "bob@etu.fr", "admin@swapcampus.fr"),
            depot.comptesDisponibles.map { it.email }
        )
        assertNotNull(depot.comptesDisponibles.firstOrNull { it.role == RoleUtilisateur.ADMINISTRATEUR })
    }

    @Test
    fun `connexion avec les bons identifiants expose l utilisateur courant`() {
        val depot = AuthRepositoryImpl()

        val compte = depot.authenticate("alice@etu.fr", "alice123")

        assertEquals(EchantillonDonnees.alice.id, compte?.id)
        assertEquals("Alice Martin", depot.currentUser.value?.nom)
    }

    @Test
    fun `l email est insensible a la casse et aux espaces`() {
        val depot = AuthRepositoryImpl()

        assertNotNull(depot.authenticate("  ALICE@ETU.FR  ", "alice123"))
    }

    @Test
    fun `un mot de passe errone ne connecte personne`() {
        val depot = AuthRepositoryImpl()

        assertNull(depot.authenticate("alice@etu.fr", "mauvais"))
        assertNull(depot.currentUser.value)
    }

    @Test
    fun `un email inconnu ne connecte personne`() {
        val depot = AuthRepositoryImpl()

        assertNull(depot.authenticate("inconnu@etu.fr", "alice123"))
    }

    @Test
    fun `un compte desactive ne peut pas se connecter`() {
        val desactive = EchantillonDonnees.bob.copy(actif = false)
        val depot = AuthRepositoryImpl(listOf(desactive))

        assertNull(depot.authenticate("bob@etu.fr", "bob123"))
    }

    @Test
    fun `la deconnexion vide l utilisateur courant`() {
        val depot = AuthRepositoryImpl()
        depot.authenticate("admin@swapcampus.fr", "admin123")
        assertNotNull(depot.currentUser.value)

        depot.logout()

        assertNull(depot.currentUser.value)
    }

    @Test
    fun `l etat de connexion est observable`() {
        kotlinx.coroutines.runBlocking {
            val depot = AuthRepositoryImpl()
            assertEquals(null, depot.currentUser.first())

            depot.authenticate("bob@etu.fr", "bob123")

            assertEquals(EchantillonDonnees.bob.id, depot.currentUser.first()?.id)
        }
    }
}
