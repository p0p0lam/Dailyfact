package com.popolam.app.dailyfact.data.remote

import com.popolam.app.dailyfact.data.Installation
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import timber.log.Timber

class PushApiService(private val httpClient: HttpClient, private val installation: Installation) {

    suspend fun sendPushToken(token: String):String {
        Timber.d("Sending push token: $token")
        return httpClient.post("pushToken") {
            contentType(ContentType.Application.Json)
            setBody(PushTokenRequest(token, installation.id(), installation.getPublicKeyPem()))
        }.body<PushTokenResponse>().let {
            Timber.d("Push token response: $it")
            it.wrappedAesKey
        }
    }
}