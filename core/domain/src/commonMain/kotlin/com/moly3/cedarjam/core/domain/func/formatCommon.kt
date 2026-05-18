package com.moly3.cedarjam.core.domain.func

import kotlin.math.pow
import kotlin.math.roundToInt

fun Double.formatCommon(decimals: Int): String {
    if (decimals <= 0) return this.roundToInt().toString()
    
    // 1. Shift the decimal point
    val multiplier = 10.0.pow(decimals)
    
    // 2. Round the number and shift it back by dividing
    // (We convert to Long to avoid floating point division issues, then back to String)
    val rounded = kotlin.math.round(this * multiplier).toLong()
    val isNegative = rounded < 0
    val absoluteRounded = kotlin.math.abs(rounded).toString()

    // 3. Pad with leading zeros if the number is very small (e.g., 0.05)
    val paddedNumber = absoluteRounded.padStart(decimals + 1, '0')
    
    // 4. Split into whole and fractional parts
    val insertIndex = paddedNumber.length - decimals
    val wholePart = paddedNumber.substring(0, insertIndex)
    val fractionPart = paddedNumber.substring(insertIndex)

    val sign = if (isNegative) "-" else ""
    
    return "$sign$wholePart.$fractionPart".dropTrailingZeros()
}

fun Float.formatCommon(decimals: Int): String {
    if (decimals <= 0) return this.roundToInt().toString()

    // 1. Shift the decimal point
    val multiplier = 10.0.pow(decimals)

    // 2. Round the number and shift it back by dividing
    // (We convert to Long to avoid floating point division issues, then back to String)
    val rounded = kotlin.math.round(this * multiplier).toLong()
    val isNegative = rounded < 0
    val absoluteRounded = kotlin.math.abs(rounded).toString()

    // 3. Pad with leading zeros if the number is very small (e.g., 0.05)
    val paddedNumber = absoluteRounded.padStart(decimals + 1, '0')

    // 4. Split into whole and fractional parts
    val insertIndex = paddedNumber.length - decimals
    val wholePart = paddedNumber.substring(0, insertIndex)
    val fractionPart = paddedNumber.substring(insertIndex)

    val sign = if (isNegative) "-" else ""

    return "$sign$wholePart.$fractionPart".dropTrailingZeros()
}

fun String.dropTrailingZeros(): String {
    // Only strip zeros if there is actually a decimal point
    return if (this.contains('.')) {
        this.trimEnd('0').trimEnd('.')
    } else {
        this
    }
}