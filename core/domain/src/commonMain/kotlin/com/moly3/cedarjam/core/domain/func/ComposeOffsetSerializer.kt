package com.moly3.cedarjam.core.domain.func

import androidx.compose.ui.geometry.Offset
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ComposeOffsetSerializer : KSerializer<Offset> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ComposeOffset", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Offset) {
        encoder.encodeString("${value.x},${value.y}")
    }

    override fun deserialize(decoder: Decoder): Offset {
        val string = decoder.decodeString()
        val parts = string.split(",")
        return Offset(
            x = parts[0].toFloat(),
            y = parts[1].toFloat()
        )
    }
}