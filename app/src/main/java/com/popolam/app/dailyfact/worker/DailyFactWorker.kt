package com.popolam.app.dailyfact.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.popolam.app.dailyfact.data.repository.FactRepository
import org.koin.core.component.KoinComponent // For Koin DI in Worker
import org.koin.core.component.inject

class DailyFactWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent { // Implement KoinComponent

    // Inject repository using Koin
    private val factRepository: FactRepository by inject()

    override suspend fun doWork(): Result {
        return try {
            val fetchResult = factRepository.fetchAndStoreNewFact()
            if (fetchResult.isSuccess) {
                Result.success()
            } else {
                Result.retry() // Or Result.failure() if it's a non-recoverable error
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}