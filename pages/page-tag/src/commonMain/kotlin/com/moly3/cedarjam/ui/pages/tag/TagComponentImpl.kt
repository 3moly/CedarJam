package com.moly3.cedarjam.ui.pages.tag

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.moly3.cedarjam.features.feature_graph.func.graphDialogScopeFactory
import com.moly3.cedarjam.features.feature_graph.func.isGraphDialogInited
import com.moly3.cedarjam.features.feature_graph.func.setIsShowGraphDialog
import com.moly3.cedarjam.core.domain.model.navigation.input.TagPageInput
import com.moly3.cedarjam.ui.pages.tag.store.TagStoreFactory
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.core.domain.model.getTagGraphId
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.features.feature_graph.model.GraphDialog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class TagComponentImpl(
    private val workspaceSession: WorkspaceSession,
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    private val data: TagPageInput,
    private val openWorkspaceSettings: (Boolean) -> Unit
) : TagComponent,
    ComponentContext by componentContext {
    private val graphDialogScope by lazy {
        graphDialogScopeFactory(
            workspaceSession = workspaceSession,
            storeFactory = storeFactory,
            openWorkspaceSettings = openWorkspaceSettings
        )
    }
    override val dialogSlot = graphDialogScope.slot

    init {
        if (data.isOpenGraphDialog && !graphDialogScope.isGraphDialogInited()) {
            graphDialogScope.setIsShowGraphDialog(target = GraphDialog.Tag(data.id), isShow = true)
        }
    }

    private val store by lazy {
        TagStoreFactory(
            workspaceSession = workspaceSession,
            storeFactory = storeFactory,
            lifecycle = lifecycle,
            pageData = data,
            setIsShowGraph = {
                graphDialogScope.setIsShowGraphDialog(
                    target = GraphDialog.Tag(data.id),
                    isShow = it
                )
            },
            openWorkspaceSettings = openWorkspaceSettings
        ).create()
    }

    override val nameFlow: StateFlow<PageNameData?> = store.nameStateFlow
    override val state: StateFlow<State> = store.stateFlow
    override fun onIntent(intent: Intent) {
        store.accept(intent)
    }
}
