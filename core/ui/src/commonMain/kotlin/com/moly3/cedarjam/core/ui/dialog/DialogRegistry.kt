package com.moly3.cedarjam.core.ui.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import com.moly3.cedarjam.core.domain.dialog.GlobalDialog
import com.moly3.cedarjam.core.ui.uikit.CJDialogGeneric

class DialogRegistry {
    // Add the extra wildcard for the Service type
    private val bindings = mutableListOf<DialogUIBinding<*, *, *>>()
    private val activeStack = mutableStateListOf<GlobalDialog<*, *>>()

    // The magic happens here: Service : GlobalDialog<Input, Result>
    fun <Service : GlobalDialog<Input, Result>, Input, Result> register(
        service: Service,
        ui: @Composable (Service, Input) -> Unit
    ) {
        bindings.add(DialogUIBinding(service, ui))
    }

    @Composable
    fun Host() {
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

        activeStack.forEachIndexed { index, service ->
            val binding = bindings.find { it.service == service } ?: return@forEachIndexed
            val stackOffset = activeStack.size - 1 - index

            key(service) {
                // Call the helper which recovers the types
                RenderBinding(binding, stackOffset)
            }
        }
    }

    @Composable
    private fun <S : GlobalDialog<I, R>, I, R> RenderBinding(
        binding: DialogUIBinding<S, I, R>,
        stackOffset: Int
    ) {
        CJDialogGeneric(
            dialog = binding.service,
            stackOffset = stackOffset
        ) { input ->
            // Now 'binding.ui' expects 'S' (the specific Service type)
            binding.ui(binding.service, input)
        }
    }
}