package com.moly3.cedarjam.features.feature_graph

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.moly3.cedarjam.features.feature_graph.store.DialogGraphStoreFactory
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.features.feature_graph.model.GraphDialogInput
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class DialogGraphComponentImpl(
    workspaceSession: WorkspaceSession,
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    private val startTargetId: GraphDialogInput,
    private val openWorkspaceSettings: (Boolean) -> Unit
) : IDialogGraphComponent,
    ComponentContext by componentContext {

    private val store by lazy {
        DialogGraphStoreFactory(
            storeFactory = storeFactory,
            lifecycle = lifecycle,
            workspaceSession = workspaceSession,
            targetInput = startTargetId,
            openWorkspaceSettings = openWorkspaceSettings
        ).create(stateKeeper = stateKeeper)
    }

    override val state: StateFlow<State> = store.stateFlow
    override fun onIntent(intent: Intent) {
        store.accept(intent = intent)
    }
}