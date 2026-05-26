package com.moly3.cedarjam.core.ui.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import co.touchlab.kermit.Logger
import com.moly3.cedarjam.core.domain.dialog.DialogState
import com.moly3.cedarjam.core.domain.dialog.GlobalDialog
import com.moly3.cedarjam.core.domain.dialog.IDialogRegister
import com.moly3.cedarjam.core.ui.uikit.CJDialogGeneric
import kotlin.reflect.KClass

typealias ComposableDialogUI = @Composable (GlobalDialog<*, *>, Any?) -> Unit


class DialogRegistry : IDialogRegister {

    val uiFactories = mutableMapOf<KClass<*>, ComposableDialogUI>()

    // The actual stack of active dialog instances
    val activeStack = mutableStateListOf<GlobalDialog<*, *>>()

    // 1. Register how to DRAW a specific TYPE of service
    inline fun <reified S : GlobalDialog<I, R>, I, R> registerUI(
        noinline ui: @Composable (S, I) -> Unit
    ) {
        uiFactories[S::class] = @Composable { service, input ->
            @Suppress("UNCHECKED_CAST")
            ui(service as S, input as I)
        }
    }

    // 2. Add an instance to the stack and start observing its lifecycle
    fun <I, R> attach(service: GlobalDialog<I, R>) {
        if (!activeStack.contains(service)) {
            activeStack.add(service)
        }
    }

    @Composable
    fun Host() {
        activeStack.forEachIndexed { index, service ->
            val uiFactory = uiFactories[service::class] ?: return@forEachIndexed
            val stackOffset = activeStack.size - 1 - index

            val state by service.inputData.collectAsState()

            // Automatically remove from stack when it becomes 'Hidden'
            LaunchedEffect(state) {
                if (state is DialogState.Hidden) {
                    activeStack.remove(service)
                }
            }

            key(service) { // Service instance is the unique key
                CJDialogGeneric(
                    dialog = service,
                    stackOffset = stackOffset
                ) { input ->
                    uiFactory(service, input)
                }
            }
        }
    }

    override fun register(dialog: GlobalDialog<*, *>) {
        attach(dialog)
    }
}