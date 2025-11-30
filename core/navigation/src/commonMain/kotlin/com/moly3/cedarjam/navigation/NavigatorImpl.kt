package com.moly3.cedarjam.navigation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class NavigatorImpl(val scope: CoroutineScope) : Navigator, NavigatorDispatcher {

    override val events: MutableSharedFlow<Route> = MutableSharedFlow()

    override fun navigate(route: Route) {
        scope.launch(Dispatchers.Main.immediate) {
            events.emit(route)
        }
    }
}