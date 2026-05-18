package com.moly3.cedarjam.core.data

import co.touchlab.kermit.Logger
import com.moly3.cedarjam.core.domain.model.anki.AnkiNote
import com.moly3.cedarjam.core.domain.repository.IAnkiEnvironment
import com.moly3.cedarjam.core.net.AnkiClient

class AnkiEnvironment : IAnkiEnvironment {

    private val ankiClient by lazy {
        AnkiClient()
    }

    override suspend fun ping(): Boolean {
        return ankiClient.ping()
    }

    override suspend fun importNotes(
        deckName: String,
        notes: List<AnkiNote>
    ): Boolean {
        val pingResult = ping()
        if (!pingResult)
            return false

        try {
            ankiClient.requestPermission()

            try {
                ankiClient.createModels(sourceSupport = true, codeHighlightSupport = false)
            } catch (exc: Exception) {
            }
            ankiClient.createDeck(deckName)

            val existingNotes = ankiClient.notesInfoByDeck(deckName)
            val newCards = notes.map {
                val frontValue = it.fields["Front"]
                val foundExisting =
                    existingNotes?.firstOrNull { d -> d.fields["Front"]?.value == frontValue }
                if (foundExisting != null)
                    it.copy(id = foundExisting.noteId)
                else
                    it
            }
            for (us in newCards.filter { r -> r.id != null }) {
                val noteIds2 = ankiClient.updateNote(us)
            }

            // Add cards to Anki
            val addNotes = newCards.filter { d -> d.id == null }
            val noteIds = ankiClient.addNotes(addNotes)

            return true
        } catch (exc: Exception) {
            Logger.e { "anki import: ${exc}" }
            return false
        }
    }
}