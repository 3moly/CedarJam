package com.moly3.cedarjam.features.feature_canvas

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.features.feature_canvas.store.DialogCanvasStoreFactory
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphData
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import com.moly3.cedarjam.navigation.AppGraphServicesLocator

@OptIn(ExperimentalCoroutinesApi::class)
class DialogCanvasComponentImpl(
    workspaceSession: WorkspaceSession,
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    private val file: FileTreeNode.File,
    private val openNode: (ObsidianGraphData) -> Unit,

    ) : IDialogCanvasComponent,
    ComponentContext by componentContext {

    private val d get() = AppGraphServicesLocator.instance
    override val filesRepository: IFilesRepository get() = d.filesRepository

    private val store by lazy {
        DialogCanvasStoreFactory(
            storeFactory = storeFactory,
            lifecycle = lifecycle,
            workspaceSession = workspaceSession,
            file = file,
            openNode = openNode
        ).create(stateKeeper = stateKeeper)
    }

    override val state: StateFlow<State> = store.stateFlow
    override fun onIntent(intent: Intent) {
        store.accept(intent = intent)
    }
}