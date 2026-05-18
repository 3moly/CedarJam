package com.moly3.cedarjam.core.ui.func

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun Modifier.flatClickable(
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    onClick: () -> Unit = {}
): Modifier {
    return this.clickable(
        enabled = enabled,
        indication = null, // This removes the ripple effect
        interactionSource = interactionSource
    ) {
        onClick()
    }
}