package com.popolam.app.dailyfact.data.repository

import com.popolam.app.dailyfact.data.model.Fact
import kotlinx.coroutines.flow.Flow

interface FactRepository {
    fun getDailyFact(): Flow<Fact?>
    suspend fun fetchAndStoreNewFact(): Result<Unit> // For worker
    suspend fun forceRefreshFact(): Result<Unit> // For manual refresh
}