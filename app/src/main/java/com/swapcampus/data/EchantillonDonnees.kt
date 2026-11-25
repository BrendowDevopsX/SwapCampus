package com.swapcampus.data

import com.swapcampus.domain.Annonce
import com.swapcampus.domain.Categorie
import com.swapcampus.domain.CompteUtilisateur
import com.swapcampus.domain.EtatAnnonce
import com.swapcampus.domain.Reservee
import com.swapcampus.domain.RoleUtilisateur
import com.swapcampus.domain.Vendue
import com.swapcampus.domain.aujourdHui
import kotlinx.datetime.LocalDate

/**
 * Jeu de données de démonstration : trois comptes et huit annonces couvrant
 * les trois états (disponible, réservée, vendue) et plusieurs catégories.
 *
 * Objectif : une démonstration ne commence pas sur un écran vide, et les
 * règles métier (filtre des annonces vendues, statistiques vendeur) sont
 * visibles immédiatement. Ce n'est **pas** de la persistance : tout vit en
 * mémoire et repart à zéro au prochain lancement (cahier des charges §8.3).
 */
object EchantillonDonnees {

    val alice = CompteUtilisateur(
        id = 1,
        nom = "Alice Martin",
        email = "alice@etu.fr",
        motDePasseHash = "alice123",
        dateCreation = LocalDate(2025, 9, 1),
        role = RoleUtilisateur.ETUDIANT
    )

    val bob = CompteUtilisateur(
        id = 2,
        nom = "Bob Diallo",
        email = "bob@etu.fr",
        motDePasseHash = "bob123",
        dateCreation = LocalDate(2025, 9, 1),
        role = RoleUtilisateur.ETUDIANT
    )

    val admin = CompteUtilisateur(
        id = 3,
        nom = "Admin ISEP",
        email = "admin@swapcampus.fr",
        motDePasseHash = "admin123",
        dateCreation = LocalDate(2025, 9, 1),
        role = RoleUtilisateur.ADMINISTRATEUR
    )

    /** Comptes de démonstration, partagés par l'authentification et les annonces. */
    val comptes: List<CompteUtilisateur> = listOf(alice, bob, admin)

    /** Annonces de démonstration, rattachées aux comptes ci-dessus. */
    fun annoncesDemo(): List<Annonce> {
        val jour = aujourdHui()
        return listOf(
            annonce(
                id = 1,
                titre = "Manuel de Kotlin, 3e édition",
                description = "Livre en très bon état, aucune annotation. Idéal pour les TD de Kotlin du S3.",
                prix = 18.50,
                categorie = Categorie.LIVRES,
                auteur = alice
            ),
            annonce(
                id = 2,
                titre = "Calculatrice graphique TI-84",
                description = "Fonctionne parfaitement, piles neuves fournies. Autorisée en examen.",
                prix = 45.00,
                categorie = Categorie.ELECTRONIQUE,
                auteur = bob
            ),
            annonce(
                id = 3,
                titre = "Cours particuliers de maths (L1)",
                description = "Étudiant en 4e année, 2 h par semaine, remise à niveau algèbre et analyse.",
                prix = 25.00,
                categorie = Categorie.COURS,
                auteur = alice
            ),
            annonce(
                id = 4,
                titre = "Raquette de tennis Wilson",
                description = "Tamis 100, cordage récent, vendue avec sa housse.",
                prix = 35.00,
                categorie = Categorie.SPORT_LOISIRS,
                auteur = bob
            ),
            annonce(
                id = 5,
                titre = "Chaise de bureau ergonomique",
                description = "Réglable en hauteur, accoudoirs, très confortable pour les révisions.",
                prix = 60.00,
                categorie = Categorie.MOBILIER,
                auteur = alice,
                etat = Reservee(acheteur = "Chloé B.")
            ),
            annonce(
                id = 6,
                titre = "Veste ISEP, taille M",
                description = "Portée deux fois, lavée. Coupe mixte, logo brodé.",
                prix = 20.00,
                categorie = Categorie.VETEMENTS,
                auteur = bob
            ),
            annonce(
                id = 7,
                titre = "Casque Bluetooth Sony",
                description = "Réduction de bruit, autonomie 30 h, câble de charge inclus.",
                prix = 30.00,
                categorie = Categorie.ELECTRONIQUE,
                auteur = alice,
                etat = Vendue(dateVente = jour)
            ),
            annonce(
                id = 8,
                titre = "Lot de 5 romans de science-fiction",
                description = "Dune, Fondation, Hyperion et deux autres classiques. À emporter en bloc.",
                prix = 12.00,
                categorie = Categorie.LIVRES,
                auteur = bob,
                etat = Vendue(dateVente = jour)
            )
        )
    }

    private fun annonce(
        id: Int,
        titre: String,
        description: String,
        prix: Double,
        categorie: Categorie,
        auteur: CompteUtilisateur,
        etat: EtatAnnonce = com.swapcampus.domain.Disponible
    ) = Annonce(
        id = id,
        titre = titre,
        description = description,
        prix = prix,
        categorie = categorie,
        photoUrl = null,
        auteur = auteur,
        etat = etat,
        dateCreation = aujourdHui()
    )
}
