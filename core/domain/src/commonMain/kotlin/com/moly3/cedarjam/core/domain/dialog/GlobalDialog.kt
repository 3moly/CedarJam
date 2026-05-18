package com.moly3.cedarjam.core.domain.dialog

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

abstract class GlobalDialog<Input, Result>(
    val isGenericDialog: Boolean = true,
    val closeValue: Result,
    val register: IDialogRegister
) {

    private var _continuation: Continuation<Result>? = null

    private var pendingResult: Result? = null

    private val _inputDataState =
        MutableStateFlow<DialogState<Input>>(DialogState.Hidden())
    val inputData: StateFlow<DialogState<Input>> = _inputDataState

    suspend fun setResult(data: Result) {
        _inputDataState.emit(DialogState.Hidden())
        _continuation?.resume(data)
        _continuation = null
    }

    suspend fun requestClose(data: Result) {
        val inputData = _inputDataState.value
        if (inputData is DialogState.Opened) {
            this.pendingResult = data
            _inputDataState.emit(DialogState.Closing(data = inputData.data))
        }
    }

    suspend fun confirmHidden() {
        // Use the saved pendingResult or the default closeValue
        val result = pendingResult ?: closeValue
        _inputDataState.emit(DialogState.Hidden())

        // Resume the 'open()' call that is currently suspended
        _continuation?.resume(result)
        _continuation = null
        pendingResult = null
    }

    fun openImmediate(inputData: Input) {
        _inputDataState.value = (DialogState.Opened(inputData))
    }

    suspend fun open(inputData: Input): Result {
        register.register(this)
        _inputDataState.emit(DialogState.Opened(inputData))
        return suspendCoroutine { continuation ->
            _continuation = continuation
        }
    }
}
