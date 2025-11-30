package com.moly3.cedarjam.core.domain.model

data class MidiNote(val tick: Long, val note: Int, val velocity: Int, val isNoteOn: Boolean)