package com.popolam.app.dailyfact.di


import com.popolam.app.dailyfact.data.Installation
import com.popolam.app.dailyfact.data.repository.FactRepository
import com.popolam.app.dailyfact.data.repository.FactRepositoryImpl
import com.popolam.app.dailyfact.data.repository.PushRepository
import com.popolam.app.dailyfact.data.repository.PushRepositoryImpl
import com.popolam.app.dailyfact.domain.GetDailyFactUseCase

import org.koin.dsl.module

val appModule = module {
    single<FactRepository> { FactRepositoryImpl(get(), get(), get(), get()) }
    single<PushRepository> { PushRepositoryImpl(get(), get()) }
    factory { GetDailyFactUseCase(get()) }
    single { Installation(get()) }
}