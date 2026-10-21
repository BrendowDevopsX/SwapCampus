package com.swapcampus.di

import com.swapcampus.repository.AuthRepository
import com.swapcampus.repository.AuthRepositoryImpl
import com.swapcampus.repository.ListingRepository
import com.swapcampus.repository.ListingRepositoryImpl

object AppContainer {
    val listingRepository: ListingRepository by lazy { ListingRepositoryImpl() }
    val authRepository: AuthRepository by lazy { AuthRepositoryImpl() }
}