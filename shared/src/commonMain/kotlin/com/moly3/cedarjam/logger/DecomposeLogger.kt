package com.moly3.cedarjam.logger

import co.touchlab.kermit.Logger
import com.moly3.cedarjam.navigation.NavigationComponent
import com.moly3.cedarjam.navigation.NavigationInstance
import com.moly3.cedarjam.navigation.NavigationParent
import com.moly3.cedarjam.navigation.stateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalCoroutinesApi::class)
object DecomposeLogger {


    private fun retrievePage(component: Any): List<StateFlow<*>> {
        val pages = mutableListOf<StateFlow<*>>()
        if (component is NavigationParent) {
            pages.add(component.children.stateFlow)
            val items = component.getItems()
            for (item in items) {
                if(item.instance is NavigationParent){
                    (item.instance as? NavigationParent)?.let {
                        pages.addAll(retrievePage(it))
                    }
                }else{
                    (item.instance as? NavigationInstance)?.let {
                        pages.addAll(retrievePage(it.component))
                    }
                }

            }
        }
        return pages
    }

    fun CoroutineScope.walk(component: Any) {
        if (component is NavigationComponent<*>) {
            val flows = retrievePage(component)
            listenToAll(flows) {
                Logger.w { "onAnyChange" }
            }
        } else {
            throw NullPointerException("component is not ComponentContext")
        }
    }

    fun CoroutineScope.listenToAll(
        flows: List<StateFlow<*>>,
        onAnyChange: () -> Unit
    ) {
        flows
            .map { it.map { Unit } } // ignore value
            .merge()
            .onEach { onAnyChange() }
            .launchIn(this)
    }
}