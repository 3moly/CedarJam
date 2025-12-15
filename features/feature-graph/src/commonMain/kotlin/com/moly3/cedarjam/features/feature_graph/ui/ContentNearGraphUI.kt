package com.moly3.cedarjam.features.feature_graph.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.moly3.cedarjam.core.domain.func.getPlatform
import com.moly3.cedarjam.features.feature_graph.IDialogGraphComponent
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.func.rememberWindowSize
import com.moly3.cedarjam.core.ui.model.WindowSize
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun ContentNearGraphUI(
    mainContent: @Composable () -> Unit,
    connectionsCount: Int,
    dialogSlot: Value<ChildSlot<*, IDialogGraphComponent>>,
    setIsShowGraph: (Boolean) -> Unit,
    optionsAlignment: Alignment = Alignment.BottomCenter
) {
    val backgroundSecondary = LocalAppTheme.current.colors.backgroundSecondary
    val hazeState = rememberHazeState(blurEnabled = false)
    val hazeStyle = remember(backgroundSecondary) {
        HazeStyle(
            backgroundColor = backgroundSecondary,
            tints = listOf(HazeTint(backgroundSecondary.copy(0.2f))),
            blurRadius = 16.dp,
            noiseFactor = HazeDefaults.noiseFactor
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().hazeSource(hazeState)) {
            mainContent()
        }
        val dialogSlot by dialogSlot.subscribeAsState()
        dialogSlot.child?.instance?.also {
            DialogGraphUI(
                modifier = Modifier.hazeEffect(state = hazeState, style = hazeStyle)
                    .hazeSource(hazeState, zIndex = 2f),
                component = it
            )
        }
        val isGraphDialogOpened = if (dialogSlot.child?.instance != null) {
            val state = dialogSlot.child?.instance?.state?.collectAsState()
            state?.value?.isShowContent == true
        } else
            false
        val windowSize by rememberWindowSize()
        if (windowSize != WindowSize.Compact) {
            SelectOption(
                modifier = Modifier.align(optionsAlignment).padding(32.dp)
                    .hazeEffect(state = hazeState, style = hazeStyle),
                isOpened = isGraphDialogOpened,
                count = connectionsCount,
                onSetIsShowGraph = { setIsShowGraph(it) })
        }
    }
}