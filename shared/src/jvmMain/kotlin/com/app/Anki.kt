import com.moly3.cedarjam.core.domain.io
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import java.net.HttpURLConnection
import java.net.URL

//@Serializable
//data class AnkiRequest(
//    val action: String,
//    val version: Int,
////    val params: Map<String, Any> = emptyMap()
//)

//@Serializable
//data class AnkiResponse(
//    val result: String? = null,
//    val error: String? = null
//)

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
    fun toAnkiJson(includeId: Boolean = false): JsonObject {
        return buildJsonObject {
            if (includeId && id != null) {
                put("id", id)
            }
            put("deckName", deckName)
            put("modelName", modelName)

            // Map the fields dictionary to a JsonObject
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

@Serializable
data class AnkiRequest(
    val action: String,
    val version: Int = 6,
    val params: JsonObject = buildJsonObject { }
)

@Serializable
data class AnkiResponse(
    val result: JsonElement? = null, // JsonElement handles any dynamic JSON value
    val error: String? = null
)

class AnkiClient {

    private val ankiUrl = "http://127.0.0.1:8765"

    suspend fun createModels(
        sourceSupport: Boolean,
        codeHighlightSupport: Boolean
    ): Any? {
        val modelActions = getModels(sourceSupport, codeHighlightSupport)

        val params = buildJsonObject {
            put("actions", JsonArray(modelActions))
        }
        return invoke("multi", params)
    }

    suspend fun createDeck(deckName: String): Long? {
        val myParams = buildJsonObject {
            put("deck", deckName)
        }
        return invoke("createDeck", myParams)
    }

//    suspend fun storeMediaFiles(cards: List<Card>): Any? {
//        val actions = mutableListOf<Map<String, Any>>()
//
//        for (card in cards) {
//            // Assuming you have media files to store
//            // You'll need to implement getMedias() method based on your needs
//            // actions.addAll(card.getMedias())
//        }
//
//        return if (actions.isNotEmpty()) {
//            invoke("multi", 6, mapOf("actions" to actions))
//        } else {
//            emptyMap<String, Any>()
//        }
//    }

    //    suspend fun storeCodeHighlightMedias(
//        highlightjsBase64: String,
//        highlightjsInitBase64: String,
//        highlightCssBase64: String
//    ): Any? {
//        val fileExists = invoke("retrieveMediaFile", 6, mapOf("filename" to "_highlightInit.js"))
//
//        if (fileExists == null) {
//            val highlightjs = mapOf(
//                "action" to "storeMediaFile",
//                "params" to mapOf(
//                    "filename" to "_highlight.js",
//                    "data" to highlightjsBase64
//                )
//            )
//            val highlightjsInit = mapOf(
//                "action" to "storeMediaFile",
//                "params" to mapOf(
//                    "filename" to "_highlightInit.js",
//                    "data" to highlightjsInitBase64
//                )
//            )
//            val highlightCss = mapOf(
//                "action" to "storeMediaFile",
//                "params" to mapOf(
//                    "filename" to "_highlight.css",
//                    "data" to highlightCssBase64
//                )
//            )
//
//            return invoke("multi", 6, mapOf(
//                "actions" to listOf(highlightjs, highlightjsInit, highlightCss)
//            ))
//        }
//        return null
//    }
//
    suspend fun addCards(cards: List<Card>): List<Long>? {
        val params = buildJsonObject {
            put("notes", buildJsonArray {
                cards.forEach { card ->
                    add(card.toAnkiJson(includeId = false))
                }
            })
        }

        // Using the invoke method from earlier
        return invoke<List<Long>>("addNotes", params)
    }
//    suspend fun updateCards(cards: List<Card>): Any? {
//        val updateActions = mutableListOf<Map<String, Any>>()
//        val ids = mutableListOf<Long>()
//
//        for (card in cards) {
//            updateActions.add(mapOf(
//                "action" to "updateNoteFields",
//                "params" to mapOf("note" to card.getCard(true))
//            ))
//
//            updateActions.addAll(mergeTags(card.oldTags, card.tags, card.id!!))
//            ids.add(card.id)
//        }
//
//        // Update deck
//        updateActions.add(mapOf(
//            "action" to "changeDeck",
//            "params" to mapOf(
//                "cards" to ids,
//                "deck" to cards[0].deckName
//            )
//        ))
//
//        return invoke("multi", 6, mapOf("actions" to updateActions))
//    }
//
//    suspend fun changeDeck(ids: List<Long>, deckName: String): Any? {
//        return invoke("changeDeck", 6, mapOf("cards" to ids, "deck" to deckName))
//    }
//
//    suspend fun cardsInfo(ids: List<Long>): Any? {
//        return invoke("cardsInfo", 6, mapOf("cards" to ids))
//    }
//
//    suspend fun getCards(ids: List<Long>): Any? {
//        return invoke("notesInfo", 6, mapOf("notes" to ids))
//    }
//
//    suspend fun deleteCards(ids: List<Long>): Any? {
//        return invoke("deleteNotes", 6, mapOf("notes" to ids))
//    }

    suspend fun ping(): Boolean {
        val version = invoke<String>("version")
        return version == "6"
    }

    @Serializable
    data class RequestPermissionResponse(
        @SerialName("version")
        val version: Int
    )


    suspend fun requestPermission(): RequestPermissionResponse? {
        return invoke("requestPermission")
    }

    private fun mergeTags(
        oldTags: List<String>,
        newTags: List<String>,
        cardId: Long
    ): List<Map<String, Any>> {
        val actions = mutableListOf<Map<String, Any>>()
        val oldTagsMutable = oldTags.toMutableList()

        // Find tags to Add
        for (tag in newTags) {
            val index = oldTagsMutable.indexOf(tag)
            if (index > -1) {
                oldTagsMutable.removeAt(index)
            } else {
                actions.add(
                    mapOf(
                        "action" to "addTags",
                        "params" to mapOf(
                            "notes" to listOf(cardId),
                            "tags" to tag
                        )
                    )
                )
            }
        }

        // All Tags to delete
        for (tag in oldTagsMutable) {
            actions.add(
                mapOf(
                    "action" to "removeTags",
                    "params" to mapOf(
                        "notes" to listOf(cardId),
                        "tags" to tag
                    )
                )
            )
        }

        return actions
    }

    val jsonClient = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(jsonClient)
        }
    }

    /**
     * The core Ktor-based invocation method.
     */
    private suspend inline fun <reified T> invoke(
        action: String,
        params: JsonObject? = null
    ): T? {
        val requestBody =
            AnkiRequest(action = action, version = 6, params = params ?: buildJsonObject { })

        val response: AnkiResponse = try {
            client.post(ankiUrl) {
                contentType(ContentType.Application.Json)

                setBody(requestBody)
            }.body()
        } catch (e: Exception) {
            throw Exception("Failed to connect to Anki: ${e.message}")
        }

        if (response.error != null) {
            throw Exception("Anki error: ${response.error}")
        }

        // Handle specific type casting from JsonElement
        return when (T::class) {
            JsonElement::class -> response.result as? T
            String::class -> response.result?.jsonPrimitive?.content as? T
            Long::class -> response.result?.jsonPrimitive?.longOrNull as? T
            else -> {
                val tryingToDecode = response.result?.toString()
                println("tryingToDecode: ${tryingToDecode}")
                response.result?.let { jsonClient.decodeFromJsonElement<T>(it) }
            }
        }
    }


    private fun getModels(
        sourceSupport: Boolean,
        codeHighlightSupport: Boolean
    ): List<JsonObject> {
        val sourceFieldContent = if (sourceSupport) "\r\n{{Source}}" else ""
        val codeScriptContent =
            if (codeHighlightSupport) "\r\n// Code highlighting script\r\n" else ""
        val sourceExtension = if (sourceSupport) "-source" else ""
        val codeExtension = if (codeHighlightSupport) "-code" else ""

        val css = """
        .card { font-family: arial; font-size: 20px; text-align: center; color: black; background-color: white; }
        .tag::before { content: "#"; }
        .tag { color: white; background-color: #9F2BFF; border: none; font-size: 11px; font-weight: bold; 
               padding: 1px 8px; margin: 0px 3px; text-align: center; border-radius: 14px; display: inline; }
        .cloze { font-weight: bold; color: blue; }
        .nightMode .cloze { color: lightblue; }
    """.trimIndent()

        val tagScript = """
        <script>
            var tagEl = document.querySelector('.tags');
            if (tagEl) {
                var tags = tagEl.innerHTML.split(' ');
                var html = '';
                tags.forEach(function(tag) {
                    if (tag.trim()) {
                        html += '<span class="tag">' + tag + '</span>';
                    }
                });
                tagEl.innerHTML = html;
            }
        </script>
    """.trimIndent()

        val front = "{{Front}}\r\n<p class=\"tags\">{{Tags}}</p>\r\n$tagScript$codeScriptContent"
        val back = "{{FrontSide}}\n\n<hr id=answer>\n\n{{Back}}$sourceFieldContent"

        val classicFields = buildJsonArray {
            add("Front")
            add("Back")
            if (sourceSupport) add("Source")
        }

        // Helper to create the action objects
        fun createModelAction(
            name: String,
            fields: JsonArray,
            templates: JsonArray,
            isCloze: Boolean = false
        ): JsonObject {
            return buildJsonObject {
                put("action", "createModel")
                put("params", buildJsonObject {
                    put("modelName", "Obsidian-$name$sourceExtension$codeExtension")
                    put("inOrderFields", fields)
                    put("css", css)
                    put("isCloze", isCloze)
                    put("cardTemplates", templates)
                })
            }
        }

        val basic = createModelAction(
            "basic",
            classicFields,
            buildJsonArray {
                add(buildJsonObject {
                    put("Name", "Front / Back")
                    put("Front", front)
                    put("Back", back)
                })
            }
        )

        val basicReversed = createModelAction(
            "basic-reversed",
            classicFields,
            buildJsonArray {
                add(buildJsonObject {
                    put("Name", "Front / Back")
                    put("Front", front)
                    put("Back", back)
                })
                add(buildJsonObject {
                    put("Name", "Back / Front")
                    put(
                        "Front",
                        "{{Back}}\r\n<p class=\"tags\">{{Tags}}</p>\r\n$tagScript$codeScriptContent"
                    )
                    put("Back", "{{FrontSide}}\n\n<hr id=answer>\n\n{{Front}}$sourceFieldContent")
                })
            }
        )

        val cloze = createModelAction(
            "cloze",
            buildJsonArray {
                add("Text")
                add("Extra")
                if (sourceSupport) add("Source")
            },
            buildJsonArray {
                add(buildJsonObject {
                    put("Name", "Cloze")
                    put("Front", "{{cloze:Text}}\n$tagScript$codeScriptContent")
                    put(
                        "Back",
                        "{{cloze:Text}}\n\n<br>{{Extra}}$sourceFieldContent$tagScript$codeScriptContent"
                    )
                })
            },
            isCloze = true
        )

        return listOf(basic, basicReversed, cloze)
    }
}

// Usage example
suspend fun main() {
    val ankiClient = AnkiClient()

    try {
        // Check if Anki-Connect is available
        if (ankiClient.ping()) {
            println("Connected to Anki!")

            ankiClient.requestPermission()
            ankiClient.createDeck("My Kotlin Deck")

            // Create models
            try {
                ankiClient.createModels(sourceSupport = true, codeHighlightSupport = false)
            }catch (exc: Exception){}


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