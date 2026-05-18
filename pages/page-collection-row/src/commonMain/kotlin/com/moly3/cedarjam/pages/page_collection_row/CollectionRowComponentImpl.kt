package com.moly3.cedarjam.pages.page_collection_row

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.moly3.cedarjam.core.domain.model.navigation.input.CollectionRowPageInput
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.domain.dialog.DialogDeleteService
import com.moly3.cedarjam.core.domain.dialog.DialogSelectTagService
import com.moly3.cedarjam.core.domain.usecase.NavigateToFileUseCaseFactory
import com.moly3.cedarjam.core.domain.usecase.OpenNodeDataUseCaseFactory
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.features.feature_graph.func.graphDialogScopeFactory
import com.moly3.cedarjam.features.feature_graph.func.setIsShowGraphDialog
import com.moly3.cedarjam.features.feature_graph.model.GraphDialogInput
import com.moly3.cedarjam.navigation.Navigator
import com.moly3.cedarjam.pages.page_collection_row.store.CollectionRowStoreFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
@AssistedInject
class CollectionRowComponentImpl(
    @Assisted override val workspaceSession: WorkspaceSession,
    @Assisted componentContext: ComponentContext,
    @Assisted storeFactory: StoreFactory,
    @Assisted private val data: CollectionRowPageInput,
    @Assisted private val openWorkspaceSettings: (Boolean) -> Unit,
    private val navigator: Navigator,
    private val navigateToFileUseCaseFactory: NavigateToFileUseCaseFactory,
    private val openNodeDataUseCaseFactory: OpenNodeDataUseCaseFactory,
    private val dialogSelectTagService: DialogSelectTagService,
    private val dialogDeleteService: DialogDeleteService,
) : CollectionRowComponent,
    ComponentContext by componentContext {

    private val store by lazy {
        CollectionRowStoreFactory(
            storeFactory = storeFactory,
            lifecycle = lifecycle,
            pageInput = data,
            workspaceSession = workspaceSession,
            openWorkspaceSettings = openWorkspaceSettings,
            navigator = navigator,
            navigateToFileUseCaseFactory = navigateToFileUseCaseFactory,
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
            openWorkspaceSettings = openWorkspaceSettings,
            openNodeDataUseCaseFactory = openNodeDataUseCaseFactory,
            selectTagService = dialogSelectTagService,
            deleteService = dialogDeleteService,
            navigator = navigator,
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