package com.moly3.cedarjam.core.domain.model.request

import androidx.compose.ui.graphics.Color

data class CreateTagRequest(
    val name: String,
    val color: Color,
    val createdTime: Long
)