package com.moly3.cedarjam.shared.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher

fun createComponentContext(
    lifecycle: Lifecycle,
    stateKeeper: StateKeeperDispatcher,
    backDispatcher: BackHandler,
    onErrorInit: (Exception) -> StateKeeperDispatcher
): ComponentContext {
    return try {
        DefaultComponentContext(
            lifecycle = lifecycle,
            stateKeeper = stateKeeper,
            backHandler = backDispatcher
        )
    } catch (exc: Exception) {
        DefaultComponentContext(
            lifecycle = lifecycle,
            stateKeeper = onErrorInit(exc),
            backHandler = backDispatcher
        )
    }
}