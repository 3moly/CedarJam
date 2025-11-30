package com.moly3.cedarjam.core.domain.func

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun Long.formatEpochMillis(epochMillis: Long = this, isShowTime: Boolean = false): String {
    val instant = Instant.fromEpochMilliseconds(epochMillis)
    val currentTimeZone = TimeZone.currentSystemDefault()
    val datetime: LocalDateTime = instant.toLocalDateTime(currentTimeZone)
    val datetimeNow: LocalDateTime = Clock.System.now().toLocalDateTime(currentTimeZone)
    fun Int.display(): String {
        return this.padStart(2, '0')
    }

    val timeInText = datetime.time.run {
        val secondsText = if (isShowTime) {
            ":${second.display()}"
        } else {
            ""
        }
        "${hour.display()}:${minute.display()}${secondsText}"

    }
    if (datetime.date == datetimeNow.date) {
        return timeInText
    }
    return "${datetime.date} $timeInText"
}