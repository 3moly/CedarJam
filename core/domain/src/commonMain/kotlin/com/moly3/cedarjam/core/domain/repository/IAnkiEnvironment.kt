package com.moly3.cedarjam.core.domain.repository

import com.moly3.cedarjam.core.domain.model.anki.AnkiNote

interface IAnkiEnvironment {
    suspend fun ping(): Boolean
    suspend fun importNotes(deckName: String, notes: List<AnkiNote>): Boolean
}