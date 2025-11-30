package com.moly3.cedarjam.core.domain.func

fun Int.padStart(length: Int, padChar: Char): String {
    return this.toString().padStart(length, padChar)
}