package com.moly3.cedarjam.navigation

import co.touchlab.kermit.Logger
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnStart
import com.arkivanov.essenty.lifecycle.doOnStop
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

fun Lifecycle.subToLog(key:String){
    this.subscribe(object : Lifecycle.Callbacks {
        override fun onCreate() {
            Logger.w { "$key lifecycle onCreate" }
        }
        override fun onStart() {
            Logger.w { "$key lifecycle onStart" }
        }
        override fun onStop() {
            Logger.w { "$key lifecycle onStop" }
        }
        override fun onDestroy() {
            Logger.w { "$key lifecycle onDestroy" }
        }
        override fun onPause() {
            Logger.w { "$key lifecycle onPause" }
        }
        override fun onResume() {
            Logger.w { "$key lifecycle onResume" }
        }
    })
}

open abstract class BaseExecutor<in Intent : Any, Action : Any, State : Any, Message : Any, Label : Any>(
    private val lifecycle: Lifecycle
) : CoroutineExecutor<Intent, Action, State, Message, Label>() {

    private var _scopeFromStartFromStop: CoroutineScope? = null

    open fun onStart(scopeFromStartToStop: CoroutineScope) {}

    override fun executeAction(action: Action) {
        super.executeAction(action)

        lifecycle.doOnStart {
            _scopeFromStartFromStop?.cancel()
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
            _scopeFromStartFromStop = scope
            onStart(scope)
        }
        lifecycle.doOnStop {
            _scopeFromStartFromStop?.cancel()
            _scopeFromStartFromStop = null
        }
    }
}