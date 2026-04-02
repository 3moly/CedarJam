package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Logger
import coil3.ImageLoader
import coil3.compose.LocalPlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.settings.WorkspaceFont
import com.moly3.cedarjam.core.domain.model.settings.WorkspaceSettings
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalImageLoader
import com.moly3.cedarjam.core.ui.compositions.LocalSystemDensity
import com.moly3.cedarjam.core.ui.compositions.LocalTextStyle
import com.moly3.cedarjam.core.ui.compositions.LocalWorkspacePath
import com.moly3.cedarjam.core.ui.func.changeLanguage
import com.moly3.cedarjam.core.ui.func.getFontByPath
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath

@Composable
fun CJWorkspaceTheme(
    settings: WorkspaceSettings,
    file: WorkspaceFont?,
    workspaceFullPath: String?,
    content: @Composable () -> Unit
) {
    val context = LocalPlatformContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory("/Users/new07/Desktop/images_Cache/".toPath())
                    .maxSizeBytes(512L * 1024 * 1024)
                    .build()
            }
            .build()
    }
    setSingletonImageLoaderFactory {
        imageLoader
    }
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
            fontSize = 14.sp,
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
    val density by remember(settings) {
        derivedStateOf {
            Density(settings.density.coerceIn(0.75f..3f), settings.fontScale.coerceIn(0.75f..3f))
        }
    }
    val customTextSelectionColors = TextSelectionColors(
        handleColor = settings.theme.primaryColor,
        backgroundColor = settings.theme.primaryColor.copy(alpha = 0.4f)
    )
    val systemDensity = LocalDensity.current
    CompositionLocalProvider(
        LocalTextSelectionColors provides customTextSelectionColors,
        LocalAppTheme provides appTheme,
        LocalTextStyle provides textStyle,
        LocalDensity provides Density(
            density = density.density * systemDensity.density,
            fontScale = density.fontScale * systemDensity.fontScale
        ),
        LocalSystemDensity provides systemDensity,
        LocalImageLoader provides imageLoader,
        LocalWorkspacePath provides (workspaceFullPath ?: "")
    ) {
        content()
    }
}