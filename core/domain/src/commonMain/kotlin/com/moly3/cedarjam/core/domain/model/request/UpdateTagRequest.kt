package com.moly3.cedarjam.core.domain.model.request

import androidx.compose.ui.graphics.Color

data class UpdateTagRequest(
    val id: Long,
    val color: Color,
    val modifiedTime: Long
)