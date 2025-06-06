package com.popolam.app.dailyfact

import android.app.Application
import com.popolam.app.dailyfact.di.appModule
import com.popolam.app.dailyfact.di.databaseModule
import com.popolam.app.dailyfact.di.networkModule
import com.popolam.app.dailyfact.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level
import androidx.work.*
import java.util.concurrent.TimeUnit
import com.popolam.app.dailyfact.worker.DailyFactWorker

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG) // Use Level.INFO or Level.NONE in release
            androidContext(this@App)
            modules(listOf(appModule, viewModelModule, networkModule, databaseModule))
        }

        setupDailyWorker()
    }

    private fun setupDailyWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Only run when connected
            .build()

        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyFactWorker>(1L, TimeUnit.DAYS)
            .setConstraints(constraints)
            // .setInitialDelay(10, TimeUnit.SECONDS) // For testing
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "DailyFactFetchWork",
            ExistingPeriodicWorkPolicy.KEEP, // Or REPLACE if you update the worker logic
            dailyWorkRequest
        )
    }
}