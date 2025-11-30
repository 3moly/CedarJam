package com.moly3.cedarjam.features.feature_canvas.func

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.features.feature_canvas.DialogCanvasComponentImpl
import com.moly3.cedarjam.features.feature_canvas.model.DialogConfig
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.features.feature_canvas.model.DialogScope

fun ComponentContext.canvasDialogScopeFactory(
    workspaceSession: WorkspaceSession,
    storeFactory: StoreFactory
): DialogScope {
    val dialogNavigation = SlotNavigation<DialogConfig>()
    val slot = childSlot(
        key = "DefaultCanvasChildSlot",
        source = dialogNavigation,
        serializer = DialogConfig.serializer(),
        handleBackButton = true,
        childFactory = { config, context ->
            DialogCanvasComponentImpl(
                workspaceSession = workspaceSession,
                componentContext = context,
                storeFactory = storeFactory,
                file = config.file,
                openNode = {}
            )
        }
    )
    return DialogScope(
        navigation = dialogNavigation,
        slot = slot
    )
}