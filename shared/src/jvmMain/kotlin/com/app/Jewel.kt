import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.moly3.cedarjam.core.ui.ToolbarHeight
import com.moly3.cedarjam.core.ui.compositions.LocalDecoratedWindowScope
import com.moly3.cedarjam.core.ui.compositions.LocalJvmToolbarState
import com.moly3.cedarjam.core.ui.func.bottomNavigationBarPadding
import com.moly3.cedarjam.core.ui.func.topStatusBarPadding
import com.moly3.cedarjam.core.ui.model.JvmToolbarState
import com.moly3.cedarjam.navigation.Root
import com.moly3.cedarjam.navigation.ui.ActualPredictiveBackGestureOverlay
import com.moly3.cedarjam.ui.MainApp
import com.moly3.core_domain.BuildConfig
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.intui.window.styling.dark
import org.jetbrains.jewel.intui.window.styling.defaults
import org.jetbrains.jewel.intui.window.styling.light
import org.jetbrains.jewel.window.DecoratedWindow
import org.jetbrains.jewel.window.TitleBar
import org.jetbrains.jewel.window.styling.DecoratedWindowStyle
import org.jetbrains.jewel.window.styling.LocalTitleBarStyle
import org.jetbrains.jewel.window.styling.TitleBarColors
import org.jetbrains.jewel.window.styling.TitleBarMetrics
import org.jetbrains.jewel.window.styling.TitleBarStyle
import org.jetbrains.jewel.window.utils.DesktopPlatform

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun ApplicationScope.JewelDesktop(
    root: Root,
    lifecycle: LifecycleRegistry,
    backDispatcher: BackDispatcher
) {
    val windowState = rememberWindowState(
        placement = WindowPlacement.Floating,
        position = androidx.compose.ui.window.WindowPosition(0.dp, 0.dp)
    )

    val titleBarStyle = if (true) {
        TitleBarStyle.dark(
            colors = TitleBarColors.dark(
                backgroundColor = Color.Transparent,
                inactiveBackground = Color.Transparent
            ),
            metrics = TitleBarMetrics.defaults(
                height = ToolbarHeight.dp,
                titlePaneButtonSize = DpSize(20.dp, 20.dp)
            )
        )
    } else {
        TitleBarStyle.light()
    }
    CompositionLocalProvider(
        LocalTitleBarStyle provides titleBarStyle,
    ) {
        IntUiTheme {
            DecoratedWindow(
                state = windowState,
                style = DecoratedWindowStyle.dark(),
                onCloseRequest = { exitApplication() }
            ) {
                val isFullScreen = this.state.isFullscreen
                val toolbarState = remember(isFullScreen) {
                    val controlsWidthToCut = if (isFullScreen) {
                        0.dp
                    }else{
                        when(DesktopPlatform.Current){
                            DesktopPlatform.Linux -> 0.dp
                            DesktopPlatform.Windows -> 144.dp
                            DesktopPlatform.MacOS -> 80.dp
                            DesktopPlatform.Unknown -> 0.dp
                        }
                    }

                    val modifierPadding = when (DesktopPlatform.Current) {
                        DesktopPlatform.Linux -> Modifier
                        DesktopPlatform.Windows -> Modifier.padding(end = controlsWidthToCut)
                        DesktopPlatform.MacOS -> Modifier.padding(start = controlsWidthToCut)
                        DesktopPlatform.Unknown -> Modifier
                    }
                    val isStartCut = when (DesktopPlatform.Current) {
                        DesktopPlatform.Linux -> false
                        DesktopPlatform.Windows -> false
                        DesktopPlatform.MacOS -> true
                        DesktopPlatform.Unknown -> true
                    }
                    JvmToolbarState(
                        isFullscreen = isFullScreen,
                        modifier = modifierPadding,
                        isFirstCut = isStartCut,
                        controlsWidthToCut = controlsWidthToCut
                    )
                }
                LaunchedEffect(Unit) {
                    addMagnifyListener(window.contentPane) { magnifyValue ->
                        root.shareMagnifyValue(magnifyValue)
                    }
                }
                val windowInfo = LocalWindowInfo.current
                LifecycleController(
                    lifecycle,
                    windowState,
                    windowInfo = windowInfo
                )

                CompositionLocalProvider(
                    LocalDecoratedWindowScope provides this,
                    LocalJvmToolbarState provides toolbarState
                ) {
                    ActualPredictiveBackGestureOverlay(
                        backDispatcher = backDispatcher,
                        modifier = Modifier
                    ) {
                        TitleBar(Modifier) { state ->
                            Box(Modifier.then(toolbarState.modifier).fillMaxSize()) {
                            }
                        }
                        MainApp(root = root)
                        if (!BuildConfig.IsRelease) {
                            Box(Modifier.fillMaxSize()) {
                                Box(
                                    Modifier.align(Alignment.TopCenter)
                                        .height(topStatusBarPadding.dp).fillMaxWidth()
                                        .background(Color.Red.copy(alpha = 0.3f))
                                )
                                Box(
                                    Modifier.align(Alignment.BottomCenter)
                                        .height(bottomNavigationBarPadding.dp).fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.3f))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}