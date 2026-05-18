package com.moly3.cedarjam.core.domain.dialog

sealed class DialogState<out T> {
    class Hidden<T> : DialogState<T>()
    data class Closing<T>(val data: T) : DialogState<T>() // Add this
    data class Opened<T>(val data: T) : DialogState<T>()
}

fun <T> DialogState<T>?.getData():T?{
    return when(this){
        is DialogState.Closing -> this.data
        is DialogState.Hidden -> null
        is DialogState.Opened -> this.data
        null -> null
    }
}