package com.moly3.cedarjam.core.domain.service

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Density

interface IImageTransform {
    fun getPdfImage(
        path: String,
        page: Int,
        density: Float
    ): ImageBitmap

    fun cropNormalized(
        imageBitmap: ImageBitmap,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ): ImageBitmap
}