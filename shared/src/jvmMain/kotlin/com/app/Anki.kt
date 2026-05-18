import com.moly3.cedarjam.core.domain.model.anki.AnkiNote
import com.moly3.cedarjam.core.net.AnkiClient


// Usage example
suspend fun main() {
    val ankiClient = AnkiClient()

    try {
        // Check if Anki-Connect is available
        if (ankiClient.ping()) {
            println("Connected to Anki!")

            val deckName = "CedarJam::My Kotlin Deck"


            // Create models
            try {

            } catch (exc: Exception) {
            }


            val notes = listOf(
                AnkiNote(
                    id = null,
                    deckName = deckName,
                    modelName = "CedarJam-basic-source",
                    fields = mapOf(
                        "Front" to "What is Kotlin?",
                        "Back" to "A modern programming language for JVM",
                        "Source" to "Kotlin Documentation"
                    ),
                    tags = listOf("kotlin", "programming")
                ),
                AnkiNote(
                    deckName = deckName,
                    modelName = "CedarJam-basic-source",
                    fields = mapOf(
                        "Front" to "What is coroutines?",
                        "Back" to "Ahehe",
                        "Source" to "Ahehe323"
                    ),
                    tags = listOf("broski")
                )
            )


        } else {
            println("Failed to connect to Anki. Make sure Anki is running with Anki-Connect plugin.")
        }
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}