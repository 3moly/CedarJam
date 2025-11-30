package com.moly3.cedarjam.core.net

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO

actual fun getHttpClientEngine(): HttpClientEngine {
    return CIO.create()
}