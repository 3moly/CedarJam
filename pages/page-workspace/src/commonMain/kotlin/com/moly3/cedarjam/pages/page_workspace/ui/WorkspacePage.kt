package com.moly3.cedarjam.pages.page_workspace.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import coil3.ImageLoader
import coil3.compose.LocalPlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalImageLoader
import com.moly3.cedarjam.core.ui.compositions.LocalWorkspacePath
import com.moly3.cedarjam.core.ui.func.imePaddingCJ
import com.moly3.cedarjam.core.ui.uikit.CJWorkspaceTheme
import com.moly3.cedarjam.features.feature_settings.ui.DialogSettingsUI
import com.moly3.cedarjam.pages.page_workspace.WorkspaceComponent
import com.moly3.cedarjam.pages.page_workspace.ui.internal.WorkspacePageContent
import okio.Path.Companion.toPath
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun WorkspacePage(component: WorkspaceComponent) {
    val state by component.state.collectAsState()
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
    CompositionLocalProvider(
        LocalImageLoader provides imageLoader,
        LocalWorkspacePath provides (state.activeWorkspace?.absolutePath ?: "")
    ) {
        CJWorkspaceTheme(settings = state.settings, file = state.workspaceFont) {
            Box(
                Modifier
                    .background(LocalAppTheme.current.colors.backgroundPrimary)
                    .imePaddingCJ()
            ) {
                WorkspacePageContent(
                    component = component,
                    state = state,
                    onIntent = { component.onIntent(it) }
                )
            }

            val child by component.settingsDialogSlot.subscribeAsState()
            AnimatedVisibility(
                visible = child.child != null,
                enter = slideInHorizontally { it } + fadeIn(),
                exit = slideOutHorizontally { it } + fadeOut()
            ) {
                child.child?.instance?.let {
                    DialogSettingsUI(it)
                }
            }
        }
    }
}