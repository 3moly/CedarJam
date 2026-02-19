package com.moly3.cedarjam.core.ui.compositions

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.window.FrameWindowScope

val LocalDecoratedWindowScope = staticCompositionLocalOf<FrameWindowScope> {
    error("DecoratedWindowScope not provided")
}