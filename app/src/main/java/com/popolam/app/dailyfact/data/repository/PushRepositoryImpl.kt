package com.popolam.app.dailyfact.data.repository

import com.popolam.app.dailyfact.data.Installation
import com.popolam.app.dailyfact.data.remote.PushApiService
import timber.log.Timber

interface PushRepository{
    suspend fun processPushToken(pushToken: String)
    suspend fun saveSecret(secret: String)
}

class PushRepositoryImpl(private val pushApiService: PushApiService, private val installation: Installation): PushRepository{
    override suspend fun processPushToken(pushToken: String){
        Timber.d("processPushToken")
        installation.savePushToken(pushToken)
        val aesKey  = pushApiService.sendPushToken(pushToken)
        installation.saveSecretKey(aesKey)
    }

    override suspend fun saveSecret(secret: String) {
        Timber.d("saveSecret called")
        installation.saveSecretKey(secret)
    }
}