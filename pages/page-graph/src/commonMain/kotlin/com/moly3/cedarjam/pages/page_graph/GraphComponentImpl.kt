package com.moly3.cedarjam.pages.page_graph

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphData
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.pages.page_graph.store.GraphStoreFactory
import com.moly3.dataviz.core.graph.engine.IGraphEngine
import com.moly3.dataviz.core.graph.engine.impl.ultra.UltraFastEngine
import com.moly3.dataviz.core.graph.engine.impl.ultra.UltraFastEngineConfig
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
class GraphComponentImpl(
    @Assisted componentContext: ComponentContext,
    @Assisted storeFactory: StoreFactory,
    @Assisted workspaceSession: WorkspaceSession,
    @Assisted private val openWorkspaceSettings: (Boolean) -> Unit,
    private val graphStoreFactory: GraphStoreFactory,
) : GraphComponent,
    ComponentContext by componentContext {

    override val engine: IGraphEngine<String, ObsidianGraphData> =
        UltraFastEngine(
            UltraFastEngineConfig()
//                .Gentle.copy(
//                startAlpha =
////                antiStickDistanceMultiplier = 0f,
////                antiStickForceMultiplier = 0f,
//                minRepelAlpha = 0f,
//                dragReheatAlpha = 0.001f,
////                deStackRadius = 50f,
////                startAlpha = 0f,
//                groupChangeReheatAlpha = 0.01f
//            )
        )

    private val store by lazy {
        graphStoreFactory.create(
            storeFactory = storeFactory,
            lifecycle = lifecycle,
            workspaceSession = workspaceSession,
            openWorkspaceSettings = openWorkspaceSettings,
            stateKeeper = stateKeeper,
            engine = engine
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<State> = store.stateFlow
    override fun onIntent(intent: Intent) {
        store.accept(intent)
    }
}
