package com.moly3.cedarjam.repository

import com.moly3.cedarjam.core.domain.model.MidiNote
import com.moly3.cedarjam.core.domain.service.IUtilsService
import java.awt.Desktop
import java.net.URI

actual fun getUtilsService(): IUtilsService {
    return object: IUtilsService{
        override fun openLink(link: String) {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop()
                    .isSupported(Desktop.Action.BROWSE)
            ) {
                Desktop.getDesktop().browse(URI(link))
            }
        }

        override fun readMidi(fileBytes: ByteArray): List<MidiNote> {
            val result = mutableListOf<MidiNote>()
            val stream = fileBytes.inputStream()
            val sequence = javax.sound.midi.MidiSystem.getSequence(stream)

            for (track in sequence.tracks) {
                for (i in 0 until track.size()) {
                    val event = track.get(i)
                    val message = event.message
                    if (message is javax.sound.midi.ShortMessage) {
                        val tick = event.tick
                        val note = message.data1
                        val velocity = message.data2
                        val isOn = message.command == javax.sound.midi.ShortMessage.NOTE_ON && velocity > 0
                        result.add(MidiNote(tick, note, velocity, isOn))
                    }
                }
            }
            return result
        }
    }
}