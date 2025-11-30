package com.moly3.cedarjam.features.feature_graph

import kotlinx.coroutines.flow.StateFlow

interface IDialogGraphComponent {

    val targetId: String?
    val state: StateFlow<State>
    fun onIntent(intent: Intent)
}