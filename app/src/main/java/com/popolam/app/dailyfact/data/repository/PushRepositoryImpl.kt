package com.popolam.app.dailyfact.data.repository

import com.popolam.app.dailyfact.data.remote.PushApiService

class PushRepositoryImpl(private val pushApiService: PushApiService) {
    suspend fun processPushToken(pushToken: String){

    }
}