package com.swapcampus

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo

/**
 * Aides partagées par les tests d'interface.
 *
 * L'application conserve son état en mémoire (cahier des charges §8.3) : la
 * session, les favoris et les annonces publiées survivent donc d'un test à
 * l'autre dans le même processus. Chaque scénario repart explicitement du
 * catalogue déconnecté pour rester indépendant de l'ordre d'exécution.
 */

/** Attend l'apparition d'un nœud portant ce testTag. */
internal fun ComposeContentTestRule.attendreTag(tag: String, timeoutMillis: Long = 10_000) {
    waitUntil(timeoutMillis) { onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
}

/** Indique si un nœud portant ce testTag est présent à l'écran. */
internal fun ComposeContentTestRule.contientTag(tag: String): Boolean =
    onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()

/**
 * Déconnecte l'utilisateur courant s'il y en a un, puis revient sur l'onglet
 * des annonces : point de départ commun à tous les scénarios.
 */
internal fun ComposeContentTestRule.repartirDuCatalogueDeconnecte() {
    attendreTag("onglet_profile")
    onNodeWithTag("onglet_profile").performClick()

    val connecte = runCatching {
        waitUntil(2_000) { onAllNodesWithTag("bouton_deconnexion").fetchSemanticsNodes().isNotEmpty() }
        true
    }.getOrDefault(false)

    if (connecte) {
        onNodeWithTag("bouton_deconnexion").performScrollTo().performClick()
        waitUntil(10_000) { onAllNodesWithTag("bouton_deconnexion").fetchSemanticsNodes().isEmpty() }
    }

    onNodeWithTag("onglet_listings").performClick()
    attendreTag("champ_recherche")
}

/**
 * Connexion par le bouton flottant, puis par la carte du compte de démonstration
 * demandé. Laisse l'application sur le formulaire de publication.
 */
internal fun ComposeContentTestRule.seConnecterAvec(compteId: Int) {
    onNodeWithTag("fab_publier").performClick()
    attendreTag("bouton_connexion")
    onNodeWithTag("compte_demo_$compteId").performScrollTo().performClick()
    onNodeWithTag("bouton_connexion").performScrollTo().performClick()
    attendreTag("champ_titre")
}
