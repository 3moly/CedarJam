package com.moly3.cedarjam.features.feature_settings.func

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.dialog.DialogColorPickerService
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.domain.usecase.ISyncUseCase
import com.moly3.cedarjam.features.feature_settings.DialogSettingsComponentImpl
import com.moly3.cedarjam.features.feature_settings.model.DialogConfig
import com.moly3.cedarjam.features.feature_settings.model.DialogScope

fun ComponentContext.settingsDialogScopeFactory(
    storeFactory: StoreFactory,
    workspaceSession: WorkspaceSession,
    dialogColorPickerService: DialogColorPickerService,
    systemFilesManager: IFilesRepository,
    syncUseCase: ISyncUseCase,
): DialogScope {
    val dialogNavigation = SlotNavigation<DialogConfig>()
    val slot = childSlot(
        key = "DefaultSettingsChildSlot",
        source = dialogNavigation,
        serializer = DialogConfig.serializer(),
        handleBackButton = true,
        childFactory = { _, context ->
            DialogSettingsComponentImpl(
                storeFactory = storeFactory,
                componentContext = context,
                workspaceSession = workspaceSession,
                dialogColorPickerService = dialogColorPickerService,
                systemFilesManager = systemFilesManager,
                syncUseCase = syncUseCase,
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
