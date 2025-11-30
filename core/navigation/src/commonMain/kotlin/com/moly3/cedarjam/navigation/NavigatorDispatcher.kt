package com.moly3.cedarjam.navigation

import kotlinx.coroutines.flow.MutableSharedFlow

interface NavigatorDispatcher {
    val events: MutableSharedFlow<Route>
}