package com.moly3.cedarjam.core.net.di

import co.touchlab.kermit.Logger
import com.moly3.cedarjam.core.net.IRemoteSyncRepository
import com.moly3.cedarjam.core.net.RemoteSyncRepository
import com.moly3.cedarjam.core.net.getHttpClientEngine
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
import org.koin.dsl.module

fun net(baseUrl: String) = module {
    single<IRemoteSyncRepository> {
        val httpClient = HttpClient(engine = getHttpClientEngine()) {
            install(HttpTimeout) {
                requestTimeoutMillis = 30 * 1000
                //Issue: IOS - Darwin doesn't support a connection timeout.
                connectTimeoutMillis = 30 * 1000
            }
            if (true) {
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
                    level = LogLevel.BODY
                }
            }

            install(DefaultRequest) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
            install(ContentNegotiation) {
                json(json = get())
            }
        }
        RemoteSyncRepository(
            httpClient = httpClient,
            baseUrl = baseUrl,
            json = get()
        )
    }
}