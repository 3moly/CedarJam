package com.moly3.cedarjam.core.ui.compositions

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import dev.chrisbanes.haze.HazeStyle

val LocalHazeStyle: ProvidableCompositionLocal<HazeStyle> =
    compositionLocalOf { HazeStyle.Unspecified }