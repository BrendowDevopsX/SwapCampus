package com.isep.composeapp.navigation

object Routes {
    const val LISTINGS = "listings"
    const val POST = "post"
    const val DETAIL = "detail/{listingId}"
    const val EDIT = "edit/{listingId}"

    fun detail(listingId: Int) = "detail/$listingId"
    fun edit(listingId: Int) = "edit/$listingId"
}