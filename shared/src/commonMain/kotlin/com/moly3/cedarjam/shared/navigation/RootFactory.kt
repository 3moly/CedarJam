package com.moly3.cedarjam.shared.navigation

import com.arkivanov.decompose.ComponentContext
import dev.zacsweers.metro.AssistedFactory

@AssistedFactory
interface RootFactory {
    fun invoke(
        componentContext: ComponentContext,
        onDestroy: () -> Unit = {}
    ): com.moly3.cedarjam.shared.navigation.RootComponent
}