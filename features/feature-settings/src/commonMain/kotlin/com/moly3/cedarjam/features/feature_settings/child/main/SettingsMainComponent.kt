package com.moly3.cedarjam.features.feature_settings.child.main

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.component.KoinComponent

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsMainComponent(
    componentContext: ComponentContext
) : ISettingsMainComponent,
    ComponentContext by componentContext, KoinComponent