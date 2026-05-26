package com.moly3.cedarjam.shared.di.metro

import com.arkivanov.decompose.ComponentContext
import com.moly3.cedarjam.shared.navigation.Root

fun createRootComponent(
    componentContext: ComponentContext,
    graph: com.moly3.cedarjam.shared.di.metro.AppGraph,
    onDestroy: () -> Unit
): com.moly3.cedarjam.shared.navigation.Root = graph.rootComponentFactory.invoke(componentContext, onDestroy = onDestroy)
