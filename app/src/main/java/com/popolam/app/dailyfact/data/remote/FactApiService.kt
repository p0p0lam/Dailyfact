package com.popolam.app.dailyfact.data.remote

import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.popolam.app.dailyfact.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import java.util.Locale

interface FactApiService {
    suspend fun generateRandomFact(): FactResponse? // Or directly Fact
}

class FactApiServiceImpl(private val httpClient: HttpClient, private val apiKeyProcessor: ApiKeyProcessor) : FactApiService {
    private val API_ENDPOINT = "https://openrouter.ai/api/v1/chat/completions"
    private val firebaseDatabase = Firebase.database
    private var ak: String? = null
    override suspend fun generateRandomFact(): FactResponse? {
        if (ak == null){
            ak = getApiKeyFromRemoteConfig()?.let {
                apiKeyProcessor.process(it)
            } ?: throw IllegalStateException("Oops")
        }
        val language = Locale.getDefault().language
        return httpClient.post(API_ENDPOINT){
            bearerAuth(ak!!)
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
            firebaseDatabase.reference.child("ak").child("or").get().await()
                .getValue(String::class.java)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e("FactApiService", "Error getting API key", e)
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