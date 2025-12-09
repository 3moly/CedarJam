package com.moly3.cedarjam.features.feature_settings.child.storage

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.StateFlow

@Stable
interface ISettingsStorageComponent {
    val state: StateFlow<State>
    fun onIntent(intent: Intent)
}