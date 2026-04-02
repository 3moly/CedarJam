package com.moly3.cedarjam.navigation

import com.arkivanov.decompose.ComponentContext
import dev.zacsweers.metro.AssistedFactory

@AssistedFactory
interface RootFactory {
    fun invoke(
        componentContext: ComponentContext,
        onDestroy: () -> Unit = {}
    ): RootComponent
}