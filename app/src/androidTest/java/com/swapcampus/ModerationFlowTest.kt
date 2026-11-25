package com.swapcampus

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

/**
 * Parcours de modération (P1) : un administrateur supprime une annonce avec un
 * motif obligatoire, l'annonce disparaît du catalogue et l'action est
 * journalisée dans son profil.
 */
class ModerationFlowTest {

    @get:Rule
    val regle = createAndroidComposeRule<MainActivity>()

    private val titreSupprime = "Veste ISEP, taille M"
    private val motif = "Annonce contraire au règlement"

    @Test
    fun un_administrateur_supprime_une_annonce_avec_un_motif() {
        regle.repartirDuCatalogueDeconnecte()

        // 1. Connexion avec le compte administrateur, puis retour au catalogue.
        regle.seConnecterAvec(compteId = 3)
        regle.runOnUiThread { regle.activity.onBackPressedDispatcher.onBackPressed() }
        regle.attendreTag("champ_recherche")

        // 2. Ouverture de l'annonce à modérer (n°6, « Veste ISEP »).
        regle.onNodeWithTag("liste_annonces").performScrollToNode(hasTestTag("annonce_6"))
        regle.onNodeWithTag("annonce_6").performClick()
        regle.attendreTag("bouton_moderer")

        // 3. Suppression avec motif obligatoire.
        regle.onNodeWithTag("bouton_moderer").performScrollTo().performClick()
        regle.attendreTag("champ_motif")
        regle.onNodeWithTag("champ_motif").performTextInput(motif)
        regle.onNodeWithTag("bouton_confirmer_moderation").performClick()

        // 4. L'annonce n'est plus dans le catalogue.
        regle.attendreTag("champ_recherche")
        regle.waitUntil(timeoutMillis = 10_000) {
            regle.onAllNodesWithText(titreSupprime).fetchSemanticsNodes().isEmpty()
        }

        // 5. La suppression est journalisée dans le profil de l'administrateur.
        regle.onNodeWithTag("onglet_profile").performClick()
        regle.attendreTag("bouton_deconnexion")
        regle.onNodeWithText(motif, substring = true).performScrollTo().assertIsDisplayed()
    }
}
