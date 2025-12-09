package com.moly3.cedarjam.features.feature_settings.child.sync

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.StateFlow

@Stable
interface ISettingsSyncComponent {
    val state: StateFlow<State>
    fun onIntent(intent: Intent)
}