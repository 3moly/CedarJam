package com.moly3.cedarjam.features.feature_graph.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.moly3.cedarjam.core.ui.compositions.LocalHazeState
import com.moly3.cedarjam.core.ui.func.navigationBarsPaddingCJ
import com.moly3.cedarjam.core.ui.uikit.NeumorphicShape
import com.moly3.cedarjam.features.feature_graph.IDialogGraphComponent
import com.moly3.cedarjam.features.feature_graph.model.GraphTabState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun ContentNearGraphUI(
    state: GraphTabState,
    mainContent: @Composable BoxScope.() -> Unit,
    dialogSlot: Value<ChildSlot<*, IDialogGraphComponent>>,
    setIsShowGraph: (Boolean) -> Unit,
    optionsAlignment: Alignment = Alignment.BottomEnd
) {
    val hazeState = rememberHazeState(blurEnabled = true)
    Box(modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().hazeSource(hazeState)) {
            mainContent()
        }
        CompositionLocalProvider(LocalHazeState provides hazeState) {
            val dialogSlot by dialogSlot.subscribeAsState()
            dialogSlot.child?.instance?.also {
                DialogGraphUI(
                    graphTabState = state,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.4f)),
                    component = it
                )
            }
            val isGraphDialogOpened = if (dialogSlot.child?.instance != null) {
                val state = dialogSlot.child?.instance?.state?.collectAsState()
                state?.value?.isShowContent == true
            } else
                false
            NeumorphicShape(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .padding(bottom = 8.dp)
                    .navigationBarsPaddingCJ()
                    .align(optionsAlignment)
                    .size(40.dp),
                isPressed = isGraphDialogOpened,
                painter = rememberVectorPainter(vectors.NetworkNode)
            ) {
                setIsShowGraph(!isGraphDialogOpened)
            }
        }
    }
}