package com.popolam.app.dailyfact.data.remote

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json
import java.util.Locale

interface FactApiService {
    suspend fun generateRandomFact(): FactResponse? // Or directly Fact
}

class FactApiServiceImpl(private val httpClient: HttpClient, private val firebaseRemoteConfig: FirebaseRemoteConfig) : FactApiService {
    private val API_ENDPOINT = "https://openrouter.ai/api/v1/chat/completions"
    private val API_KEY_CONFIG_KEY = "openrouter_api_key"
    override suspend fun generateRandomFact(): FactResponse? {
        val apiKey = getApiKeyFromRemoteConfig() ?: return null
        val language = Locale.getDefault().language
        return httpClient.post(API_ENDPOINT){
            bearerAuth(apiKey)
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
            val body = createAiApiRequest(language)
            setBody(body)

        }.let {
            val result = it.body<AiApiResponse>()
            extractFactFromMessage(result)
        }
    }

    private suspend fun getApiKeyFromRemoteConfig(): String? {
        return try {
            firebaseRemoteConfig.fetchAndActivate()
            firebaseRemoteConfig.getString(API_KEY_CONFIG_KEY)
                .takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            null
        }
    }
    private fun extractFactFromMessage(response: AiApiResponse): FactResponse? {
        val parsedFact = response.choices.firstOrNull()?.message?.takeIf { it.role == "assistant" }?.content?.let {
            val jsonStart = it.indexOf("```json")
            if (jsonStart == -1) null
            else Json.decodeFromString<FactResponse>(it.substring(jsonStart).removePrefix("```json").removeSuffix("```").trim())
        }
        return parsedFact
    }
}