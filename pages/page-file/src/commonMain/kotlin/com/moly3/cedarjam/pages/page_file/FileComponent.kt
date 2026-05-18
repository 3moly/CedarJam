package com.moly3.cedarjam.pages.page_file

import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.moly3.cedarjam.core.domain.dialog.DialogColorPickerService
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.ui.service.IJvmBrowserService
import com.moly3.cedarjam.core.ui.service.MacTrackpadGestureService
import com.moly3.cedarjam.core.domain.service.IUtilsService
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.features.feature_canvas.IDialogCanvasComponent
import com.moly3.cedarjam.features.feature_graph.IDialogGraphContainer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Immutable
interface FileComponent: IDialogGraphContainer {
    val dialogCanvasSlot: Value<ChildSlot<*, IDialogCanvasComponent>>
    val jvmBrowserService: IJvmBrowserService
    val filesRepository: IFilesRepository
    val dialogColorPicker: DialogColorPickerService
    val appEnvironment: IAppEnvironment
    val macTrackpadGestureService: MacTrackpadGestureService
    val utilsService: IUtilsService
    val workspaceSession: WorkspaceSession
    val nameFlow: Flow<PageNameData?>
    val state: StateFlow<State>
    fun onIntent(intent: Intent)
}