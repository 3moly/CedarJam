package com.moly3.cedarjam.pages.page_file

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.moly3.cedarjam.core.domain.dialog.DialogColorPickerService
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.core.domain.model.navigation.input.FilePageInput
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.service.IUtilsService
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.ui.service.IJvmBrowserService
import com.moly3.cedarjam.core.ui.service.MacTrackpadGestureService
import com.moly3.cedarjam.features.feature_canvas.func.canvasDialogScopeFactory
import com.moly3.cedarjam.features.feature_canvas.func.setIsShowGraphDialog
import com.moly3.cedarjam.features.feature_graph.func.graphDialogScopeFactory
import com.moly3.cedarjam.features.feature_graph.func.setIsShowGraphDialog
import com.moly3.cedarjam.features.feature_graph.model.GraphDialogInput
import com.moly3.cedarjam.pages.page_file.store.FileStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@OptIn(ExperimentalCoroutinesApi::class)
class FileComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    private val openMenu: (Boolean) -> Unit,
    private val data: FilePageInput,
    override val workspaceSession: WorkspaceSession
) : FileComponent,
    ComponentContext by componentContext,
    KoinComponent {
//
    private val graphDialogScope by lazy {
        graphDialogScopeFactory(
            workspaceSession = workspaceSession,
            storeFactory = storeFactory,
            openWorkspaceSettings = openMenu
        )
    }
    private val canvasDialogScope by lazy {
        canvasDialogScopeFactory(workspaceSession, storeFactory)
    }
    override val dialogSlot = graphDialogScope.slot
    override val dialogCanvasSlot = canvasDialogScope.slot

    override val appEnvironment: IAppEnvironment by inject()
    override val macTrackpadGestureService: MacTrackpadGestureService by inject()
    override val utilsService: IUtilsService by inject()
    override val dialogColorPicker: DialogColorPickerService by inject()
    override val jvmBrowserService: IJvmBrowserService by inject()
    override val filesRepository: IFilesRepository by inject()

    private val store by lazy {
        FileStoreFactory(
            storeFactory = storeFactory,
            lifecycle = lifecycle,
            data = data,
            openMenu = openMenu,
            workspaceSession = workspaceSession,
            showCanvasDialog = { file ->
                canvasDialogScope.setIsShowGraphDialog(file, isShow = true)
            }
        ).create(stateKeeper)
    }
    override val nameFlow: Flow<PageNameData?> = store.nameStateFlow
    override val state: StateFlow<State> = store.stateFlow
    override fun onIntent(intent: Intent) {
        store.accept(intent)
    }

    override fun setIsShowGraph(isShowMenu: Boolean) {
        graphDialogScope.setIsShowGraphDialog(
            target = GraphDialogInput.File(data.timestamp),
            isShow = isShowMenu
        )
    }
}
