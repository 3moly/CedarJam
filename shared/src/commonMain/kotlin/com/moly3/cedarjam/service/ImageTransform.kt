package com.moly3.cedarjam.service

import androidx.compose.ui.graphics.ImageBitmap
import com.moly3.cedarjam.core.domain.service.IImageTransform
import com.moly3.cedarjam.core.ui.func.cropNormalized

class ImageTransform : IImageTransform {
    override fun getPdfImage(
        path: String,
        page: Int,
        density: Float
    ): ImageBitmap {
        return com.moly3.cedarjam.core.ui.func.getPdfImage(
            path = path,
            page = page,
            density = density
        )
    }

    override fun cropNormalized(
        imageBitmap: ImageBitmap,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ): ImageBitmap {
        return imageBitmap.cropNormalized(
            x = x,
            y = y,
            width = width,
            height = height
        )
    }
}