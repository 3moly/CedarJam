package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.domain.func.getPlatform
import com.moly3.cedarjam.core.domain.model.settings.AppSettings
import com.moly3.cedarjam.core.domain.model.Platform
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalTextStyle
import io.github.sudarshanmhasrup.localina.api.LocalinaApp

@Composable
fun CJApplicationTheme(
    appSettings: AppSettings,
    content: @Composable () -> Unit
) {
    val textStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color =  appSettings.theme.colors.primaryFont
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
    CompositionLocalProvider(
        LocalTextSelectionColors provides customTextSelectionColors,
        LocalAppTheme provides appTheme,
        LocalTextStyle provides textStyle
    ) {
        LocalinaApp {
            content()
        }
    }
}