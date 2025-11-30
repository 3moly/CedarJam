package com.moly3.cedarjam.core.domain.dialog

sealed class DialogState<InputData> {
    data class Hidden<InputData>(val inputData: InputData? = null) :
        DialogState<InputData>()

    data class Opened<InputData>(val inputData: InputData) :
        DialogState<InputData>()
}