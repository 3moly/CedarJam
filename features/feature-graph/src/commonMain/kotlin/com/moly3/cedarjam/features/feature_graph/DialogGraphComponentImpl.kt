package com.moly3.cedarjam.features.feature_graph

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.moly3.cedarjam.core.domain.dialog.DialogDeleteService
import com.moly3.cedarjam.core.domain.dialog.DialogSelectTagService
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphData
import com.moly3.cedarjam.core.domain.usecase.IOpenNodeDataUseCase
import com.moly3.cedarjam.features.feature_graph.store.DialogGraphStoreFactory
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.features.feature_graph.model.GraphDialogInput
import com.moly3.cedarjam.navigation.Navigator
import com.moly3.dataviz.core.graph.engine.IGraphEngine
import com.moly3.dataviz.core.graph.engine.impl.ultra.UltraFastEngine
import com.moly3.dataviz.core.graph.engine.impl.ultra.UltraFastEngineConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class DialogGraphComponentImpl(
    workspaceSession: WorkspaceSession,
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    private val startTargetId: GraphDialogInput,
    private val openWorkspaceSettings: (Boolean) -> Unit,
    private val openPdfPage: (Int) -> Unit,
    private val openNodeDataUseCase: IOpenNodeDataUseCase,
    private val selectTagService: DialogSelectTagService,
    private val deleteService: DialogDeleteService,
    private val navigator: Navigator,

    ) : IDialogGraphComponent,
    ComponentContext by componentContext {

    private val store by lazy {
        DialogGraphStoreFactory(
            storeFactory = storeFactory,
            lifecycle = lifecycle,
            workspaceSession = workspaceSession,
            targetInput = startTargetId,
            openWorkspaceSettings = openWorkspaceSettings,
            openPdfPage = openPdfPage,
            openNodeDataUseCase = openNodeDataUseCase,
            selectTagService = selectTagService,
            deleteService = deleteService,
            navigator = navigator,
        ).create(stateKeeper = stateKeeper)
    }

    override val engine: IGraphEngine<String, ObsidianGraphData> by lazy {
        UltraFastEngine(UltraFastEngineConfig.Default.copy(startAlpha = 0f))
    }

    override val state: StateFlow<State> = store.stateFlow
    override fun onIntent(intent: Intent) {
        store.accept(intent = intent)
    }
}
