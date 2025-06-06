package com.popolam.app.dailyfact.di

import androidx.room.Room
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.popolam.app.dailyfact.data.repository.FactRepository
import com.popolam.app.dailyfact.data.repository.FactRepositoryImpl
import com.popolam.app.dailyfact.domain.GetDailyFactUseCase

import org.koin.dsl.module

val appModule = module {
    single<FactRepository> { FactRepositoryImpl(get(), get()) }
    factory { GetDailyFactUseCase(get()) }
    single {
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig
    }
    // Add other app-wide singletons if any
}