import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberTrayState
import androidx.compose.ui.window.rememberWindowState
import co.touchlab.kermit.Logger
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import com.badoo.reaktive.coroutinesinterop.asScheduler
import com.badoo.reaktive.scheduler.overrideSchedulers
import com.moly3.cedarjam.core.domain.DefaultJson
import com.moly3.cedarjam.core.domain.model.AndroidApplicationContext
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.storage.ISystemFilesManager
import com.moly3.cedarjam.core.ui.ToolbarHeight
import com.moly3.cedarjam.core.ui.compositions.LocalJvmToolbarState
import com.moly3.cedarjam.core.ui.model.JvmToolbarState
import com.moly3.cedarjam.core.ui.vectors.Tag
import com.moly3.cedarjam.di.initApp
import com.moly3.cedarjam.navigation.Root
import com.moly3.cedarjam.navigation.createRootComponentSafe
import com.moly3.cedarjam.navigation.ui.ActualPredictiveBackGestureOverlay
import com.moly3.cedarjam.pages.page_workspace.ui.ToolbarState
import com.moly3.cedarjam.ui.MainApp
import dev.datlag.kcef.KCEF
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.filesDir
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
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
import org.koin.mp.KoinPlatform.getKoin
import java.io.File


@OptIn(ExperimentalSerializationApi::class)
fun SerializableContainer.writeToFile(file: File) {
    file.outputStream().use { output ->
        DefaultJson.encodeToStream(SerializableContainer.serializer(), this, output)
    }
}

@OptIn(ExperimentalSerializationApi::class)
fun File.readSerializableContainer(): SerializableContainer? =
    takeIf(File::exists)?.inputStream()?.use { input ->
        try {
            DefaultJson.decodeFromStream(SerializableContainer.serializer(), input)
        } catch (e: Exception) {
            null
        }
    }

private const val SAVED_STATE_FILE_NAME = "saved_state.dat"

@OptIn(
    ExperimentalComposeUiApi::class, ExperimentalJewelApi::class,
    ExperimentalDecomposeApi::class
)
fun main() {
//    ComposeStabilityAnalyzer.setEnabled(false)
//    ComposeStabilityAnalyzer.setLogger(object : RecompositionLogger {
//        override fun log(event: RecompositionEvent) {
//            if (false) {
////                if (event.tag in tagsToLog || event.tag.isEmpty()) {
////                    // Example: Send to Firebase Analytics only log events with specific tags
////                    FirebaseAnalytics.getInstance(this).logEvent("excessive_recomposition") {
////                        param("tag", event.tag)
////                        param("composable", event.composableName)
////                        param("count", event.recompositionCount)
////                        param("unstable_params", event.unstableParameters.joinToString())
////                    }
////                }
//            } else {
//                // Log everything on the debug mode
//                //Logger.e{event.toString()}
//            }
//        }
//    })
    overrideSchedulers(main = Dispatchers.Main::asScheduler)

    initApp(AndroidApplicationContext())

    val appDir = FileKit.filesDir.file
    val saveFile = File(appDir, SAVED_STATE_FILE_NAME)
    var stateKeeper =
        StateKeeperDispatcher(saveFile.readSerializableContainer())

    val lifecycle = LifecycleRegistry()
    val backDispatcher = BackDispatcher()

    fun saveState() {
        try {
            stateKeeper.save().writeToFile(saveFile)
        } catch (exc: Exception) {
            Logger.e(exc.message ?: "")
        }
    }
    //val textMeasurementExecutor = Executors.newSingleThreadExecutor()
    val root: Root = runOnUiThread {
        createRootComponentSafe(
            lifecycle = lifecycle,
            stateKeeper = stateKeeper,
            backDispatcher = backDispatcher,
            onDestroy = {
                saveState()
            },
            onErrorInit = {
                File(SAVED_STATE_FILE_NAME).delete()
                stateKeeper =
                    StateKeeperDispatcher(File(SAVED_STATE_FILE_NAME).readSerializableContainer())
                stateKeeper
            }
        )
    }
//    addTempDirectoryRemovalHook()
//    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
//        val errorMessage = "💥 Uncaught exception in thread ${thread.name}: ${throwable.message}"
//        root.messageService.sendMessage(throwable.toString())
//        println(errorMessage)
//        throwable.printStackTrace()
//        // You can also log to file or show a UI dialog here
//    }
    //LocalWindowExceptionHandlerFactory provides WindowExceptionHandlerFactory { window ->
    //                WindowExceptionHandler { ex ->
    //                    root.messageService.sendMessage(ex.toString())
    ////                    ex.
    ////                    lastException = ex
    ////                    exitApplication()
    //                }
    //            }
//        val fontFamily = FontFamily(listOf(font))
    //                    if (!isFullScreenMode) {
//                        MacWindowControls(windowState = state, onClose = {
//                            exitApplication()
//                        })
//                    }

    application {
        System.setProperty("apple.awt.application.name", "Kotlin Explorer")
//        enableNewSwingCompositing()

        val trayState = rememberTrayState()
        Tray(
            icon = rememberVectorPainter(Tag),
            state = trayState,
            menu = {
                Item("Quit App", onClick = ::exitApplication)
            }
        )

        //val screenSize: Dimension = Toolkit.getDefaultToolkit().screenSize

        val windowState = rememberWindowState(
            placement = WindowPlacement.Floating,
            //size = DpSize(screenSize.width.dp, screenSize.height.dp),
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
            LocalTitleBarStyle provides titleBarStyle
        ) {
            DecoratedWindow(
                state = windowState,
                style = DecoratedWindowStyle.dark(),
                onCloseRequest = { exitApplication() }
            ) {
                val windowInfo = LocalWindowInfo.current
                LifecycleController(
                    lifecycle,
                    windowState,
                    windowInfo = windowInfo
                )

                val toolbarState = remember(state.isFullscreen) {
                    val padding = if (state.isFullscreen) 0.dp else 40.dp

                    val modifierPadding = when (DesktopPlatform.Current) {
                        DesktopPlatform.Linux -> Modifier
                        DesktopPlatform.Windows -> Modifier
                            .padding(start = 70.dp)
                            .padding(end = 70.dp)

                        DesktopPlatform.MacOS -> Modifier.padding(horizontal = padding)
                        DesktopPlatform.Unknown -> Modifier
                    }
                    val isStartCut = when (DesktopPlatform.Current) {
                        DesktopPlatform.Linux -> true
                        DesktopPlatform.Windows -> false
                        DesktopPlatform.MacOS -> true
                        DesktopPlatform.Unknown -> true
                    }
                    val endControlsWidth = when (DesktopPlatform.Current) {
                        DesktopPlatform.Linux -> 0.dp
                        DesktopPlatform.Windows -> 140.dp
                        DesktopPlatform.MacOS -> 80.dp
                        DesktopPlatform.Unknown -> 0.dp
                    }
                    JvmToolbarState(
                        isFullscreen = state.isFullscreen,
                        modifier = modifierPadding,
                        isFirstCut = isStartCut,
                        endControlsWidth = endControlsWidth,
                        controlsWidthToCut = padding * 2f
                    )
                }
                LaunchedEffect(Unit) {
                    addMagnifyListener(window.contentPane) { magnifyValue ->
                        root.shareMagnifyValue(magnifyValue)
                    }
                }

                CompositionLocalProvider(LocalJvmToolbarState provides toolbarState) {
                    ActualPredictiveBackGestureOverlay(
                        backDispatcher = backDispatcher,
                        modifier = Modifier
                    ) {
                        MainApp(root = root) { titleBarContent ->
                            TitleBar(Modifier) { state ->
                                Box(Modifier.then(toolbarState.modifier).fillMaxSize()) {
                                    titleBarContent(
//                                        ToolbarState(
//                                            isFullscreen = state.isFullscreen,
//                                            menuButtonsWidth = toolbarState.controlsWidthToCut,
//                                            isFirstCut = toolbarState.isFirstCut,
//                                            controlsWidthToCut = toolbarState.endControlsWidth
//                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                try {
                    KCEF.disposeBlocking()
                } catch (exc: Exception) {
                }
            }
        }
    }
}
