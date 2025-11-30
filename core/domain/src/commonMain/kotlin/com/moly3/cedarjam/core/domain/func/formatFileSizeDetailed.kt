package com.moly3.cedarjam.core.domain.func

import kotlin.math.log10
import kotlin.math.pow

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"

    val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB")
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
    val size = bytes / 1024.0.pow(digitGroups.toDouble())

    return "${roundToOneDecimal(size)} ${units[digitGroups]}"
}

private fun roundToOneDecimal(value: Double): String {
    val rounded = (value * 10).toInt() / 10.0
    return if (rounded == rounded.toInt().toDouble()) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}