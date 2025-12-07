package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Logger
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.settings.WorkspaceFont
import com.moly3.cedarjam.core.domain.model.settings.WorkspaceSettings
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalTextStyle
import com.moly3.cedarjam.core.ui.func.changeLanguage
import com.moly3.cedarjam.core.ui.func.getFontByPath
import kotlinx.coroutines.launch

@Composable
fun CJWorkspaceTheme(
    settings: WorkspaceSettings,
    file: WorkspaceFont?,
    content: @Composable () -> Unit
) {
    LaunchedEffect(settings.language) {
        changeLanguage(settings.language ?: "en")
    }
    var textStyleState by remember { mutableStateOf<FontFamily>(FontFamily.Default) }
    val textStyle = remember(
        textStyleState,
        file?.timestamp,
        settings.theme.colors.primaryFont
    ) {
        TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = settings.theme.colors.primaryFont,
            fontFamily = textStyleState
        )
    }
    LaunchedEffect(file, settings.theme.colors.primaryFont) {
        launch(io) {
            val font = file
            textStyleState = if (font != null) {
                FontFamily(getFontByPath(font.font.getFullPath(), key = font.timestamp.toString()))
            } else
                FontFamily.Default
        }
    }
    val appTheme: CJAppTheme = remember(settings.theme) {
        CJAppTheme(
            colors = settings.theme.colors,
            primaryColor = settings.theme.primaryColor,
            currentTheme = settings.theme.colorsType
        )
    }
    val customTextSelectionColors = TextSelectionColors(
        handleColor = settings.theme.primaryColor,
        backgroundColor = settings.theme.primaryColor.copy(alpha = 0.4f)
    )
    CompositionLocalProvider(
        LocalTextSelectionColors provides customTextSelectionColors,
        LocalAppTheme provides appTheme,
        LocalTextStyle provides textStyle
    ) {
        content()
    }
}