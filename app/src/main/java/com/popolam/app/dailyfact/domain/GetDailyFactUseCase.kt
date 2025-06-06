package com.popolam.app.dailyfact.domain

import com.popolam.app.dailyfact.data.model.Fact
import com.popolam.app.dailyfact.data.repository.FactRepository
import kotlinx.coroutines.flow.Flow

class GetDailyFactUseCase(private val repository: FactRepository) {
    operator fun invoke(): Flow<Fact?> = repository.getDailyFact()
}