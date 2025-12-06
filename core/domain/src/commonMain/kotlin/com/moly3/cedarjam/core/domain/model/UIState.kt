package com.moly3.cedarjam.core.domain.model

import androidx.compose.runtime.Stable

@Stable
sealed class UIState<out T, out E> {
    data object Loading : UIState<Nothing, Nothing>()

    data class Error<E>(val error: E) : UIState<Nothing, E>()

    data class Success<T>(val data: T) : UIState<T, Nothing>()

    fun isLoading() = this is Loading
    fun isSuccess() = this is Success
    fun isError() = this is Error

    fun getOrNull(): T? = (this as? Success)?.data

    fun getOrDefault(default: @UnsafeVariance T): T =
        (this as? Success)?.data ?: default

    inline fun <R> map(mapper: (T) -> R): UIState<R, E> = when (this) {
        is Loading -> Loading
        is Error -> Error(error)
        is Success -> Success(mapper(data))
    }

    inline fun <R> mapState(mapper: (T) -> UIState<R, @UnsafeVariance E>): UIState<R, E> =
        when (this) {
            is Loading -> Loading
            is Error -> Error(error)
            is Success -> mapper(data)
        }
}

inline fun <S, E, UIS, UIE> ResultWrapper<S, E>.mapToUIState(
    mapError: (E) -> UIE,
    mapSuccess: (S) -> UIS,
    onError: (Exception) -> UIE
): UIState<UIS, UIE> {
    return when (this) {
        is ResultWrapper.Error -> UIState.Error(mapError(error))
        is ResultWrapper.Success -> try {
            UIState.Success(mapSuccess(value))
        } catch (exc: Exception) {
            UIState.Error(onError(exc))
        }
    }
}

fun <S, E> ResultWrapper<S, E>.mapToUIState(onError: (Exception) -> E): UIState<S, E> {
    return this.mapToUIState(mapError = { d -> d }, mapSuccess = { x -> x }, onError = onError)
}