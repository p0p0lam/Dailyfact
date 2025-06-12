package com.popolam.app.dailyfact.data.remote

import io.ktor.client.HttpClient
import timber.log.Timber

class PushApiService(private val httpClient: HttpClient) {
    suspend fun sendPushToken(token: String) {
        Timber.d("Sending push token: $token")
        /*httpClient.post<Unit>("/api/push/token") {
            body = PushTokenRequest(token)
        }*/
    }
}