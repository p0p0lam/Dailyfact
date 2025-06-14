package com.popolam.app.dailyfact.data.remote

import android.content.Context
import com.popolam.app.dailyfact.data.KeyNotFoundException
import com.popolam.app.dailyfact.data.KeysRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.util.Locale

interface FactApiService {
    suspend fun generateRandomFact(userId:String): FactResponse? // Or directly Fact
}

class FactApiServiceImpl(private val httpClient: HttpClient, private val context: Context) : FactApiService {

    override suspend fun generateRandomFact(userId:String): FactResponse? {
        val currentDate = System.currentTimeMillis().toString().toByteArray()
        if (!KeysRepository.keyExists(context)) throw KeyNotFoundException()
        val authHeader = KeysRepository.encryptWithStoredAES(currentDate, context)?: throw KeyNotFoundException()
        val language = Locale.getDefault().language
        return try {
            httpClient.post("getRandomFact") {
                headers {
                    append(HttpHeaders.Authorization, authHeader)
                    append(HttpHeaders.AcceptLanguage, language)
                    append("X-User-Id", userId)
                }
            }.let {
                when (it.status.value) {
                    200 -> {
                        val result = it.body<AiApiResponse>()
                        extractFactFromMessage(result)
                    }
                    401 -> {
                        throw KeyNotFoundException()
                    }
                    else -> {
                        throw Exception("Error while fetching random fact: ${it.status.value}")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e,"Error while fetching random fact: ${e.message}")
            throw e
        }
    }

    private fun extractFactFromMessage(response: AiApiResponse): FactResponse? {
        val parsedFact = response.choices.firstOrNull()?.message?.takeIf { it.role == "assistant" }?.content?.let {
            val jsonStart = it.indexOf("```json")
            if (jsonStart == -1){
                kotlin.runCatching {
                    Json.decodeFromString<FactResponse>(it)
                }.getOrNull()
            }
            else Json.decodeFromString<FactResponse>(it.substring(jsonStart).removePrefix("```json").removeSuffix("```").trim())
        }
        return parsedFact
    }
}