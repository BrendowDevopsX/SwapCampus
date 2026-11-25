package com.swapcampus

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import org.junit.Rule
import org.junit.Test

/**
 * Parcours des favoris (P1) : une annonce mise en favori depuis le catalogue
 * se retrouve dans l'onglet « Favoris ».
 */
class FavorisFlowTest {

    @get:Rule
    val regle = createAndroidComposeRule<MainActivity>()

    private val titre = "Manuel de Kotlin, 3e édition"

    @Test
    fun une_annonce_mise_en_favori_apparait_dans_longlet_favoris() {
        regle.repartirDuCatalogueDeconnecte()

        // 1. Connexion (les favoris sont liés à un compte).
        regle.seConnecterAvec(compteId = 1)
        regle.runOnUiThread { regle.activity.onBackPressedDispatcher.onBackPressed() }
        regle.attendreTag("champ_recherche")

        // 2. On part d'un état sans favori pour l'annonce n°1.
        regle.onNodeWithTag("onglet_favoris").performClick()
        regle.waitForIdle()
        if (regle.contientTag("favori_1")) {
            regle.onNodeWithTag("favori_1").performClick()
            regle.waitForIdle()
        }
        regle.onNodeWithTag("onglet_listings").performClick()
        regle.attendreTag("champ_recherche")

        // 3. L'annonce n°1 est mise en favori depuis le catalogue.
        regle.onNodeWithTag("liste_annonces").performScrollToNode(hasTestTag("favori_1"))
        regle.onNodeWithTag("favori_1").performClick()

        // 4. Elle apparaît dans l'onglet Favoris.
        regle.onNodeWithTag("onglet_favoris").performClick()
        regle.attendreTag("favori_1")
        regle.onNodeWithText(titre).performScrollTo().assertIsDisplayed()
    }
}
