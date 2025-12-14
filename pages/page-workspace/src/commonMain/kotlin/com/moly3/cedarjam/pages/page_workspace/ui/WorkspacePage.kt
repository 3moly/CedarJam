package com.moly3.cedarjam.pages.page_workspace.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.moly3.cedarjam.core.domain.func.hiddenDirectory
import com.moly3.cedarjam.core.domain.func.imgCache
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.shouldBeSuccess
import com.moly3.cedarjam.core.ui.JvmWindowScope
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalImageLoader
import com.moly3.cedarjam.core.ui.uikit.CJWorkspaceTheme
import com.moly3.cedarjam.features.feature_settings.ui.DialogSettingsUI
import com.moly3.cedarjam.pages.page_workspace.WorkspaceComponent
import com.moly3.cedarjam.pages.page_workspace.ui.internal.WorkspacePageContent
import okio.Path.Companion.toPath
import kotlin.time.ExperimentalTime


data class ToolbarState(
    val isFullscreen: Boolean,
    val menuButtonsWidth: Dp,
    val isFirstCut: Boolean,
    val controlsWidthToCut: Dp
)

@OptIn(ExperimentalTime::class)
@Composable
fun JvmWindowScope.WorkspacePage(
    component: WorkspaceComponent,
    titleBarContent: @Composable (@Composable () -> Unit) -> Unit = {}
) {
    val state by component.state.collectAsState()
    val context = LocalPlatformContext.current
    val imageLoader = remember {
//        val absolutePath =
//            component.workspaceSession.workspaceEnvStateFlow.value.getWorkspace().absolutePath
//        val imgCache = pathWrapper(absolutePath, hiddenDirectory, imgCache).pathString
//        val result =
//            component.filesRepository.createDirectory(fullPath = imgCache, isMustCreate = true)
//        result.shouldBeSuccess()

        ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .maxSizeBytes(512L * 1024 * 1024)
                    .build()
            }
            .build()
    }
    CompositionLocalProvider(
        LocalImageLoader provides imageLoader
    ) {
        CJWorkspaceTheme(settings = state.settings, file = state.workspaceFont) {
            Box(
                Modifier
                    .background(LocalAppTheme.current.colors.backgroundPrimary)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                WorkspacePageContent(
                    component = component,
                    state = state,
                    onIntent = {
                        component.onIntent(it)
                    },
                    titleBarContent = titleBarContent
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