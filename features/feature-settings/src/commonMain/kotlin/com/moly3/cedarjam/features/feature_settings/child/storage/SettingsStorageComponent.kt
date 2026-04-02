package com.moly3.cedarjam.features.feature_settings.child.storage

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.features.feature_settings.child.storage.store.SettingsStorageStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsStorageComponent(
    componentContext: ComponentContext,
    private val workspaceSession: WorkspaceSession,
    storeFactory: StoreFactory,
    private val back: () -> Unit,
    private val close: () -> Unit
) : ISettingsStorageComponent,
    ComponentContext by componentContext {

    private val store by lazy {
        SettingsStorageStoreFactory(
            storeFactory = storeFactory,
            lifecycle = lifecycle,
            back = back,
            close = close,
            workspaceSession = workspaceSession
        ).create()
    }

    override val state: StateFlow<State> = store.stateFlow

    override fun onIntent(intent: Intent) {
        store.accept(intent)
    }
}