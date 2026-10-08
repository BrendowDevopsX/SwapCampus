package com.isep.composeapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.isep.composeapp.di.AppContainer
import com.isep.composeapp.ui.screens.detail.ListingDetailScreen
import com.isep.composeapp.ui.screens.edit.EditListingScreen
import com.isep.composeapp.ui.screens.listings.ListingsScreen
import com.isep.composeapp.ui.screens.login.LoginScreen
import com.isep.composeapp.ui.screens.mylistings.MyListingsScreen
import com.isep.composeapp.ui.screens.post.PostListingScreen
import com.isep.composeapp.ui.screens.profile.ProfileScreen

@Composable
fun SwapCampusNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val currentUser by AppContainer.authRepository.currentUser.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Routes.LISTINGS,
        modifier = modifier
    ) {
        composable(Routes.LISTINGS) {
            ListingsScreen(
                onListingClick = { id -> navController.navigate(Routes.detail(id)) }
            )
        }

        composable(Routes.MY_LISTINGS) {
            if (currentUser == null) {
                LoginScreen(
                    onLoginSuccess = { navController.popBackStack() }
                )
            } else {
                MyListingsScreen(
                    onListingClick = { id -> navController.navigate(Routes.detail(id)) },
                    onPostClick = { navController.navigate(Routes.POST) }
                )
            }
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onLoginClick = { navController.navigate(Routes.LOGIN) }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { navController.popBackStack() }
            )
        }

        composable(Routes.POST) {
            if (currentUser == null) {
                LoginScreen(
                    onLoginSuccess = { navController.popBackStack() }
                )
            } else {
                PostListingScreen(
                    onSuccess = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("listingId") { type = NavType.IntType })
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getInt("listingId") ?: return@composable
            ListingDetailScreen(
                listingId = listingId,
                onEdit = { id -> navController.navigate(Routes.edit(id)) },
                onDeleted = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.EDIT,
            arguments = listOf(navArgument("listingId") { type = NavType.IntType })
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getInt("listingId") ?: return@composable
            if (currentUser == null) {
                LoginScreen(
                    onLoginSuccess = { navController.popBackStack() }
                )
            } else {
                EditListingScreen(
                    listingId = listingId,
                    onSuccess = { navController.popBackStack() }
                )
            }
        }
    }
}