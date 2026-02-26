package com.moly3.cedarjam.features.feature_graph.ui.internal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalHazeState
import com.moly3.cedarjam.core.ui.compositions.LocalTextStyle
import com.moly3.cedarjam.core.ui.uikit.ButtSnapType
import com.moly3.cedarjam.core.ui.uikit.CJButtSnap
import com.moly3.cedarjam.core.ui.uikit.CJSlider
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.FileMenuContent
import com.moly3.cedarjam.core.ui.uikit.NeumorphicShape
import com.moly3.cedarjam.features.feature_graph.Intent
import com.moly3.cedarjam.features.feature_graph.State
import com.moly3.cedarjam.features.feature_graph.model.GraphPage
import com.moly3.cedarjam.features.feature_graph.ui.internal.graphPages.GraphPage
import com.moly3.dataviz.core.graph.model.GraphViewSettings
import com.moly3.dataviz.graph.ui.Graph
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.rememberHazeState
import vectors.DummySquareSmall

@Composable
internal fun DialogGraphUIContent(
    modifier: Modifier,
    targetId: String?,
    state: State,
    onIntent: (Intent) -> Unit
) {

    val backgroundSecondary = LocalAppTheme.current.colors.backgroundSecondary
    val hazeStyle = remember(backgroundSecondary) {
        HazeStyle(
            backgroundColor = backgroundSecondary,
            tints = listOf(HazeTint(backgroundSecondary.copy(0.2f))),
            blurRadius = 16.dp,
            noiseFactor = HazeDefaults.noiseFactor
        )
    }
    FileMenuContent(
        modifier = Modifier
            .safeDrawingPadding()
            .fillMaxSize(),
        borderModifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .hazeEffect(LocalHazeState.current, hazeStyle),
        isOpenedMenu = state.isShowContent,
        openWorkspaceSettings = {
            onIntent(Intent.OpenWorkspaceSettings)
            // onIntent(com.moly3.cedarjam.pages.page_collection.Intent.OpenWorkspaceSettings)
        },
        onClick = {
            //isPressed = !isPressed
        },
        content = {
            Column(modifier = Modifier.fillMaxWidth()) {

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NeumorphicShape(
                        modifier = Modifier.weight(1f).height(40.dp),
                        isPressed = true,
                        buttonShape = RoundedCornerShape(100.dp),
                        painter = rememberVectorPainter(DummySquareSmall),
                        content = {
                            CJText(text = "ssdsdsd")
                        },
                        onClick = {}
                    )
                    NeumorphicShape(
                        modifier = Modifier.size(40.dp),
                        isPressed = true,
                        buttonShape = RoundedCornerShape(100.dp),
                        painter = rememberVectorPainter(DummySquareSmall),
                        content = {
                            CJText(text = "pdf")
                        },
                        onClick = {}
                    )
                }
            }
            Box(Modifier.weight(1f)) {
                when (state.currentPage) {
                    GraphPage.General -> {

                    }

                    GraphPage.Annotations -> {}

                    GraphPage.Graph -> GraphPage(
                        targetId = targetId,
                        state = state,
                        onIntent = onIntent
                    )

                    GraphPage.Tags -> {}
                }
            }
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                CJButtSnap(
                    painter = rememberVectorPainter(vectors.BarLeft),
                    buttType = ButtSnapType.Left,
                    isSelected = state.currentPage == GraphPage.General,
                ) {
                    onIntent(Intent.SetCurrentPage(GraphPage.General))
                }
                CJButtSnap(
                    modifier = Modifier.weight(1f),
                    painter = rememberVectorPainter(vectors.BarLeft),
                    buttType = ButtSnapType.Center,
                    isSelected = state.currentPage == GraphPage.Tags,
                ) {
                    onIntent(Intent.SetCurrentPage(GraphPage.Tags))
                }
                CJButtSnap(
                    painter = rememberVectorPainter(vectors.NetworkNode),
                    buttType = ButtSnapType.Right,
                    isSelected = state.currentPage == GraphPage.Graph,
                ) {
                    onIntent(Intent.SetCurrentPage(GraphPage.Graph))
                }
            }
        }
    )
}