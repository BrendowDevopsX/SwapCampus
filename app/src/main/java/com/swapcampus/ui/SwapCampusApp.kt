package com.swapcampus.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swapcampus.R
import com.swapcampus.navigation.Routes
import com.swapcampus.navigation.SwapCampusNavHost

private data class BottomItem(
    val route: String,
    @StringRes val libelle: Int,
    val glyphe: String
)

private val bottomItems = listOf(
    BottomItem(Routes.LISTINGS, R.string.nav_annonces, "🏷️"),
    BottomItem(Routes.MY_LISTINGS, R.string.nav_mes_annonces, "📦"),
    BottomItem(Routes.FAVORIS, R.string.nav_favoris, "❤️"),
    BottomItem(Routes.PROFILE, R.string.nav_profil, "👤")
)

/** Routes en plein écran : ni barre de navigation ni bouton flottant. */
private val routesPleinEcran = setOf(
    Routes.DETAIL,
    Routes.EDIT,
    Routes.POST,
    Routes.LOGIN
)

/** Onglets qui possèdent déjà leur propre bouton de publication. */
private val routesSansFab = setOf(Routes.MY_LISTINGS, Routes.FAVORIS)

/** Racine de l'application : coquille (barre de navigation + FAB) et graphe de navigation. */
@Composable
fun SwapCampusApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val pleinEcran = currentRoute != null && currentRoute in routesPleinEcran
    // « Mes annonces » possède déjà son propre bouton de publication dans l'en-tête.
    val afficherFab = !pleinEcran && currentRoute !in routesSansFab

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (!pleinEcran) {
                BottomBar(navController = navController)
            }
        },
        floatingActionButton = {
            if (afficherFab) {
                FloatingActionButton(
                    onClick = { navController.navigate(Routes.POST) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.testTag("fab_publier")
                ) {
                    Text(
                        text = "+",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    ) { padding ->
        SwapCampusNavHost(
            navController = navController,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
private fun BottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        bottomItems.forEach { item ->
            val selectionne = currentDestination?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                selected = selectionne,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Text(
                        text = item.glyphe,
                        fontSize = 17.sp
                    )
                },
                label = { Text(stringResource(item.libelle)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("onglet_" + item.route)
            )
        }
    }
}
