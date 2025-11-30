package com.moly3.cedarjam.core.domain.dialog

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

abstract class GlobalDialog<Input, Result>(val isGenericDialog: Boolean = true, val closeValue: Result) {

    private var _continuation: Continuation<Result>? = null
    private val _inputDataState =
        MutableStateFlow<DialogState<Input>>(DialogState.Hidden())
    val inputData: StateFlow<DialogState<Input>> = _inputDataState
    val isEnabledFlow: Flow<Boolean> = _inputDataState.map {
        it is DialogState.Opened
    }

    fun isOpened(): Boolean {
        return inputData.value is DialogState.Opened
    }

    suspend fun setResult(data: Result) {
        _inputDataState.emit(DialogState.Hidden())
        if (_continuation != null) {
            _continuation?.resume(data)
            _continuation = null
        }
    }

    suspend fun open(inputData: Input): Result {
        _inputDataState.emit(DialogState.Opened(inputData))
        return suspendCoroutine { continuation ->
            _continuation = continuation
        }
    }
}
