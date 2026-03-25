package com.moly3.cedarjam.core.ui.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import com.moly3.cedarjam.core.domain.dialog.DialogState
import com.moly3.cedarjam.core.domain.dialog.GlobalDialog
import com.moly3.cedarjam.core.ui.uikit.CJDialogGeneric

class DialogRegistry {
    private val bindings = mutableListOf<DialogUIBinding<*, *>>()

    // This tracks the actual ORDER of opening
    private val activeStack = mutableStateListOf<GlobalDialog<*, *>>()

    fun <Input, Result> register(
        service: GlobalDialog<Input, Result>,
        ui: @Composable (GlobalDialog<Input, Result>, Input?) -> Unit
    ) {
        bindings.add(DialogUIBinding(service, ui))
    }

    @Composable
    fun Host() {
        // 1. Sync the activeStack with the service states
        bindings.forEach { binding ->
            val isOpened by binding.service.isEnabledFlow.collectAsState(false)

            LaunchedEffect(isOpened) {
                if (isOpened) {
                    if (!activeStack.contains(binding.service)) {
                        activeStack.add(binding.service)
                    }
                } else {
                    activeStack.remove(binding.service)
                }
            }
        }

        // 2. Render based on the Stack order, not the registration order
        activeStack.forEachIndexed { index, service ->
            val binding = bindings.find { it.service == service } ?: return@forEachIndexed

            // stackOffset: 0 is the newest (top), 1 is the one below it, etc.
            val stackOffset = activeStack.size - 1 - index

            key(service) {
                RenderBinding(binding, stackOffset)
            }
        }
    }

    @Composable
    private fun <I, R> RenderBinding(binding: DialogUIBinding<I, R>, stackOffset: Int) {
        CJDialogGeneric(
            dialog = binding.service,
            stackOffset = stackOffset // Pass the offset to your UI
        ) { input ->
            binding.ui(binding.service, input)
        }
    }
}