import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.application
import co.touchlab.kermit.Logger
import com.arkivanov.decompose.DecomposeSettings
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import com.badoo.reaktive.coroutinesinterop.asScheduler
import com.badoo.reaktive.scheduler.overrideSchedulers
import com.moly3.cedarjam.core.domain.DefaultJson
import com.moly3.cedarjam.core.domain.model.AndroidApplicationContext
import com.moly3.cedarjam.core.ui.compositions.LocalIsRelease
import com.moly3.cedarjam.di.initApp
import com.moly3.cedarjam.logger.DecomposeLogger
import com.moly3.cedarjam.logger.DecomposeLogger.walk
import com.moly3.cedarjam.navigation.Root
import com.moly3.cedarjam.navigation.createRootComponentSafe
import dev.datlag.kcef.KCEF
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.filesDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
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
    ExperimentalComposeUiApi::class,
    ExperimentalDecomposeApi::class, ExperimentalFoundationApi::class
)
fun main() {
    overrideSchedulers(main = Dispatchers.Main::asScheduler)

    initApp(AndroidApplicationContext())

    DecomposeSettings.update {
        DecomposeSettings.settings.copy(
            duplicateConfigurationsEnabled = false,
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

    val root: Root = runOnUiThread {
        createRootComponentSafe(
            lifecycle = lifecycle,
            stateKeeper = stateKeeper,
            backDispatcher = backDispatcher,
            onDestroy = {
                saveState()
            },
            onErrorInit = {
                //File(SAVED_STATE_FILE_NAME).delete()
                stateKeeper =
                    StateKeeperDispatcher(File(SAVED_STATE_FILE_NAME).readSerializableContainer())
                stateKeeper
            }
        )
    }
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        val errorMessage = "💥 Uncaught exception in thread ${thread.name}: ${throwable.message}"
        root.messageService.sendMessage(throwable.toString())
        println(errorMessage)
        throwable.printStackTrace()
        // You can also log to file or show a UI dialog here
    }

    application {
//        LaunchedEffect(Unit){
//            launch {
//                this.walk(root)
//            }
//        }
        System.setProperty("apple.awt.application.name", "CedarJam")



        JewelDesktop(
            root = root,
            lifecycle = lifecycle,
            backDispatcher = backDispatcher
        )
//        val trayState = rememberTrayState()
//        Tray(
//            icon = rememberVectorPainter(Tag),
//            state = trayState,
//            menu = {
//                Item("Quit App", onClick = ::exitApplication)
//            }
//        )


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
