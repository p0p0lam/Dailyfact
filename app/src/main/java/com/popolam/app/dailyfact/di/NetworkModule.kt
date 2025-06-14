package com.popolam.app.dailyfact.di

import com.popolam.app.dailyfact.data.remote.FactApiService
import com.popolam.app.dailyfact.data.remote.FactApiServiceImpl
import com.popolam.app.dailyfact.data.remote.PushApiService
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.HttpHeaders
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.named
import org.koin.dsl.module

val networkModule = module {
    single {
        HttpClient(OkHttp) {
            engine {
                // this: OkHttpConfig
                config {
                    // this: OkHttpClient.Builder
                    followRedirects(true)
                    // ...
                }
            }
            defaultRequest {
                url("http://192.168.1.148:8080/api/")
            }
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true // Important for API evolution
                })
            }
            install(Logging){
                logger = Logger.DEFAULT
                level = LogLevel.ALL
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }
            // Configure engine, timeouts, logging etc.
            // engine { connectTimeout = 10_000; socketTimeout = 10_000 }
        }
    }
    single<FactApiService> { FactApiServiceImpl(get(), get()) }
    single<PushApiService> { PushApiService(get(), get()) }
    single(createdAtStart = true, qualifier = named("base_url")) { "http://192.168.1.148:8080/" }
}