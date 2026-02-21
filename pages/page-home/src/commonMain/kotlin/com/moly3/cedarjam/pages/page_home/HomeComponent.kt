package com.moly3.cedarjam.pages.page_home

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.StateFlow

@Immutable
interface HomeComponent {
    val workspaceFullPath: String
    val state: StateFlow<State>
    fun onIntent(intent: Intent)
}