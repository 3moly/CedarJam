package com.moly3.cedarjam.features.feature_settings.func

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.moly3.cedarjam.features.feature_settings.DialogSettingsComponentImpl
import com.moly3.cedarjam.features.feature_settings.model.DialogConfig
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.features.feature_settings.model.DialogScope

fun ComponentContext.settingsDialogScopeFactory(
    workspaceSession: WorkspaceSession
): DialogScope {
    val dialogNavigation = SlotNavigation<DialogConfig>()
    val slot = childSlot(
        key = "DefaultSettingsChildSlot",
        source = dialogNavigation,
        serializer = DialogConfig.serializer(),
        handleBackButton = true,
        childFactory = { _, context ->
            DialogSettingsComponentImpl(
                componentContext = context,
                workspaceSession = workspaceSession,
                onClose = {
                    dialogNavigation.dismiss()
                }
            )
        }
    )
    return DialogScope(
        navigation = dialogNavigation,
        slot = slot
    )
}