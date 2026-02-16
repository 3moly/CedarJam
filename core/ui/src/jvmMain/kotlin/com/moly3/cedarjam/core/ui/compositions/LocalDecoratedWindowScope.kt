package com.moly3.cedarjam.core.ui.compositions

import androidx.compose.runtime.staticCompositionLocalOf
import org.jetbrains.jewel.window.DecoratedWindowScope

val LocalDecoratedWindowScope = staticCompositionLocalOf<DecoratedWindowScope> {
    error("DecoratedWindowScope not provided")
}