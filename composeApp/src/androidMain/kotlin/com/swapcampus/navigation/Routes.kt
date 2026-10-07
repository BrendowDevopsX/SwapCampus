package com.isep.composeapp.navigation

object Routes {
    const val LISTINGS = "listings"
    const val MY_LISTINGS = "my_listings"
    const val PROFILE = "profile"
    const val LOGIN = "login"
    const val POST = "post"
    const val DETAIL = "detail/{listingId}"
    const val EDIT = "edit/{listingId}"

    fun detail(listingId: Int) = "detail/$listingId"
    fun edit(listingId: Int) = "edit/$listingId"
}