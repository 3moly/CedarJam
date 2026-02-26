package com.moly3.cedarjam.pages.page_collection_row

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.moly3.cedarjam.features.feature_graph.func.graphDialogScopeFactory
import com.moly3.cedarjam.features.feature_graph.func.setIsShowGraphDialog
import com.moly3.cedarjam.core.domain.model.navigation.input.CollectionRowPageInput
import com.moly3.cedarjam.pages.page_collection_row.store.CollectionRowStoreFactory
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.features.feature_graph.model.GraphDialogInput
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class CollectionRowComponentImpl(
    override val workspaceSession: WorkspaceSession,
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    private val data: CollectionRowPageInput,
    private val openWorkspaceSettings: (Boolean) -> Unit
) : CollectionRowComponent,
    ComponentContext by componentContext {

    private val store by lazy {
        CollectionRowStoreFactory(
            storeFactory = storeFactory,
            lifecycle = lifecycle,
            pageInput = data,
            workspaceSession = workspaceSession,
            openWorkspaceSettings = openWorkspaceSettings,
        ).create()
    }

    override val nameFlow: StateFlow<PageNameData?> = store.nameStateFlow
    override val state: StateFlow<State> = store.stateFlow
    override fun onIntent(intent: Intent) {
        store.accept(intent)
    }

    private val graphDialogScope by lazy {
        graphDialogScopeFactory(
            workspaceSession = workspaceSession,
            storeFactory = storeFactory,
            openWorkspaceSettings = openWorkspaceSettings
        )
    }
    override val dialogSlot = graphDialogScope.slot

    override fun setIsShowGraph(isShowMenu: Boolean) {
        graphDialogScope.setIsShowGraphDialog(
            target = GraphDialogInput.Row(data.rowId),
            isShow = isShowMenu
        )
    }
}