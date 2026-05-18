package com.moly3.cedarjam.features.feature_settings.model

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.value.Value
import com.moly3.cedarjam.features.feature_settings.IDialogSettingsComponent

data class DialogScope(
    val navigation: SlotNavigation<DialogConfig>,
    val slot: Value<ChildSlot<DialogConfig, IDialogSettingsComponent>>
)