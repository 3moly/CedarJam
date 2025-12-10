package com.moly3.cedarjam.core.domain.func

fun String.relativeTo(path: String): String {
    return if (!this.startsWith(path))
        this
    else
        this.substring(path.length).removePrefix("/")
}