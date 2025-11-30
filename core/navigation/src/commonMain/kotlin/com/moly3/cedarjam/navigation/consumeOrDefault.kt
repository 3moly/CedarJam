package com.moly3.cedarjam.navigation

import com.arkivanov.essenty.statekeeper.StateKeeper
import kotlinx.serialization.DeserializationStrategy

fun <T : Any> StateKeeper.consumeOrDefault(
    key: String,
    strategy: DeserializationStrategy<T>,
    default: T
): T {
    return try {
        this.consume(key, strategy = strategy) ?: default
    } catch (exc: Exception) {
        default
    }
}