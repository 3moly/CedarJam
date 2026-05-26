package com.moly3.cedarjam.shared.di.metro

import com.moly3.cedarjam.core.coordinator.di.CoordinatorBindings
import com.moly3.cedarjam.core.net.di.NetworkBindings
import com.moly3.cedarjam.core.storage.di.StorageBindings
import com.moly3.cedarjam.navigation.di.NavigationBindings
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.createGraph

@DependencyGraph(
    scope = AppScope::class,
    bindingContainers = [
        NetworkBindings::class,
        CoordinatorBindings::class,
        StorageBindings::class,
        NavigationBindings::class,
        AppBindings::class,
        DialogBindings::class,
        DomainBindings::class,
    ],
)
internal interface CedarJamAppGraph : AppGraph

fun createCedarJamAppGraph(): AppGraph = createGraph<CedarJamAppGraph>()
