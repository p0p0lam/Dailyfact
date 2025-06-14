package com.popolam.app.dailyfact.data.repository

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.popolam.app.dailyfact.data.Installation
import com.popolam.app.dailyfact.data.KeyNotFoundException
import com.popolam.app.dailyfact.data.local.FactDao
import com.popolam.app.dailyfact.data.local.toDomain
import com.popolam.app.dailyfact.data.local.toEntity
import com.popolam.app.dailyfact.data.model.Fact
import com.popolam.app.dailyfact.data.remote.FactApiService
import com.popolam.app.dailyfact.data.remote.toDomainFact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.util.Calendar
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FactRepositoryImpl(
    private val factDao: FactDao,
    private val factApiService: FactApiService,
    private val installation: Installation,
    private val pushRepository: PushRepository
) : FactRepository {
    override fun getDailyFact(): Flow<Fact?> {
        // This flow will emit null if no fact is found initially,
        // then emit the fact once it's fetched and stored.
        return factDao.getLatestFact().map { entity ->
            entity?.toDomain()
        }
    }

    // Called by Worker or manual refresh
    override suspend fun fetchAndStoreNewFact(): Result<Unit> {
        return try {
            val apiResponse =try {
                factApiService.generateRandomFact(installation.id())
            } catch (e:KeyNotFoundException){
                updatePushTokenAndKey()
                // force to receive new key and try second time
                factApiService.generateRandomFact(installation.id())
            }
            val todayDateString =
                getTodayDateString() // Helper to create a consistent ID for the day
            val newFact = apiResponse?.toDomainFact(
                id = todayDateString,
                timestamp = System.currentTimeMillis()
            )
            newFact?.let {
                factDao.insertFact(it.toEntity())
            }

            Result.success(Unit)
        } catch (e: Exception) {
            // Log error
            Result.failure(e)
        }
    }



    override suspend fun forceRefreshFact(): Result<Unit> {
        return fetchAndStoreNewFact() // Simple for now
    }

    // Helper to generate a consistent ID for "today's" fact
    private fun getTodayDateString(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }

    private suspend fun updatePushTokenAndKey(){
        val token =suspendCoroutine<String> {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Timber.w("Fetching FCM registration token failed", task.exception)
                    it.resumeWith(
                        Result.failure(
                            task.exception ?: Exception("Unknown error")
                        )
                    )
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                it.resume(token)
            })
        }
        pushRepository.processPushToken(token)
    }
}