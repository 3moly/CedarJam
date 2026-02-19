package com.moly3.cedarjam.navigation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher

fun createRootComponentSafe(
    lifecycle: Lifecycle,
    stateKeeper: StateKeeperDispatcher,
    backDispatcher: BackHandler,
    onDestroy: () -> Unit,
    onErrorInit: (Exception) -> StateKeeperDispatcher
): RootComponent {
    return try {
        RootComponent(
            DefaultComponentContext(
                lifecycle = lifecycle,
                stateKeeper = stateKeeper,
                backHandler = backDispatcher
            ),
            onDestroy = onDestroy
        )
    } catch (exc: Exception) {
        RootComponent(
            DefaultComponentContext(
                lifecycle = lifecycle,
                stateKeeper = onErrorInit(exc),
                backHandler = backDispatcher
            ),
            onDestroy = onDestroy
        )
    }
}