package com.isep.composeapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
                onListingClick = { /* detail page in question 7 */ }
            )
        }
        composable(Routes.POST) {
            PostListingScreen(
                onSuccess = { navController.popBackStack() }
            )
        }
    }
}