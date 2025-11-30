package com.moly3.cedarjam

import co.touchlab.kermit.Logger
import com.arkivanov.essenty.statekeeper.SerializableContainer
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.json.Json
import platform.Foundation.NSCoder
import platform.Foundation.NSString
import platform.Foundation.decodeTopLevelObjectOfClass
import platform.Foundation.encodeObject

internal val json =
    Json {
        allowStructuredMapKeys = true
    }

@Suppress("unused") // Used in Swift
fun save(coder: NSCoder, state: SerializableContainer) {
    coder.encodeObject(
        `object` = json.encodeToString(SerializableContainer.serializer(), state),
        forKey = "state"
    )
}

@Suppress("unused") // Used in Swift
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun restore(coder: NSCoder): SerializableContainer? =
    (coder.decodeTopLevelObjectOfClass(
        aClass = NSString,
        forKey = "state",
        error = null
    ) as String?)?.let {
        try {
            json.decodeFromString(SerializableContainer.serializer(), it)
        } catch (e: Exception) {
            Logger.e { e.message ?: "" }
            null
        }
    }
