package com.moly3.cedarjam.pages.page_select_workspace

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow

@Immutable
interface ISelectWorkspaceComponent {
    val state: Flow<State>
    fun onIntent(intent: Intent)
}