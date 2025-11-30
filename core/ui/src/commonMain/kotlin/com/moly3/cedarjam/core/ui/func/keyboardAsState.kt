package com.moly3.cedarjam.core.ui.func

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.platform.LocalDensity

@Composable
fun keyboardAsState(): State<Boolean> {
    //todo can't do > 0, because there is in ios values 1, or 2 sometimes.
    val sum = WindowInsets.ime.getBottom(LocalDensity.current)
    return derivedStateOf {
        sum > 10
    }
}