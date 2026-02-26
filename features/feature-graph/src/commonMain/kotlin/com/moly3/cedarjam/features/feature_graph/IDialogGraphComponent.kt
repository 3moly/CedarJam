package com.moly3.cedarjam.features.feature_graph

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.StateFlow

@Stable
interface IDialogGraphComponent {

    val state: StateFlow<State>
    fun onIntent(intent: Intent)

}