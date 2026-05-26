package shared_tests.func

import io.kotest.matchers.collections.shouldHaveSize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout

suspend fun <T> Flow<List<T>>.checkFlowListSize(
    expectedSize: Int,
    timeoutMs: Long = 2000L
): List<T> {
    return withTimeout(timeoutMs) {
        first { it.size == expectedSize }
    }
}

suspend fun <T, D> Flow<Map<T, D>>.checkFlowMapSize(
    expectedSize: Int,
    timeoutMs: Long = 2000L
): Map<T, D> {
    return withTimeout(timeoutMs) {
        first { it.size == expectedSize }
    }
}

fun <T> List<T>.shouldHaveSizeAndExplain(expectedSize: Int) {
    if (expectedSize != this.size) {
        val items = this.mapIndexed { index, item -> "  [$index] $item" }
            .joinToString(separator = "\n")
        throw AssertionError(
            """
            |Expected size: $expectedSize, but was: ${this.size}
            |Items (${this.size}):
            |$items
            """.trimMargin()
        )
    }
}

fun <T, D> Map<T, D>.shouldHaveSizeAndExplain(expectedSize: Int) {
    if (expectedSize != this.size) {
        val entries = this.entries.mapIndexed { index, (key, value) -> "  [$index] $key -> $value" }
            .joinToString(separator = "\n")
        throw AssertionError(
            """
            |Expected size: $expectedSize, but was: ${this.size}
            |Entries (${this.size}):
            |$entries
            """.trimMargin()
        )
    }
}