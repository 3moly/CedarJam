package com.moly3.cedarjam.features.feature_graph.func

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.dialog.DialogDeleteService
import com.moly3.cedarjam.core.domain.dialog.DialogSelectTagService
import com.moly3.cedarjam.core.domain.usecase.OpenNodeDataUseCaseFactory
import com.moly3.cedarjam.features.feature_graph.DialogGraphComponentImpl
import com.moly3.cedarjam.features.feature_graph.model.DialogConfig
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.features.feature_graph.model.DialogScope
import com.moly3.cedarjam.navigation.Navigator

fun ComponentContext.graphDialogScopeFactory(
    workspaceSession: WorkspaceSession,
    storeFactory: StoreFactory,
    openWorkspaceSettings: (Boolean) -> Unit,
    openNodeDataUseCaseFactory: OpenNodeDataUseCaseFactory,
    selectTagService: DialogSelectTagService,
    deleteService: DialogDeleteService,
    navigator: Navigator,
    openPdfPage: (Int) -> Unit = {}
): DialogScope {
    val dialogNavigation = SlotNavigation<DialogConfig>()
    val slot = childSlot(
        key = "DefaultGraphChildSlot",
        source = dialogNavigation,
        serializer = DialogConfig.serializer(),
        handleBackButton = false,
        childFactory = { config, context ->
            DialogGraphComponentImpl(
                workspaceSession = workspaceSession,
                componentContext = context,
                storeFactory = storeFactory,
                startTargetId = config.target,
                openWorkspaceSettings = openWorkspaceSettings,
                openPdfPage = openPdfPage,
                openNodeDataUseCase = openNodeDataUseCaseFactory(workspaceSession.fileManagerService),
                selectTagService = selectTagService,
                deleteService = deleteService,
                navigator = navigator,
            )
        }
    )
    return DialogScope(
        dialogNavigation,
        slot
    )
}
