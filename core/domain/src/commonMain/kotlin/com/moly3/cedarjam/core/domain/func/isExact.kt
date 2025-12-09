package com.moly3.cedarjam.core.domain.func

import kotlin.math.abs

fun Long?.isExact(timeInMs: Long?): Boolean {
    if (this == null && timeInMs == null)
        return true
    if (this == null || timeInMs == null)
        return false
    return abs(this - timeInMs) <= 5
}

fun Long?.isNotExact(timeInMs: Long?): Boolean {
    return !this.isExact(timeInMs)
}

fun Long?.isMoreThan(timeInMs: Long?): Boolean {
    if (this != null && timeInMs == null)
        return true
    if (this == null)
        return false
    if (timeInMs == null)
        return true
    return this.isNotExact(timeInMs) && this > timeInMs
}

fun Long?.isMoreThanOrExact(timeInMs: Long?): Boolean {
    return this.isExact(timeInMs) || this.isMoreThan(timeInMs)
}