package com.moly3.cedarjam.core.domain.model

import androidx.compose.runtime.Stable

@Stable
data class LineMatch(
    val line: Int,
    val text: String
)