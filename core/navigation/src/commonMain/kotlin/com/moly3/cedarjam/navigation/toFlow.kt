package com.moly3.cedarjam.navigation

import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@ExperimentalCoroutinesApi
val <T : Any> Value<T>.stateFlow: StateFlow<T>
    get() = StoreStateFlow(store = this)

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
private class StoreStateFlow<T : Any>(
    private val store: Value<T>,
) : StateFlow<T> {

    override val value: T get() = store.value

    override val replayCache: List<T> get() = listOf(store.value)

    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        val flow = MutableStateFlow(store.value)

        val disposable = store.subscribe({
            flow.value = it
        })

        try {
            flow.collect(collector)
        } finally {
            disposable.cancel()
        }
    }
}