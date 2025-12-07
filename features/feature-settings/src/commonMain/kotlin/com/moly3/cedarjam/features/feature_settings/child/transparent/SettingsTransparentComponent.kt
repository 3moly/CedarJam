package com.moly3.cedarjam.features.feature_settings.child.transparent

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.component.KoinComponent

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsTransparentComponent(
    componentContext: ComponentContext
) : ISettingsTransparentComponent,
    ComponentContext by componentContext, KoinComponent