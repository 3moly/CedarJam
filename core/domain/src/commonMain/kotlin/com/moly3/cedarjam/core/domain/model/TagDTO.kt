package com.moly3.cedarjam.core.domain.model

import androidx.compose.ui.graphics.Color

data class TagDTO(
    val id: Long,
    val name: String,
    val color: Color,
    val createdTime: Long,
    val modifiedTime: Long
)