package com.moly3.app

import JewelDesktop
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.application
import co.touchlab.kermit.Logger
import com.app.di.createDesktopAppGraph
import com.arkivanov.decompose.DecomposeSettings
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import com.badoo.reaktive.coroutinesinterop.asScheduler
import com.badoo.reaktive.scheduler.overrideSchedulers
import com.moly3.app.func.readSerializableContainer
import com.moly3.app.func.writeToFile
import com.moly3.cedarjam.core.domain.model.AndroidApplicationContext
import com.moly3.cedarjam.di.initApp
import com.moly3.cedarjam.di.metro.createRootComponent
import com.moly3.cedarjam.navigation.Root
import com.moly3.cedarjam.navigation.createComponentContext
import dev.datlag.kcef.KCEF
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.filesDir
import kotlinx.coroutines.Dispatchers
import runOnUiThread
import java.io.File

private const val SAVED_STATE_FILE_NAME = "saved_state.dat"

@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalDecomposeApi::class, ExperimentalFoundationApi::class
)
fun main() {
    overrideSchedulers(main = Dispatchers.Main::asScheduler)

    initApp(AndroidApplicationContext())

    DecomposeSettings.update {
        DecomposeSettings.settings.copy(
            duplicateConfigurationsEnabled = true,
            onDecomposeError = {
                Logger.e { "Decompose error: ${it}" }
            })
    }

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

    //    onDestroy = {
    //                saveState()
    //            },

    val appGraph = createDesktopAppGraph()
    val root: Root = runOnUiThread {
        runOnUiThread {
            createRootComponent(
                componentContext = createComponentContext(
                    lifecycle = lifecycle,
                    stateKeeper = stateKeeper,
                    backDispatcher = backDispatcher,
                    onErrorInit = {
                        saveFile.delete()
                        stateKeeper =
                            StateKeeperDispatcher(saveFile.readSerializableContainer())
                        stateKeeper
                    }
                ),
                graph = appGraph,
                onDestroy = {
                    saveState()
                }
            )
        }
    }

//    }

    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        val errorMessage = "💥 Uncaught exception in thread ${thread.name}: ${throwable.message}"
        root.messageService.sendMessage(throwable.toString())
        println(errorMessage)
        throwable.printStackTrace()
        // You can also log to file or show a UI dialog here
    }

    application {
        System.setProperty("apple.awt.application.name", "CedarJam")

        JewelDesktop(
            root = root,
            lifecycle = lifecycle,
            backDispatcher = backDispatcher
        )

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
