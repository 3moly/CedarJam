package com.moly3.cedarjam.repository

import com.moly3.cedarjam.core.domain.model.MidiNote
import com.moly3.cedarjam.core.domain.service.IUtilsService

actual fun getUtilsService(): IUtilsService {
    return object : IUtilsService {
        override fun openLink(link: String) {

        }

        override fun readMidi(fileBytes: ByteArray): List<MidiNote> {
            return listOf()
        }
    }
}