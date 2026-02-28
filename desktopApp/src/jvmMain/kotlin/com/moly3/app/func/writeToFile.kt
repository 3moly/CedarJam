package com.moly3.app.func

import JewelDesktop
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.application
import co.touchlab.kermit.Logger
import com.app.SimpleDesktop
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
import com.moly3.cedarjam.di.initApp
import com.moly3.cedarjam.navigation.Root
import com.moly3.cedarjam.navigation.createRootComponentSafe
import dev.datlag.kcef.KCEF
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.filesDir
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import runOnUiThread
import java.io.File

@OptIn(ExperimentalSerializationApi::class)
fun SerializableContainer.writeToFile(file: File) {
    file.outputStream().use { output ->
        DefaultJson.encodeToStream(SerializableContainer.serializer(), this, output)
    }
}