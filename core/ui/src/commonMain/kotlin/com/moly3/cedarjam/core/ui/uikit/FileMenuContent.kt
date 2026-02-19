package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.moly3.cedarjam.core.ui.compositions.LocalUIConfig
import com.moly3.cedarjam.core.ui.func.flatClickable
import com.moly3.cedarjam.core.ui.func.navigationBarsPaddingCJ
import com.moly3.cedarjam.core.ui.func.statusBarsPaddingCJ
import com.moly3.cedarjam.core.ui.func.windowToolbarPaddingCJ
import com.moly3.cedarjam.core.ui.vectors.BarLeft
import com.moly3.cedarjam.core.ui.vectors.DummySquareSmall

@Composable
fun FileMenuContent(
    modifier: Modifier,
    borderModifier: Modifier = Modifier,
    annotationsCount: Int = 0,
    isOpenedMenu: Boolean,
    isIOSwitchPressed: Boolean,
    openWorkspaceSettings: () -> Unit = {},
    onClick: () -> Unit,
    onIOClick: () -> Unit
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
            Box(
                modifier = Modifier
                    .padding(top = padding.dp)
                    .padding(horizontal = padding.dp)
                    .then(borderModifier)
//                    .border(
//                        1.dp,
//                        LocalAppTheme.current.colors.backgroundPrimary,
//                        shape = RoundedCornerShape(16.dp)
//                    )
                    .clip(RoundedCornerShape(16.dp))
                    .fillMaxSize()
//                    .background(LocalAppTheme.current.colors.backgroundSecondary)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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
                    NeumorphicShape(
                        modifier = Modifier.padding(top = 8.dp).fillMaxWidth().height(40.dp),
                        isPressed = false,
                        buttonShape = RoundedCornerShape(100.dp),
                        painter = rememberVectorPainter(DummySquareSmall),
                        content = {
                            CJText(text = "Annotations: ${annotationsCount}")
                        },
                        onClick = {}
                    )

                }
                Column(
                    modifier = Modifier
                        .padding(padding.dp)
                        .align(Alignment.BottomStart)
                ) {
                    NeumorphicShape(
                        modifier = Modifier.size(LocalUIConfig.current.fabCircleSize),
                        isPressed = false,
                        buttonShape = RoundedCornerShape(100.dp),
                        painter = rememberVectorPainter(BarLeft),
                        onClick = openWorkspaceSettings
                    )
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
            CJIOSwitch(
                height = (LocalUIConfig.current.fabCircleSize.value / 2f).toInt(),
                isPressed = isIOSwitchPressed,
                onClick = {
                    onIOClick()
                })
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


@Preview
@Composable
fun FidgetPoppinPreviewLight() {
    CJApplicationTheme(
        appSettings = AppSettings(AppThemeData.Default.copy(colors = AppColorsData.Light))
    ) {
        var isPressed by remember { mutableStateOf(true) }
        var isIOPressed by remember { mutableStateOf(true) }
        Box(
            Modifier.fillMaxSize().background(Color(0xFF191A1C)),
            contentAlignment = Alignment.Center
        ) {
            FileMenuContent(
                modifier = Modifier.fillMaxSize(),
                isIOSwitchPressed = isIOPressed,
                isOpenedMenu = isPressed,
                onClick = {
                    isPressed = !isPressed
                },

                onIOClick = {
                    isIOPressed = !isIOPressed
                })
            JustMenuContent(modifier = Modifier, openWorkspaceSettings = {})
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
            var isIOPressed by remember { mutableStateOf(false) }
            FileMenuContent(
                modifier = Modifier.fillMaxSize(),
                isIOSwitchPressed = isIOPressed,
                isOpenedMenu = isPressed,
                onClick = {
                    isPressed = !isPressed
                },
                onIOClick = {
                    isIOPressed = !isIOPressed
                })
        }
    }
}