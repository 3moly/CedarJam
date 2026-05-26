package com.moly3.cedarjam.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.LocalBackgroundTextMeasurementExecutor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.asEssentyLifecycle
import com.arkivanov.essenty.lifecycle.doOnStop
import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import com.moly3.cedarjam.shared.ui.MainApp
import com.moly3.cedarjam.core.domain.DefaultJson
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.path
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.util.concurrent.Executors

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

class MainActivity : ComponentActivity() {

    private var stateKeeperDispatcher: StateKeeperDispatcher? = null

    private fun getSaveStateFile(): File {
        return File(FileKit.filesDir.path, SAVED_STATE_FILE_NAME)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FileKit.init(this)
        stateKeeperDispatcher =
            StateKeeperDispatcher(getSaveStateFile().readSerializableContainer())


        val essentyLifecycle = lifecycle.asEssentyLifecycle()
        val rootComponent =
            _root_ide_package_.com.moly3.cedarjam.shared.di.metro.createRootComponent(
                componentContext = _root_ide_package_.com.moly3.cedarjam.shared.navigation.createComponentContext(
                    lifecycle = essentyLifecycle,
                    stateKeeper = stateKeeperDispatcher!!,
                    backDispatcher = BackHandler(onBackPressedDispatcher),
                    onErrorInit = {
                        stateKeeperDispatcher = StateKeeperDispatcher(null)
                        stateKeeperDispatcher!!
                    }
                ),
                graph = _root_ide_package_.com.moly3.cedarjam.shared.di.metro.CedarJamGraph.instance,
                onDestroy = {
                    //saveState()
                }
            )
        lifecycle.asEssentyLifecycle().doOnStop {
            try {
                stateKeeperDispatcher?.save()?.writeToFile(getSaveStateFile())
            } catch (exc: Exception) {
            }
        }
        val textMeasurementExecutor = Executors.newSingleThreadExecutor()

        setContent {
            CompositionLocalProvider(
                LocalBackgroundTextMeasurementExecutor provides textMeasurementExecutor
            ) {
                Box(Modifier.fillMaxSize()) {
                    MainApp(root = rootComponent)
                }
            }
        }
    }
}