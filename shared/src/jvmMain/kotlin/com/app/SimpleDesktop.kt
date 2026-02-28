package com.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.awt.SwingWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.WindowDecoration
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.rememberWindowState
import com.app.desktop.CedarJamWindowMeasurePolicy
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.moly3.cedarjam.core.ui.compositions.LocalDecoratedWindowScope
import com.moly3.cedarjam.core.ui.func.bottomNavigationBarPadding
import com.moly3.cedarjam.core.ui.func.topStatusBarPadding
import com.moly3.cedarjam.navigation.Root
import com.moly3.cedarjam.navigation.ui.ActualPredictiveBackGestureOverlay
import com.moly3.cedarjam.ui.MainApp
import com.moly3.core_domain.BuildConfig
import org.jetbrains.jewel.foundation.LocalComponent
import org.jetbrains.jewel.foundation.Stroke
import org.jetbrains.jewel.foundation.modifier.border
import org.jetbrains.jewel.foundation.modifier.trackWindowActivation
import org.jetbrains.jewel.window.DecoratedWindowScope
import org.jetbrains.jewel.window.DecoratedWindowState
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JComponent

@OptIn(ExperimentalDecomposeApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ApplicationScope.SimpleDesktop(
    root: Root,
    lifecycle: LifecycleRegistry,
    backDispatcher: BackDispatcher
) {
    val windowState = rememberWindowState()
    SwingWindow(
        onCloseRequest = {},
        init = {},
        title = "CedarJam",
        state = windowState,
        decoration = WindowDecoration.Undecorated()
//        create = {
//            val window = ComposeWindow(
//                graphicsConfiguration = null,
//                skiaLayerAnalytics = SkiaLayerAnalytics.Empty
//            )
//            window.isUndecorated = true
//            window.opacity = 0.5f
//            window
//        },
//        dispose = {}
    ) {
        var decoratedWindowState by remember { mutableStateOf(DecoratedWindowState.of(window)) }

        DisposableEffect(window) {
            val adapter =
                object : WindowAdapter(), ComponentListener {
                    override fun windowActivated(e: WindowEvent?) {
                        decoratedWindowState = DecoratedWindowState.of(window)
                    }

                    override fun windowDeactivated(e: WindowEvent?) {
                        decoratedWindowState = DecoratedWindowState.of(window)
                    }

                    override fun windowIconified(e: WindowEvent?) {
                        decoratedWindowState = DecoratedWindowState.of(window)
                    }

                    override fun windowDeiconified(e: WindowEvent?) {
                        decoratedWindowState = DecoratedWindowState.of(window)
                    }

                    override fun windowStateChanged(e: WindowEvent) {
                        decoratedWindowState = DecoratedWindowState.of(window)
                    }

                    override fun componentResized(e: ComponentEvent?) {
                        decoratedWindowState = DecoratedWindowState.of(window)
                    }

                    override fun componentMoved(e: ComponentEvent?) {
                        // Empty
                    }

                    override fun componentShown(e: ComponentEvent?) {
                        // Empty
                    }

                    override fun componentHidden(e: ComponentEvent?) {
                        // Empty
                    }
                }

            window.addWindowListener(adapter)
            window.addWindowStateListener(adapter)
            window.addComponentListener(adapter)

            onDispose {
                window.removeWindowListener(adapter)
                window.removeWindowStateListener(adapter)
                window.removeComponentListener(adapter)
            }
        }

        CompositionLocalProvider(
            LocalDecoratedWindowScope provides this
        ){
            val windowInfo = LocalWindowInfo.current
            LifecycleController(
                lifecycle,
                windowState,
                windowInfo = windowInfo
            )
            ActualPredictiveBackGestureOverlay(
                backDispatcher = backDispatcher,
                modifier = Modifier
            ) {
                val undecoratedWindowBorder =
                    if (true && !decoratedWindowState.isMaximized) {
                        Modifier.border(
                            Stroke.Alignment.Inside,
                            1.dp,
                            Color.Red,
                            RectangleShape,
                        )
                            .padding(40.dp)
                    } else {
                        Modifier
                    }
                val currentComponent = remember(window) { window.contentPane.components.filterIsInstance<JComponent>().first() }

                CompositionLocalProvider(
                    LocalComponent provides currentComponent,
//                    LocalTitleBarInfo provides TitleBarInfo(title, icon),
                ) {
                    Layout(
                        content = {
                            val scope =
                                object : DecoratedWindowScope {
                                    override val state: DecoratedWindowState
                                        get() = decoratedWindowState

                                    override val window: ComposeWindow
                                        get() = this@SwingWindow.window
                                }
                            key(scope){
                                MainApp(root = root)
                                Row(Modifier.height(30.dp)){
                                    Box(Modifier.size(30.dp).background(Color.Red)){

                                    }
                                    Box(Modifier.size(30.dp).background(Color.Yellow).clickable{
                                        windowState.placement = WindowPlacement.Fullscreen
                                    }){

                                    }
                                }
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

                        },
                        modifier = undecoratedWindowBorder.trackWindowActivation(window),
                        measurePolicy = CedarJamWindowMeasurePolicy,
                    )
                }

            }
        }
    }
}