fun main() {
    val cipher = "2E3GX3C3U93E4JK545I35M69L6Q6N78F91PAF9D0CED0GEAF91GQHZRJ6K95LON1TN4QB2S0TMVVR"

    println("Analyzing cipher: $cipher")
    println("Length: ${cipher.length}\n")

    // Пробуем разные фильтры шума
    val filters = listOf(
        "No filter" to { s: String -> s },
        "Every 2nd" to { s: String -> s.filterIndexed { i, _ -> i % 2 == 0 } },
        "Every 3rd" to { s: String -> s.filterIndexed { i, _ -> i % 3 == 0 } },
        "Every 4th" to { s: String -> s.filterIndexed { i, _ -> i % 4 == 0 } }
    )

    val alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val base = 36

    println("Testing different noise filters and parameters...\n")

    var bestSolution: Triple<String, String, Double>? = null
    var bestScore = 0.0

    // Тестируем разные комбинации
    for ((filterName, filter) in filters) {
        val filtered = filter(cipher)

        for (reversed in listOf(false, true)) {
            val processed = if (reversed) filtered.reversed() else filtered

            // Фокусируемся на наиболее вероятных диапазонах
            for (baseShift in -30..30) {
                for (multiplier in -5..5) {
                    try {
                        val decoded = decryptBase36(processed, alphabet, baseShift, multiplier, base)
                        val score = scoreText(decoded)
                        println(decoded)
                        if (score > bestScore && decoded.length >= 10) {
                            bestScore = score
                            val params = "Filter: $filterName, Reversed: $reversed, BaseShift: $baseShift, Mult: $multiplier"
                            bestSolution = Triple(decoded, params, score)

                            if (score > 50) {
                                println("🎯 Strong candidate (score: ${"%.1f".format(score)}):")
                                println("   Message: $decoded")
                                println("   $params\n")
                            }
                        }
                    } catch (e: Exception) {
                        // Skip invalid combinations
                    }
                }
            }
        }
    }

    println("\n" + "=".repeat(70))
    println("BEST SOLUTION FOUND:")
    println("=".repeat(70))
    bestSolution?.let { (message, params, score) ->
        println("Message: $message")
        println("Score: ${"%.2f".format(score)}")
        println("Parameters: $params")
        println("\nStats:")
        println("  Length: ${message.length}")
        println("  Letters: ${message.count { it.isLetter() }}")
        println("  Digits: ${message.count { it.isDigit() }}")
        println("  Spaces: ${message.count { it == ' ' }}")
    } ?: println("No valid solution found")
}

fun decryptBase36(text: String, alphabet: String, baseShift: Int, multiplier: Int, base: Int): String {
    val numbers = text.mapIndexed { index, char ->
        val value = alphabet.indexOf(char)
        if (value == -1) throw IllegalArgumentException("Invalid character: $char")

        val shift = baseShift + (index * multiplier)
        var original = (value - shift) % alphabet.length
        if (original < 0) original += alphabet.length

        original
    }

    // Преобразуем base36 числа в ASCII символы
    return numbers.mapNotNull { num ->
        if (num in 32..126) num.toChar() else null
    }.joinToString("")
}

fun scoreText(text: String): Double {
    if (text.length < 10) return 0.0

    var score = 0.0

    // Проверяем наличие цифр и букв
    val digits = "0123456789".toList().map { it.toString() }
    val letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toList().map { it.toString() }

    val upperText = text.uppercase()
    val digitMatches = digits.count { upperText.contains(it) }
    val letterMatches = letters.count { upperText.contains(it) }

    score += digitMatches * 3.0
    score += letterMatches * 2.0

    // Бонус за читаемость
    val letterRatio = text.count { it.isLetter() || it.isDigit() }.toDouble() / text.length
    score += letterRatio * 20.0

    val spaceRatio = text.count { it == ' ' }.toDouble() / text.length
    if (spaceRatio in 0.05..0.25) score += 15.0

    // Бонус за длину
    score += kotlin.math.min(text.length / 5.0, 15.0)

    // Штраф за слишком много спецсимволов
    val specialChars = text.count { !it.isLetterOrDigit() && it != ' ' }
    if (specialChars > text.length * 0.3) score -= 20.0

    return score
}