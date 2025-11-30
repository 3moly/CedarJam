package com.moly3.cedarjam.navigation

import androidx.compose.runtime.Stable
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner

interface NavigationComponent<Child: Any> : BackHandlerOwner {

    @Stable
    val childStack: Value<ChildStack<*, Child>>
    fun onNavigate(route: Route)
}