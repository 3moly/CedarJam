package com.moly3.cedarjam.shared.navigation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import com.moly3.cedarjam.shared.di.metro.CedarJamGraph
import com.moly3.cedarjam.shared.di.metro.createRootComponent

fun createRootComponentSafe(
    lifecycle: Lifecycle,
    stateKeeper: StateKeeperDispatcher,
    backDispatcher: BackHandler,
    onDestroy: () -> Unit,
    onErrorInit: (Exception) -> StateKeeperDispatcher
): Root {
    return createRootComponent(
        componentContext = createComponentContext(
            lifecycle = lifecycle,
            stateKeeper = stateKeeper,
            backDispatcher = backDispatcher,
            onErrorInit = onErrorInit
        ),
        graph = CedarJamGraph.instance,
        onDestroy = onDestroy
    )
}