import com.moly3.cedarjam.core.domain.io
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class AnkiRequest(
    val action: String,
    val version: Int,
//    val params: Map<String, Any> = emptyMap()
)

@Serializable
data class AnkiResponse(
    val result: String? = null,
    val error: String? = null
)

@Serializable
data class CardTemplate(
    val Name: String,
    val Front: String,
    val Back: String
)

@Serializable
data class ModelParams(
    val modelName: String,
    val inOrderFields: List<String>,
    val css: String,
    val cardTemplates: List<CardTemplate>,
    val isCloze: Boolean = false
)

@Serializable
data class CreateModelAction(
    val action: String = "createModel",
    val params: ModelParams
)

@Serializable
data class MediaFile(
    val filename: String,
    val data: String
)

@Serializable
data class StoreMediaAction(
    val action: String = "storeMediaFile",
    val params: MediaFile
)

@Serializable
data class Card(
    val id: Long? = null,
    val deckName: String,
    val modelName: String,
    val fields: Map<String, String>,
    val tags: List<String> = emptyList(),
    val oldTags: List<String> = emptyList()
) {
    fun getCard(includeId: Boolean = false): Map<String, Any> {
        val note = mutableMapOf<String, Any>(
            "deckName" to deckName,
            "modelName" to modelName,
            "fields" to fields,
            "tags" to tags
        )
        if (includeId && id != null) {
            note["id"] = id
        }
        return note
    }
}

class AnkiClient {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val ankiUrl = "http://127.0.0.1:8765"

    suspend fun createModels(
        sourceSupport: Boolean,
        codeHighlightSupport: Boolean
    ): Any? {
        var models = getModels(sourceSupport, false)
        if (codeHighlightSupport) {
            models = models + getModels(sourceSupport, true)
        }

        return invoke("multi", 6, mapOf("actions" to models))
    }

    suspend fun createDeck(deckName: String): Any? {
        return invoke("createDeck", 6, mapOf("deck" to deckName))
    }

    suspend fun storeMediaFiles(cards: List<Card>): Any? {
        val actions = mutableListOf<Map<String, Any>>()

        for (card in cards) {
            // Assuming you have media files to store
            // You'll need to implement getMedias() method based on your needs
            // actions.addAll(card.getMedias())
        }

        return if (actions.isNotEmpty()) {
            invoke("multi", 6, mapOf("actions" to actions))
        } else {
            emptyMap<String, Any>()
        }
    }

    suspend fun storeCodeHighlightMedias(
        highlightjsBase64: String,
        highlightjsInitBase64: String,
        highlightCssBase64: String
    ): Any? {
        val fileExists = invoke("retrieveMediaFile", 6, mapOf("filename" to "_highlightInit.js"))

        if (fileExists == null) {
            val highlightjs = mapOf(
                "action" to "storeMediaFile",
                "params" to mapOf(
                    "filename" to "_highlight.js",
                    "data" to highlightjsBase64
                )
            )
            val highlightjsInit = mapOf(
                "action" to "storeMediaFile",
                "params" to mapOf(
                    "filename" to "_highlightInit.js",
                    "data" to highlightjsInitBase64
                )
            )
            val highlightCss = mapOf(
                "action" to "storeMediaFile",
                "params" to mapOf(
                    "filename" to "_highlight.css",
                    "data" to highlightCssBase64
                )
            )

            return invoke("multi", 6, mapOf(
                "actions" to listOf(highlightjs, highlightjsInit, highlightCss)
            ))
        }
        return null
    }

    suspend fun addCards(cards: List<Card>): List<Long>? {
        val notes = cards.map { it.getCard(false) }
        return invoke("addNotes", 6, mapOf("notes" to notes)) as? List<Long>
    }

    suspend fun updateCards(cards: List<Card>): Any? {
        val updateActions = mutableListOf<Map<String, Any>>()
        val ids = mutableListOf<Long>()

        for (card in cards) {
            updateActions.add(mapOf(
                "action" to "updateNoteFields",
                "params" to mapOf("note" to card.getCard(true))
            ))

            updateActions.addAll(mergeTags(card.oldTags, card.tags, card.id!!))
            ids.add(card.id)
        }

        // Update deck
        updateActions.add(mapOf(
            "action" to "changeDeck",
            "params" to mapOf(
                "cards" to ids,
                "deck" to cards[0].deckName
            )
        ))

        return invoke("multi", 6, mapOf("actions" to updateActions))
    }

    suspend fun changeDeck(ids: List<Long>, deckName: String): Any? {
        return invoke("changeDeck", 6, mapOf("cards" to ids, "deck" to deckName))
    }

    suspend fun cardsInfo(ids: List<Long>): Any? {
        return invoke("cardsInfo", 6, mapOf("cards" to ids))
    }

    suspend fun getCards(ids: List<Long>): Any? {
        return invoke("notesInfo", 6, mapOf("notes" to ids))
    }

    suspend fun deleteCards(ids: List<Long>): Any? {
        return invoke("deleteNotes", 6, mapOf("notes" to ids))
    }

    suspend fun ping(): Boolean {
        return invoke("version", 6) == 6
    }

    suspend fun requestPermission(): Any? {
        return invoke("requestPermission", 6)
    }

    private fun mergeTags(oldTags: List<String>, newTags: List<String>, cardId: Long): List<Map<String, Any>> {
        val actions = mutableListOf<Map<String, Any>>()
        val oldTagsMutable = oldTags.toMutableList()

        // Find tags to Add
        for (tag in newTags) {
            val index = oldTagsMutable.indexOf(tag)
            if (index > -1) {
                oldTagsMutable.removeAt(index)
            } else {
                actions.add(mapOf(
                    "action" to "addTags",
                    "params" to mapOf(
                        "notes" to listOf(cardId),
                        "tags" to tag
                    )
                ))
            }
        }

        // All Tags to delete
        for (tag in oldTagsMutable) {
            actions.add(mapOf(
                "action" to "removeTags",
                "params" to mapOf(
                    "notes" to listOf(cardId),
                    "tags" to tag
                )
            ))
        }

        return actions
    }

    private suspend fun invoke(action: String, version: Int = 6, params: Map<String, Any> = emptyMap()): Any? {
        return withContext(io) {
            try {
                val url = URL(ankiUrl)
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

//                val requestBody = mapOf(
//                    "action" to action,
//                    "version" to version,
////                    "params" to params
//                )
//
//                val jsonRequest = json.encodeToString(requestBody)
//
//                connection.outputStream.use { outputStream ->
//                    outputStream.write(jsonRequest.toByteArray())
//                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val ankiResponse = json.decodeFromString<Map<String, Any>>(response)

                    if (ankiResponse.containsKey("error") && ankiResponse["error"] != null) {
                        throw Exception("Anki error: ${ankiResponse["error"]}")
                    }

                    return@withContext ankiResponse["result"]
                } else {
                    throw Exception("HTTP error: $responseCode")
                }
            } catch (e: Exception) {
                //throw Exception("Failed to connect to Anki: ${e.message}")
            }
        }
    }

    private fun getModels(sourceSupport: Boolean, codeHighlightSupport: Boolean): List<Map<String, Any>> {
        val sourceFieldContent = if (sourceSupport) "\r\n{{Source}}" else ""
        val codeScriptContent = if (codeHighlightSupport) "\r\n// Code highlighting script\r\n" else ""
        val sourceExtension = if (sourceSupport) "-source" else ""
        val codeExtension = if (codeHighlightSupport) "-code" else ""

        val css = """
            .card {
                fonts-family: arial;
                fonts-size: 20px;
                text-align: center;
                color: black;
                background-color: white;
            }

            .tag::before {
                content: "#";
            }

            .tag {
                color: white;
                background-color: #9F2BFF;
                border: none;
                fonts-size: 11px;
                fonts-weight: bold;
                padding: 1px 8px;
                margin: 0px 3px;
                text-align: center;
                text-decoration: none;
                cursor: pointer;
                border-radius: 14px;
                display: inline;
                vertical-align: middle;
            }
            .cloze { fonts-weight: bold; color: blue;}
            .nightMode .cloze { color: lightblue;}
        """.trimIndent()

        val tagScript = """
            <script>
                var tagEl = document.querySelector('.tags');
                var tags = tagEl.innerHTML.split(' ');
                var html = '';
                tags.forEach(function(tag) {
                    if (tag) {
                        var newTag = '<span class="tag">' + tag + '</span>';
                        html += newTag;
                        tagEl.innerHTML = html;
                    }
                });
            </script>
        """.trimIndent()

        val front = "{{Front}}\r\n<p class=\"tags\">{{Tags}}</p>\r\n$tagScript$codeScriptContent"
        val back = "{{FrontSide}}\n\n<hr id=answer>\n\n{{Back}}$sourceFieldContent"
        val frontReversed = "{{Back}}\r\n<p class=\"tags\">{{Tags}}</p>\r\n$tagScript$codeScriptContent"
        val backReversed = "{{FrontSide}}\n\n<hr id=answer>\n\n{{Front}}$sourceFieldContent"

        var classicFields = listOf("Front", "Back")
        var promptFields = listOf("Prompt")
        var clozeFields = listOf("Text", "Extra")

        if (sourceSupport) {
            classicFields = classicFields + "Source"
            promptFields = promptFields + "Source"
            clozeFields = clozeFields + "Source"
        }

        val basic = mapOf(
            "action" to "createModel",
            "params" to mapOf(
                "modelName" to "Obsidian-basic$sourceExtension$codeExtension",
                "inOrderFields" to classicFields,
                "css" to css,
                "cardTemplates" to listOf(
                    mapOf(
                        "Name" to "Front / Back",
                        "Front" to front,
                        "Back" to back
                    )
                )
            )
        )

        val basicReversed = mapOf(
            "action" to "createModel",
            "params" to mapOf(
                "modelName" to "Obsidian-basic-reversed$sourceExtension$codeExtension",
                "inOrderFields" to classicFields,
                "css" to css,
                "cardTemplates" to listOf(
                    mapOf(
                        "Name" to "Front / Back",
                        "Front" to front,
                        "Back" to back
                    ),
                    mapOf(
                        "Name" to "Back / Front",
                        "Front" to frontReversed,
                        "Back" to backReversed
                    )
                )
            )
        )

        val close = mapOf(
            "action" to "createModel",
            "params" to mapOf(
                "modelName" to "Obsidian-cloze$sourceExtension$codeExtension",
                "inOrderFields" to clozeFields,
                "css" to css,
                "isCloze" to true,
                "cardTemplates" to listOf(
                    mapOf(
                        "Name" to "Cloze",
                        "Front" to "{{cloze:Text}}\n$tagScript$codeScriptContent",
                        "Back" to "{{cloze:Text}}\n\n<br>{{Extra}}$sourceFieldContent$tagScript$codeScriptContent"
                    )
                )
            )
        )

        val spaced = mapOf(
            "action" to "createModel",
            "params" to mapOf(
                "modelName" to "Obsidian-spaced$sourceExtension$codeExtension",
                "inOrderFields" to promptFields,
                "css" to css,
                "cardTemplates" to listOf(
                    mapOf(
                        "Name" to "Spaced",
                        "Front" to "{{Prompt}}\r\n<p class=\"tags\">🧠spaced {{Tags}}</p>\r\n$tagScript$codeScriptContent",
                        "Back" to "{{FrontSide}}\n\n<hr id=answer>🧠 Review done.$sourceFieldContent"
                    )
                )
            )
        )

        return listOf(basic, basicReversed, close, spaced)
    }
}

// Usage example
suspend fun main() {
    val ankiClient = AnkiClient()

    try {
        // Check if Anki-Connect is available
        if (ankiClient.ping()) {
            println("Connected to Anki!")

            // Request permission (first time only)
            ankiClient.requestPermission()

            // Create a deck
            ankiClient.createDeck("My Kotlin Deck")

            // Create models
            ankiClient.createModels(sourceSupport = true, codeHighlightSupport = false)

            // Create some cards
            val cards = listOf(
                Card(
                    deckName = "My Kotlin Deck",
                    modelName = "Obsidian-basic-source",
                    fields = mapOf(
                        "Front" to "What is Kotlin?",
                        "Back" to "A modern programming language for JVM",
                        "Source" to "Kotlin Documentation"
                    ),
                    tags = listOf("kotlin", "programming")
                ),
                Card(
                    deckName = "My Kotlin Deck",
                    modelName = "Obsidian-basic-source",
                    fields = mapOf(
                        "Front" to "What is coroutines?",
                        "Back" to "Asynchronous programming in Kotlin",
                        "Source" to "Kotlin Coroutines Guide"
                    ),
                    tags = listOf("kotlin", "coroutines", "async")
                )
            )

            // Add cards to Anki
            val noteIds = ankiClient.addCards(cards)
            println("Added cards with IDs: $noteIds")

        } else {
            println("Failed to connect to Anki. Make sure Anki is running with Anki-Connect plugin.")
        }
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}