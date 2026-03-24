package com.moly3.cedarjam.features.feature_graph.ui.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.model.AppColorsData
import com.moly3.cedarjam.core.domain.model.AppThemeData
import com.moly3.cedarjam.core.domain.model.settings.AppSettings
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalUIConfig
import com.moly3.cedarjam.core.ui.func.flatClickable
import com.moly3.cedarjam.core.ui.func.isCompactUI
import com.moly3.cedarjam.core.ui.func.navigationBarsPaddingCJ
import com.moly3.cedarjam.core.ui.func.statusBarsPaddingCJ
import com.moly3.cedarjam.core.ui.func.windowToolbarPaddingCJ
import com.moly3.cedarjam.core.ui.uikit.CJApplicationTheme
import com.moly3.cedarjam.core.ui.uikit.JustMenuContent
import com.moly3.cedarjam.core.ui.uikit.NeumorphicShape
import vector.BarLeft
import vector.DummySquareSmall

@Composable
fun FileMenuContent(
    modifier: Modifier,
    borderModifier: Modifier = Modifier,
    isOpenedMenu: Boolean,
    openWorkspaceSettings: () -> Unit = {},
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val padding = 8
    Box(
        modifier = modifier
            .windowToolbarPaddingCJ()
            .statusBarsPaddingCJ()
            .navigationBarsPaddingCJ()
            .fillMaxSize()
            .let {
                if (isOpenedMenu) {
                    it.flatClickable {}
                } else {
                    it
                }
            }

    ) {
        if (isOpenedMenu) {
            Column(
                modifier = Modifier
                    .padding(top = padding.dp)
                    .padding(horizontal = padding.dp)
                    .then(borderModifier)
                    .clip(RoundedCornerShape(16.dp))
                    .fillMaxSize()
                    .background(LocalAppTheme.current.colors.backgroundSecondary.copy(alpha = 0.3f))
                    .padding(padding.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    content()
                }
                Column(
                    modifier = Modifier.height(LocalUIConfig.current.fabCircleSize)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isCompactUI()) {
                            NeumorphicShape(
                                modifier = Modifier.size(LocalUIConfig.current.fabCircleSize),
                                isPressed = false,
                                buttonShape = RoundedCornerShape(100.dp),
                                painter = rememberVectorPainter(BarLeft),
                                onClick = openWorkspaceSettings
                            )
                        }
                        Box(Modifier.weight(1f))
                        Box(Modifier.weight(1f))
                        Box(Modifier.size(LocalUIConfig.current.fabCircleSize))
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = padding.dp)
                .padding(padding.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            NeumorphicShape(
                modifier = Modifier.size(LocalUIConfig.current.fabCircleSize),
                isPressed = isOpenedMenu,
                buttonShape = RoundedCornerShape(100.dp),
                painter = rememberVectorPainter(DummySquareSmall),
                onClick = onClick
            )
        }
    }
}




@Preview(widthDp = 275)
@Composable
fun FidgetPoppinPreviewLight() {
    CJApplicationTheme(
        appSettings = AppSettings(AppThemeData.Default.copy(colors = AppColorsData.Light))
    ) {
        var isPressed by remember { mutableStateOf(true) }
        Box(
            Modifier.fillMaxSize().background(Color(0xFF191A1C)),
            contentAlignment = Alignment.Center
        ) {
            FileMenuContent(
                modifier = Modifier.fillMaxSize(),
                isOpenedMenu = isPressed,
                onClick = {
                    isPressed = !isPressed
                }
            )
            JustMenuContent(
                modifier = Modifier,
                canGoForward = true,
                canGoBack = false,
                goBack = {},
                goForward = {},
                openWorkspaceSettings = {})
        }
    }
}

@Preview
@Composable
fun FidgetPoppinPreview2() {
    CJApplicationTheme(
        appSettings = AppSettings(AppThemeData.Default.copy(colors = AppColorsData.Dark))
    ) {
        Box(
            Modifier.fillMaxSize().background(Color(0xFF191A1C)),
            contentAlignment = Alignment.Center
        ) {
            var isPressed by remember { mutableStateOf(false) }
            FileMenuContent(
                modifier = Modifier.fillMaxSize(),
                isOpenedMenu = isPressed,
                onClick = {
                    isPressed = !isPressed
                })
        }
    }
}