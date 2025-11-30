package com.moly3.cedarjam.core.domain.func

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Actual implementation of runBlocking for wasmJs target
 *
 * Since WebAssembly/JavaScript is single-threaded and event-loop based,
 * we cannot truly block the thread. Instead, we use a cooperative approach
 * with the JavaScript event loop.
 */
actual fun <T> runBlocking(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): T {
    // Create a scope with the provided context
    val newContext = context + Dispatchers.Main.immediate
    val scope = CoroutineScope(newContext)

    // For wasmJs, we need to handle this differently than JVM
    // We'll use a blocking approach that works with the JS event loop
    return runBlockingInternal(scope, block)
}

/**
 * Internal implementation that handles the blocking semantics for wasmJs
 */
private fun <T> runBlockingInternal(
    scope: CoroutineScope,
    block: suspend CoroutineScope.() -> T
): T {
    var result: T? = null
    var exception: Throwable? = null
    var completed = false

    // Launch the coroutine
    val job = scope.launch {
        try {
            result = block()
        } catch (e: Throwable) {
            exception = e
        } finally {
            completed = true
        }
    }

    // For wasmJs, we need to pump the event loop until completion
    // This is different from JVM's thread blocking
    while (!completed) {
        // Yield control back to the JavaScript event loop
        // This allows other coroutines and JS tasks to execute
        processEventLoop()
    }

    // Handle the result
    exception?.let { throw it }
    @Suppress("UNCHECKED_CAST")
    return result as T
}

/**
 * Process the JavaScript event loop
 * This is a platform-specific implementation for wasmJs
 */
private fun processEventLoop() {
    // In wasmJs, we need to yield to the JavaScript event loop
    // This can be implemented using setTimeout with 0 delay
    // or other platform-specific mechanisms

    // Note: This is a simplified version. In a real implementation,
    // you might need to use platform-specific APIs or external declarations
    js("""
        // Yield to the event loop by scheduling a microtask
        if (typeof Promise !== 'undefined') {
            Promise.resolve().then(() => {});
        } else if (typeof setTimeout !== 'undefined') {
            setTimeout(() => {}, 0);
        }
    """)
}
