package com.isep.composeapp.navigation

object Routes {
    const val LISTINGS = "listings"
    const val POST = "post"
    const val EDIT = "edit/{listingId}"

    fun edit(listingId: Int) = "edit/$listingId"
}