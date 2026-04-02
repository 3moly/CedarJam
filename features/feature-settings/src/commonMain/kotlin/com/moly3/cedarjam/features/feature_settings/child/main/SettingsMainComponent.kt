package com.moly3.cedarjam.features.feature_settings.child.main

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsMainComponent(
    componentContext: ComponentContext,
    private val openGeneral: () -> Unit,
    private val close: () -> Unit,
    private val openStorage: () -> Unit,
    private val openSync: () -> Unit,
) : ISettingsMainComponent,
    ComponentContext by componentContext {

    override fun onIntent(intent: Intent) {
        when (intent) {
            Intent.Close -> close()
            Intent.General -> openGeneral()
            Intent.Storage -> openStorage()
            Intent.Sync -> openSync()
        }
    }
}