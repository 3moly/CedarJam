package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.domain.model.AppColorsData
import com.moly3.cedarjam.core.domain.model.AppThemeData
import com.moly3.cedarjam.core.domain.model.settings.AppSettings
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalIsRelease
import com.moly3.cedarjam.core.ui.compositions.LocalTextStyle
import com.moly3.cedarjam.core.ui.compositions.LocalUIConfig
import com.moly3.cedarjam.core.ui.compositions.UIConfig
import io.github.sudarshanmhasrup.localina.api.LocalinaApp

@Composable
fun CJApplicationTheme(
    appSettings: AppSettings,
    isRelease: Boolean = true,
    content: @Composable () -> Unit
) {
    val textStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = appSettings.theme.colors.primaryFont
    )
    val appTheme: CJAppTheme = remember(appSettings.theme) {
        CJAppTheme(
            colors = appSettings.theme.colors,
            primaryColor = appSettings.theme.primaryColor,
            currentTheme = appSettings.theme.colorsType
        )
    }
    val customTextSelectionColors = TextSelectionColors(
        handleColor = appSettings.theme.primaryColor,
        backgroundColor = appSettings.theme.primaryColor.copy(alpha = 0.4f)
    )
    val config = remember {
        UIConfig(fabCircleSize = 36.dp)
    }
    CompositionLocalProvider(
        LocalIsRelease provides isRelease,
        LocalTextSelectionColors provides customTextSelectionColors,
        LocalAppTheme provides appTheme,
        LocalTextStyle provides textStyle,
        LocalUIConfig provides config
    ) {
        if (LocalInspectionMode.current) {
            content()
        } else {
            LocalinaApp {
                content()
            }
        }
    }
}

@Composable
fun AppThemePreview(
    isFullscreen: Boolean = false,
    isDark: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (isDark)
        AppColorsData.Dark
    else
        AppColorsData.Light
    CJApplicationTheme(
        appSettings = AppSettings(AppThemeData.Default.copy(colors = colors))
    ) {
        Box(
            Modifier.let {
                if (isFullscreen)
                    it.fillMaxSize()
                else {
                    it
                }
            },
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}