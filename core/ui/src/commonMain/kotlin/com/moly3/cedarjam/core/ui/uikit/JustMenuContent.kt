package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.ui.compositions.LocalUIConfig
import com.moly3.cedarjam.core.ui.func.navigationBarsPaddingCJ
import com.moly3.cedarjam.core.ui.func.statusBarsPaddingCJ
import com.moly3.cedarjam.core.ui.func.windowToolbarPaddingCJ
import vectors.BarLeft

@Composable
fun JustMenuContent(
    modifier: Modifier = Modifier,
    canGoBack: Boolean,
    canGoForward: Boolean,
    goBack: () -> Unit,
    goForward: () -> Unit,
    openWorkspaceSettings: () -> Unit = {},
) {
    val padding = 8
    Box(
        modifier = modifier
            .windowToolbarPaddingCJ()
            .statusBarsPaddingCJ()
            .navigationBarsPaddingCJ()
            .fillMaxSize()
    ) {
        Row(
            Modifier
                .padding(horizontal = (padding * 2).dp)
                .padding(bottom = padding.dp)
                .align(Alignment.BottomStart),
            verticalAlignment = Alignment.Bottom
        ) {
            NeumorphicShape(
                modifier = Modifier.size(LocalUIConfig.current.fabCircleSize),
                isPressed = false,
                buttonShape = RoundedCornerShape(100.dp),
                painter = rememberVectorPainter(BarLeft),
                onClick = openWorkspaceSettings
            )
            Box(Modifier.weight(1f))
            TabNavButts(
                canGoBack = canGoBack,
                canGoForward = canGoForward,
                goForward = { goForward() },
                goBack = { goBack() }
            )
            Box(Modifier.weight(1f))
            Box(modifier = Modifier.size(LocalUIConfig.current.fabCircleSize))
        }
    }
}