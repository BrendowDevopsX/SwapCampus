package com.swapcampus

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

/**
 * Test d'interface du parcours de publication d'une annonce (UC1 + UC2).
 *
 * Vérifie de bout en bout : le bouton flottant mène à la connexion quand on
 * n'est pas connecté, le compte de démonstration permet de se connecter, le
 * formulaire publie bien l'annonce, et celle-ci apparaît dans le catalogue.
 */
class PostListingFlowTest {

    @get:Rule
    val regle = createAndroidComposeRule<MainActivity>()

    @Test
    fun publication_dune_annonce_depuis_le_catalogue() {
        regle.repartirDuCatalogueDeconnecte()

        // 1. Le catalogue de démonstration est visible.
        regle.onNodeWithText("SwapCampus").assertIsDisplayed()

        // 2. Connexion avec le compte de démonstration d'Alice.
        regle.seConnecterAvec(compteId = 1)

        // 3. Le formulaire de publication remplace l'écran de connexion.
        val titre = "Rayon de soleil E2E"
        regle.onNodeWithTag("champ_titre").performScrollTo().performTextInput(titre)
        regle.onNodeWithTag("champ_description").performScrollTo()
            .performTextInput("Accessoire de plage, très peu servi.")
        regle.onNodeWithTag("champ_prix").performScrollTo().performTextInput("7,50")
        regle.onNodeWithTag("cat_AUTRE").performScrollTo().performClick()

        // 4. Publication.
        regle.onNodeWithTag("bouton_publier_annonce").performScrollTo().performClick()

        // 5. Retour au catalogue : la nouvelle annonce y figure en tête.
        regle.waitUntil(timeoutMillis = 10_000) {
            regle.onAllNodesWithText(titre).fetchSemanticsNodes().isNotEmpty()
        }
        regle.onNodeWithText(titre).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun une_annonce_vendue_ne_figure_pas_dans_le_catalogue() {
        regle.repartirDuCatalogueDeconnecte()

        // Titre d'une annonce vendue du jeu de démonstration.
        assert(
            regle.onAllNodesWithText("Casque Bluetooth Sony").fetchSemanticsNodes().isEmpty()
        ) { "Une annonce vendue ne doit pas apparaître dans le catalogue (UC2)" }

        // Une annonce disponible, elle, est bien listée.
        regle.onNodeWithText("Manuel de Kotlin, 3e édition").performScrollTo().assertIsDisplayed()
    }
}
