package com.moly3.cedarjam.core.domain.model

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class ResultWrapper<out S, out E> {
    data class Success<out S>(val value: S) : ResultWrapper<S, Nothing>()
    data class Error<out E>(val error: E) : ResultWrapper<Nothing, E>()
}

fun <S, E> ResultWrapper<S, E>.isSuccess(): Boolean = this is ResultWrapper.Success
fun <S, E> ResultWrapper<S, E>.isError(): Boolean = this is ResultWrapper.Error

fun <S, E> success(value: S): ResultWrapper<S, E> = ResultWrapper.Success(value)
fun <S, E> error(error: E): ResultWrapper<S, E> = ResultWrapper.Error(error)

@OptIn(ExperimentalContracts::class)
inline fun <reified S, E> ResultWrapper<S, E>.shouldBeSuccess() {
    contract {
        returns() implies (this@shouldBeSuccess is ResultWrapper.Success<S>)
    }
    when (this) {
        is ResultWrapper.Error -> throw IllegalArgumentException(this.error.toString())
        is ResultWrapper.Success -> { /* Success case - do nothing */
        }
    }
}

/**
 * Checks a condition. If the condition is false, the execution of the
 * enclosing resultBlock is immediately stopped by calling raise() with the
 * result of the 'error' lambda.
 * * @param condition The condition to check.
 * @param error A lambda that returns the error value E if the condition is false.
 */
@OptIn(ExperimentalContracts::class)
fun <E> ResultRaise<E>.ensure(condition: Boolean, error: () -> E) {
    contract {
        returns() implies (condition)
    }
    if (!condition) {
        // If the condition fails, use the custom 'raise' function to exit the block
        raise(error())
    }
}

fun <T : Any, E> ResultRaise<E>.ensureNotNull(value: T?, error: () -> E): T {
    if (value == null) {
        // If the value is null, use the custom 'raise' function to exit the block
        raise(error())
    }
    // Kotlin's smart-cast now treats 'value' as non-null (T)
    return value
}


@OptIn(ExperimentalContracts::class)
inline fun <S, reified E> ResultWrapper<S, E>.shouldBeError() {
    contract {
        returns() implies (this@shouldBeError is E)
    }
    when (this) {
        is ResultWrapper.Error -> {}
        is ResultWrapper.Success -> throw IllegalArgumentException(this.value.toString())
    }
}

fun <S, E> ResultWrapper<S, E>.getValueOrNull(): S? = when (this) {
    is ResultWrapper.Success -> this.value
    is ResultWrapper.Error -> null
}

//inline fun <reified S, reified E> Either<E, S>.toResult(): ResultWrapper<S, E> {
//    return when (this) {
//        is Either.Left -> ResultWrapper.Error(this.value)
//        is Either.Right -> ResultWrapper.Success(this.value)
//    }
//}

//fun <S, E> Raise<E>.bind(result: ResultWrapper<S, E>): S = when (result) {
//    is ResultWrapper.Error -> raise(result.error)
//    is ResultWrapper.Success -> result.value
//}

// Alternative: Extension function that requires Raise context
//fun <S, E> ResultWrapper<S, E>.bind(raise: Raise<E>): S = when (this) {
//    is ResultWrapper.Error -> raise.raise(this.error)
//    is ResultWrapper.Success -> this.value
//}

inline fun <S, E, T> ResultWrapper<S, E>.fold(
    onSuccess: (value: S) -> T,
    onFailure: (E) -> T
): T {
    return when (this) {
        is ResultWrapper.Error -> onFailure(this.error)
        is ResultWrapper.Success -> onSuccess(this.value)
    }
}


// Helper function to bind a ResultWrapper within the custom ResultRaise context
fun <S, E> ResultRaise<E>.bind(result: ResultWrapper<S, E>): S = when (result) {
    is ResultWrapper.Error -> raise(result.error)
    is ResultWrapper.Success -> result.value
}

/**
 * Checks if the nullable 'value' is not null.
 * If it is null, the execution of the enclosing resultBlock is immediately
 * stopped by calling raise() with the result of the 'error' lambda.
 * * @param value The nullable value to check.
 * @param error A lambda that returns the error value E if the 'value' is null.
 * @return The non-nullable value of type T if the check succeeds.
 */

interface ResultRaise<E> {
    fun raise(error: E): Nothing
}

class ResultRaiseException(val error: Any?) : Exception()

class ResultRaiseImpl<E> : ResultRaise<E> {
    override fun raise(error: E): Nothing {
        throw ResultRaiseException(error)
    }
}

inline fun <S, E> resultBlock(block: ResultRaise<E>.() -> S): ResultWrapper<S, E> {
    val raise = ResultRaiseImpl<E>()
    return try {
        val successValue = block(raise)
        ResultWrapper.Success(successValue)
    } catch (e: ResultRaiseException) {
        @Suppress("UNCHECKED_CAST")
        ResultWrapper.Error(e.error as E)
    } catch (e: Exception) {
        // Handle unexpected exceptions if necessary, e.g., mapping them to a default E
        // For simplicity, we are assuming the original code only throws on 'raise' or 'try/catch'
        // If your original 'raise' was used for Exception->E mapping, you'd do that here.
        ResultWrapper.Error("Unexpected exception: ${e.message}" as E) // Cast might be risky if E is not String
    }
}