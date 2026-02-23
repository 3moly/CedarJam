package com.moly3.cedarjam.pages.page_select_workspace

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.StateFlow

@Immutable
interface ISelectWorkspaceComponent {
    val state: StateFlow<State>
    fun onIntent(intent: Intent)
}