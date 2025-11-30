package com.moly3.cedarjam.ui.pages.tags

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.StateFlow

@Immutable
interface TagsComponent {
    val state: StateFlow<State>
    fun onIntent(intent: Intent)
}