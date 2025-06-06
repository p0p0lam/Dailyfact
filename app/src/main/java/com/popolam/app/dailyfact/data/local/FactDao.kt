package com.popolam.app.dailyfact.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FactDao {
    @Query("SELECT * FROM facts ORDER BY dateFetched DESC LIMIT 1")
    fun getLatestFact(): Flow<FactEntity?> // Observe changes

    @Query("SELECT * FROM facts WHERE id = :id")
    suspend fun getFactById(id: String): FactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFact(fact: FactEntity)

    @Query("DELETE FROM facts WHERE dateFetched < :thresholdDate")
    suspend fun clearOldFacts(thresholdDate: Long)
}

