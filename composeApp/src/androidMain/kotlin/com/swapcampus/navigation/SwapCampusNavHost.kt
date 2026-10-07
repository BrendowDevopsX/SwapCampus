package com.isep.composeapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.isep.composeapp.ui.screens.edit.EditListingScreen
import com.isep.composeapp.ui.screens.listings.ListingsScreen
import com.isep.composeapp.ui.screens.post.PostListingScreen

@Composable
fun SwapCampusNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.LISTINGS
    ) {
        composable(Routes.LISTINGS) {
            ListingsScreen(
                onListingClick = { id ->
                    navController.navigate(Routes.edit(id))
                }
            )
        }

        composable(Routes.POST) {
            PostListingScreen(
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.EDIT,
            arguments = listOf(navArgument("listingId") { type = NavType.IntType })
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getInt("listingId") ?: return@composable
            EditListingScreen(
                listingId = listingId,
                onSuccess = { navController.popBackStack() }
            )
        }
    }
}