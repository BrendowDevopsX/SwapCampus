package com.isep.composeapp.di

import com.isep.shared.repository.AuthRepository
import com.isep.shared.repository.AuthRepositoryImpl
import com.isep.shared.repository.ListingRepository
import com.isep.shared.repository.ListingRepositoryImpl

object AppContainer {
    val listingRepository: ListingRepository by lazy { ListingRepositoryImpl() }
    val authRepository: AuthRepository by lazy { AuthRepositoryImpl() }
}