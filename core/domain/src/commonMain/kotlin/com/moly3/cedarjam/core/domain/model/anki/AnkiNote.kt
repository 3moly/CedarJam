package com.moly3.cedarjam.core.domain.model.anki

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class AnkiNote(
    val id: Long? = null,
    val deckName: String,
    val modelName: String,
    val fields: Map<String, String>,
    val tags: List<String> = emptyList(),
    val oldTags: List<String> = emptyList()
) {
    fun toAnkiJson(includeId: Boolean = false): JsonObject {
        return buildJsonObject {
            if (includeId && id != null) {
                put("id", id)
            }
            put("deckName", deckName)
            put("modelName", modelName)
            put("options", buildJsonObject {
                put("allowDuplicate", false)
                put("duplicateScope", "deck")
                put("duplicateScopeOptions", buildJsonObject {
                    put("deckName", deckName)
                })
            })
            put("fields", buildJsonObject {
                fields.forEach { (key, value) -> put(key, value) }
            })

            // Map the tags list to a JsonArray
            put("tags", buildJsonArray {
                tags.forEach { add(it) }
            })
        }
    }
}