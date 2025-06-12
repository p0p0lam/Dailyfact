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
import com.google.firebase.FirebaseApp
import java.util.concurrent.TimeUnit
import com.popolam.app.dailyfact.worker.DailyFactWorker
import timber.log.Timber
import java.util.Calendar

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        startKoin {
            androidLogger(if(BuildConfig.DEBUG) Level.DEBUG else Level.NONE) // Use Level.INFO or Level.NONE in release
            androidContext(this@App)
            modules(listOf(appModule, viewModelModule, networkModule, databaseModule))
        }

        setupDailyWorker()
    }

    private fun setupDailyWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Only run when connected
            .build()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= Calendar.getInstance().timeInMillis) add(Calendar.DAY_OF_MONTH, 1)
        }
        val initialDelay = calendar.timeInMillis - Calendar.getInstance().timeInMillis
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyFactWorker>(1L, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "DailyFactFetchWork",
            ExistingPeriodicWorkPolicy.UPDATE, // Or REPLACE if you update the worker logic
            dailyWorkRequest
        )
    }
}