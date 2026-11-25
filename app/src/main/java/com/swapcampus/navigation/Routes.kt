package com.swapcampus.navigation

/**
 * Destination des onglets et des écrans de l'application.
 *
 * Les onglets sont des routes sans argument ; les écrans de détail portent
 * l'identifiant de l'annonce et sont construits par [detail] et [edit].
 */
object Routes {
    const val LISTINGS = "listings"
    const val MY_LISTINGS = "my_listings"
    const val FAVORIS = "favoris"
    const val PROFILE = "profile"
    const val LOGIN = "login"
    const val POST = "post"
    const val DETAIL = "detail/{listingId}"
    const val EDIT = "edit/{listingId}"

    fun detail(listingId: Int) = "detail/$listingId"
    fun edit(listingId: Int) = "edit/$listingId"
}
