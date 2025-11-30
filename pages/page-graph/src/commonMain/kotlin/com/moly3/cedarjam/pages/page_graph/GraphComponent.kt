package com.moly3.cedarjam.pages.page_graph

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.StateFlow

@Immutable
interface GraphComponent {
    val state: StateFlow<State>
    fun onIntent(intent: Intent)
}