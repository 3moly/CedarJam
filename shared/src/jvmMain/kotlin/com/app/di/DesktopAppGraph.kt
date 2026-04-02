package com.app.di

import com.moly3.cedarjam.core.coordinator.di.CoordinatorBindings
import com.moly3.cedarjam.core.net.di.NetworkBindings
import com.moly3.cedarjam.core.storage.di.StorageBindings
import com.moly3.cedarjam.core.ui.service.MacTrackpadGestureService
import com.moly3.cedarjam.di.metro.AppBindings
import com.moly3.cedarjam.di.metro.AppGraph
import com.moly3.cedarjam.navigation.Navigator
import com.moly3.cedarjam.navigation.NavigatorImpl
import com.moly3.cedarjam.navigation.di.NavigationBindings
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraph
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@DependencyGraph(
    scope = AppScope::class,
    bindingContainers = [
        NetworkBindings::class,
        CoordinatorBindings::class,
        StorageBindings::class,
        NavigationBindings::class,
        AppBindings::class
    ],
)
internal interface DesktopAppGraph : AppGraph

fun createDesktopAppGraph(): AppGraph = createGraph<DesktopAppGraph>()

