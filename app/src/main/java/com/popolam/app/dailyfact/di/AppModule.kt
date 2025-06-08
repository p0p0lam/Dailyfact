package com.popolam.app.dailyfact.di


import com.popolam.app.dailyfact.data.repository.FactRepository
import com.popolam.app.dailyfact.data.repository.FactRepositoryImpl
import com.popolam.app.dailyfact.domain.GetDailyFactUseCase

import org.koin.dsl.module

val appModule = module {
    single<FactRepository> { FactRepositoryImpl(get(), get()) }
    factory { GetDailyFactUseCase(get()) }

}