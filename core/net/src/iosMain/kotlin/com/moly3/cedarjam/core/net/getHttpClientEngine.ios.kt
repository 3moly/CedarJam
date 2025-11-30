package com.moly3.cedarjam.core.net

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual fun getHttpClientEngine(): HttpClientEngine {
    return Darwin.create {
        configureRequest {
            setAllowsCellularAccess(true)
        }
    }
}