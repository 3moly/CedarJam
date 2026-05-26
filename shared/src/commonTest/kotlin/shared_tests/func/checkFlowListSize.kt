package shared_tests.func

import io.ktor.util.internal.initCauseBridge
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout

suspend inline fun <T> Flow<List<T>>.checkFlowListSize(
    expectedSize: Int,
    timeoutMs: Long = 2000L
): List<T> {
    // Capture call site BEFORE suspending
    val callSite = Throwable("checkFlowListSize called here")

    var lastSeen: List<T>? = null
    return try {
        withTimeout(timeoutMs) {
            first { list ->
                lastSeen = list
                list.size == expectedSize
            }
        }
    } catch (e: TimeoutCancellationException) {
        val seen = lastSeen
        val error = if (seen != null) {
            buildSizeError(seen, expectedSize, timeoutMs)
        } else {
            AssertionError("Flow timed out after ${timeoutMs}ms waiting for size $expectedSize, but never emitted any lists.")
        }
        error.initCauseBridge(callSite)
        throw error
    }
}

inline fun <T> buildSizeError(list: List<T>, expectedSize: Int, timeoutMs: Long): AssertionError {
    val items = list.mapIndexed { i, item -> "  [$i] $item" }.joinToString("\n")
    return AssertionError(
        """
        |Expected size: $expectedSize, but was: ${list.size} (after ${timeoutMs}ms)
        |Items (${list.size}):
        |$items
        """.trimMargin()
    )
}

suspend inline fun <T, D> Flow<Map<T, D>>.checkFlowMapSize(
    expectedSize: Int,
    timeoutMs: Long = 2000L
): Map<T, D> {
    var lastSeen: Map<T, D>? = null
    return try {
        withTimeout(timeoutMs) {
            first { map ->
                lastSeen = map
                map.size == expectedSize
            }
        }
    } catch (e: TimeoutCancellationException) {
        // If the flow emitted at least one item, trigger the explanation method
        lastSeen?.shouldHaveSizeAndExplain(expectedSize)

        // Fallback if the flow timed out without emitting absolutely anything
        throw AssertionError(
            "Flow timed out after ${timeoutMs}ms waiting for size $expectedSize, but never emitted any maps.",
            e
        )
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

inline fun <T, D> Map<T, D>.shouldHaveSizeAndExplain(expectedSize: Int) {
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