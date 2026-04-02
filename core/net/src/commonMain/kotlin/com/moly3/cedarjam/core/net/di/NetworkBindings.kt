package com.moly3.cedarjam.core.net.di

import co.touchlab.kermit.Logger
import com.moly3.cedarjam.core.domain.DefaultJson
import com.moly3.cedarjam.core.net.IRemoteSyncRepository
import com.moly3.cedarjam.core.net.RemoteSyncRepository
import com.moly3.cedarjam.core.net.getHttpClientEngine
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

@ContributesTo(AppScope::class)
@BindingContainer
object NetworkBindings {

    @SingleIn(AppScope::class)
    @Provides
    fun provideJson(): Json {
        return DefaultJson
    }

    @SingleIn(AppScope::class)
    @Provides
    fun provideRemoteSyncRepository(json: Json): IRemoteSyncRepository {
        val httpClient = HttpClient(engine = getHttpClientEngine()) {
            install(HttpTimeout) {
                requestTimeoutMillis = 120 * 1000
                //Issue: IOS - Darwin doesn't support a connection timeout.
                connectTimeoutMillis = 120 * 1000
            }
            install(Logging) {
                logger = object : io.ktor.client.plugins.logging.Logger {
                    override fun log(message: String) {
                        if (message.length < 1_000_000) {
                            try {
                                Logger.w { "IAPIGenerator $message" }
                                //loggerService.w("IAPIGenerator $message")
                            } catch (exc: Exception) {
                            }
                        }
                    }
                }
                level = LogLevel.INFO
            }

            install(DefaultRequest) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
            install(ContentNegotiation) {
                json(json = json)
            }
        }
        return RemoteSyncRepository(
            httpClient = httpClient,
            baseUrl = "baseUrl",
            json = json,
            token = "token"
        )
    }
}