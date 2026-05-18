package com.moly3.cedarjam.pages.page_file

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.moly3.cedarjam.core.domain.dialog.DialogColorPickerService
import com.moly3.cedarjam.core.domain.dialog.DialogDeleteService
import com.moly3.cedarjam.core.domain.dialog.DialogSelectTagService
import com.moly3.cedarjam.core.domain.model.FileType
import com.moly3.cedarjam.core.domain.model.navigation.input.FilePageInput
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.service.IImageTransform
import com.moly3.cedarjam.core.domain.service.IUtilsService
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.domain.usecase.OpenNodeDataUseCaseFactory
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.core.ui.service.IJvmBrowserService
import com.moly3.cedarjam.core.ui.service.MacTrackpadGestureService
import com.moly3.cedarjam.features.feature_canvas.func.canvasDialogScopeFactory
import com.moly3.cedarjam.features.feature_canvas.func.setIsShowGraphDialog
import com.moly3.cedarjam.features.feature_graph.func.graphDialogScopeFactory
import com.moly3.cedarjam.features.feature_graph.func.setIsShowGraphDialog
import com.moly3.cedarjam.features.feature_graph.model.GraphDialogInput
import com.moly3.cedarjam.navigation.Navigator
import com.moly3.cedarjam.pages.page_file.store.FileStoreFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
@AssistedInject
class FileComponentImpl(
    @Assisted componentContext: ComponentContext,
    @Assisted storeFactory: StoreFactory,
    @Assisted private val openMenu: (Boolean) -> Unit,
    @Assisted private val data: FilePageInput,
    @Assisted override val workspaceSession: WorkspaceSession,
    override val appEnvironment: IAppEnvironment,
    override val macTrackpadGestureService: MacTrackpadGestureService,
    override val utilsService: IUtilsService,
    override val dialogColorPicker: DialogColorPickerService,
    override val jvmBrowserService: IJvmBrowserService,
    override val filesRepository: IFilesRepository,
    private val navigator: Navigator,
    private val imageTransform: IImageTransform,
    private val openNodeDataUseCaseFactory: OpenNodeDataUseCaseFactory,
    private val dialogSelectTagService: DialogSelectTagService,
    private val dialogDeleteService: DialogDeleteService,
) : FileComponent,
    ComponentContext by componentContext {

    private val graphDialogScope by lazy {
        graphDialogScopeFactory(
            workspaceSession = workspaceSession,
            storeFactory = storeFactory,
            openWorkspaceSettings = openMenu,
            openNodeDataUseCaseFactory = openNodeDataUseCaseFactory,
            selectTagService = dialogSelectTagService,
            deleteService = dialogDeleteService,
            navigator = navigator,
            openPdfPage = {
                val state = store.state.fileType
                if (state is FileType.PDF) {
                    store.accept(Intent.ToPage(state, it))
                }
            }
        )
    }
    private val canvasDialogScope by lazy {
        canvasDialogScopeFactory(
            workspaceSession = workspaceSession,
            storeFactory = storeFactory,
            openNodeDataUseCaseFactory = openNodeDataUseCaseFactory,
            navigator = navigator,
            magnifier = macTrackpadGestureService,
            filesRepository = filesRepository,
        )
    }
    override val dialogSlot = graphDialogScope.slot
    override val dialogCanvasSlot = canvasDialogScope.slot

    private val store by lazy {
        FileStoreFactory(
            storeFactory = storeFactory,
            lifecycle = lifecycle,
            data = data,
            openMenu = openMenu,
            workspaceSession = workspaceSession,
            showCanvasDialog = { file ->
                canvasDialogScope.setIsShowGraphDialog(file, isShow = true)
            },
            navigator = navigator,
            imageTransform = imageTransform,
            filesRepository = filesRepository,
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
