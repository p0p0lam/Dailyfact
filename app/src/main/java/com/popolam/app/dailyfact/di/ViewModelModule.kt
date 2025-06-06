package com.popolam.app.dailyfact.di

import com.popolam.app.dailyfact.ui.viewmodel.FactViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { FactViewModel(get(), get()) }
}