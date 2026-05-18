package com.moly3.cedarjam.ui.pages.tag

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.moly3.cedarjam.core.domain.dialog.DialogDeleteService
import com.moly3.cedarjam.core.domain.dialog.DialogSelectTagService
import com.moly3.cedarjam.core.domain.model.navigation.input.TagPageInput
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.domain.usecase.OpenNodeDataUseCaseFactory
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.features.feature_graph.func.graphDialogScopeFactory
import com.moly3.cedarjam.features.feature_graph.func.isGraphDialogInited
import com.moly3.cedarjam.features.feature_graph.func.setIsShowGraphDialog
import com.moly3.cedarjam.features.feature_graph.model.GraphDialogInput
import com.moly3.cedarjam.navigation.Navigator
import com.moly3.cedarjam.ui.pages.tag.store.TagStoreFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
@AssistedInject
class TagComponentImpl(
    @Assisted private val workspaceSession: WorkspaceSession,
    @Assisted componentContext: ComponentContext,
    @Assisted storeFactory: StoreFactory,
    @Assisted private val data: TagPageInput,
    @Assisted private val openWorkspaceSettings: (Boolean) -> Unit,
    private val dialogSelectTagService: DialogSelectTagService,
    private val dialogDeleteService: DialogDeleteService,
    private val openNodeDataUseCaseFactory: OpenNodeDataUseCaseFactory,
    private val navigator: Navigator,
) : TagComponent,
    ComponentContext by componentContext {
    private val graphDialogScope by lazy {
        graphDialogScopeFactory(
            workspaceSession = workspaceSession,
            storeFactory = storeFactory,
            openWorkspaceSettings = openWorkspaceSettings,
            openNodeDataUseCaseFactory = openNodeDataUseCaseFactory,
            selectTagService = dialogSelectTagService,
            deleteService = dialogDeleteService,
            navigator = navigator,
        )
    }
    override val dialogSlot = graphDialogScope.slot

    init {
        if (data.isOpenGraphDialog && !graphDialogScope.isGraphDialogInited()) {
            graphDialogScope.setIsShowGraphDialog(target = GraphDialogInput.Tag(data.id), isShow = true)
        }
    }

    private val store by lazy {
        TagStoreFactory(
            workspaceSession = workspaceSession,
            storeFactory = storeFactory,
            lifecycle = lifecycle,
            pageData = data,
            openWorkspaceSettings = openWorkspaceSettings,
            selectTagService = dialogSelectTagService,
            openNodeDataUseCaseFactory = openNodeDataUseCaseFactory,
            navigator = navigator,
        ).create()
    }

    override val nameFlow: StateFlow<PageNameData?> = store.nameStateFlow
    override val state: StateFlow<State> = store.stateFlow
    override fun onIntent(intent: Intent) {
        store.accept(intent)
    }

    override fun setIsShowGraph(isShowMenu: Boolean) {
        graphDialogScope.setIsShowGraphDialog(
            target = GraphDialogInput.Tag(data.id),
            isShow = isShowMenu
        )
    }
}
