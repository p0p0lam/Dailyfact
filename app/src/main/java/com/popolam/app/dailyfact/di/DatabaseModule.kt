package com.popolam.app.dailyfact.di

import androidx.room.Room
import com.popolam.app.dailyfact.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java, "daily_fact_db"
        ).fallbackToDestructiveMigration(false) // For simplicity, handle migrations properly in prod
            .build()
    }
    single { get<AppDatabase>().factDao() }
}