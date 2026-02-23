package com.moly3.cedarjam.ui.pages.tag.ui.internal

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.ui.pages.tag.Intent
import com.moly3.cedarjam.ui.pages.tag.State
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJCircularProgressIndicator
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.CJGraphPresentationUI
import com.moly3.cedarjam.core.ui.uikit.FileMenuContent
import com.moly3.cedarjam.core.ui.vectors.SquareHelp
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
@Composable
internal fun PageContent(
    state: State,
    onIntent: (Intent) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var isIOPressed by remember { mutableStateOf(false) }

    val appTheme = LocalAppTheme.current
    val hazeState = rememberHazeState(blurEnabled = true)
    val hazeStyle = remember(appTheme) {
        HazeStyle(
            backgroundColor = appTheme.colors.backgroundSecondary.copy(alpha = 0.8f),
            tints = listOf(HazeTint(appTheme.colors.backgroundSecondary.copy(0.5f))),
            blurRadius = 16.dp,
            noiseFactor = HazeDefaults.noiseFactor
        )
    }
    when (state.tagState) {
        is UIState.Error<*> -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painter = rememberVectorPainter(SquareHelp), contentDescription = null)
                }
            }
        }

        UIState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CJCircularProgressIndicator()
            }
        }

        is UIState.Success -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item("top_block") {
                    CJText(text = state.tagState.data.name)

                    CJButton(text = "Link tag to tag") {
                        onIntent(Intent.SetNewTag)
                    }
                    CJText(
                        modifier = Modifier.padding(top = 12.dp),
                        text = "# linked connections",
                        fontSize = 16.sp
                    )
                }

                for (file in state.connections) {
                    item(file.toString()) {
                        CJGraphPresentationUI(
                            modifier = Modifier,
                            data = file,
                            onClick = {
                                onIntent(Intent.OpenLink(file))
                            },
                            onDelete = {
                                onIntent(Intent.DeleteLink(file))
                            })
                    }
                }
            }
        }
    }
    FileMenuContent(
        modifier = Modifier.safeDrawingPadding().fillMaxSize(),
        borderModifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .hazeEffect(hazeState, hazeStyle)
        ,
        annotationsCount = 0,
        isIOSwitchPressed = isIOPressed,
        isOpenedMenu = isPressed,
        openWorkspaceSettings = {
            onIntent(Intent.OpenWorkspaceSettings)
        },
        onIOClick = {
            isIOPressed = !isIOPressed
        },
        onClick = {
            isPressed = !isPressed
        }
    )
}