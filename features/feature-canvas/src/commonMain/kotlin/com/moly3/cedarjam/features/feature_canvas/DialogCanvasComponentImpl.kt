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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@OptIn(ExperimentalCoroutinesApi::class)
class DialogCanvasComponentImpl(
    workspaceSession: WorkspaceSession,
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    private val file: FileTreeNode.File,
    private val openNode: (ObsidianGraphData) -> Unit,

    ) : IDialogCanvasComponent,
    ComponentContext by componentContext, KoinComponent {

    override val filesRepository: IFilesRepository by inject()

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