package com.moly3.cedarjam.features.feature_canvas.func

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.usecase.OpenNodeDataUseCaseFactory
import com.moly3.cedarjam.core.ui.service.MacTrackpadGestureService
import com.moly3.cedarjam.features.feature_canvas.DialogCanvasComponentImpl
import com.moly3.cedarjam.features.feature_canvas.model.DialogConfig
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.features.feature_canvas.model.DialogScope
import com.moly3.cedarjam.navigation.Navigator

fun ComponentContext.canvasDialogScopeFactory(
    workspaceSession: WorkspaceSession,
    storeFactory: StoreFactory,
    openNodeDataUseCaseFactory: OpenNodeDataUseCaseFactory,
    navigator: Navigator,
    magnifier: MacTrackpadGestureService,
    filesRepository: IFilesRepository,
): DialogScope {
    val dialogNavigation = SlotNavigation<DialogConfig>()
    val slot = childSlot(
        key = "DefaultCanvasChildSlot",
        source = dialogNavigation,
        serializer = DialogConfig.serializer(),
        handleBackButton = false,
        childFactory = { config, context ->
            DialogCanvasComponentImpl(
                workspaceSession = workspaceSession,
                componentContext = context,
                storeFactory = storeFactory,
                file = config.file,
                openNodeDataUseCase = openNodeDataUseCaseFactory(workspaceSession.fileManagerService),
                navigator = navigator,
                magnifier = magnifier,
                filesRepository = filesRepository,
            )
        }
    )
    return DialogScope(
        navigation = dialogNavigation,
        slot = slot
    )
}
