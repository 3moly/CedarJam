package com.moly3.cedarjam.shared.di.metro

import com.arkivanov.decompose.ComponentContext
import com.moly3.cedarjam.shared.navigation.Root

fun createRootComponent(
    componentContext: ComponentContext,
    graph: AppGraph,
    onDestroy: () -> Unit
): Root = graph.rootComponentFactory.invoke(componentContext, onDestroy = onDestroy)
