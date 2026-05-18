package com.moly3.cedarjam.features.feature_canvas

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.features.feature_canvas.store.DialogCanvasStoreFactory
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphData
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.domain.usecase.IOpenNodeDataUseCase
import com.moly3.cedarjam.core.ui.service.MacTrackpadGestureService
import com.moly3.cedarjam.navigation.Navigator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class DialogCanvasComponentImpl(
    workspaceSession: WorkspaceSession,
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    private val file: FileTreeNode.File,
    private val openNodeDataUseCase: IOpenNodeDataUseCase,
//    private val filesRepository: IFilesRepository,
    private val navigator: Navigator,
    private val magnifier: MacTrackpadGestureService,
//    private val openNode: (ObsidianGraphData) -> Unit,
    override val filesRepository: IFilesRepository,
) : IDialogCanvasComponent,
    ComponentContext by componentContext {

    private val store by lazy {
        DialogCanvasStoreFactory(
            storeFactory = storeFactory,
            lifecycle = lifecycle,
            workspaceSession = workspaceSession,
            file = file,
            openNodeDataUseCase = openNodeDataUseCase,
            navigator = navigator,
            magnifier = magnifier,
            filesRepository = filesRepository
        ).create(stateKeeper = stateKeeper)
    }

    override val state: StateFlow<State> = store.stateFlow
    override fun onIntent(intent: Intent) {
        store.accept(intent = intent)
    }
}