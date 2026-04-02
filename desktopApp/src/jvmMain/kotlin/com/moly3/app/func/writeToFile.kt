package com.moly3.app.func

import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.moly3.cedarjam.core.domain.DefaultJson
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.encodeToStream
import java.io.File

@OptIn(ExperimentalSerializationApi::class)
fun SerializableContainer.writeToFile(file: File) {
    file.outputStream().use { output ->
        DefaultJson.encodeToStream(SerializableContainer.serializer(), this, output)
    }
}