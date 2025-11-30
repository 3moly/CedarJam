package com.moly3.cedarjam.core.ui.compositions

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import dev.chrisbanes.haze.HazeState

val LocalHazeState: ProvidableCompositionLocal<HazeState> =
    compositionLocalOf { HazeState() }