package com.moly3.cedarjam.core.domain.service

import com.moly3.cedarjam.core.domain.model.MidiNote

interface IUtilsService {

    fun openLink(link: String)
    fun readMidi(fileBytes: ByteArray): List<MidiNote>
}