package com.moly3.cedarjam.core.ui.func

import androidx.compose.ui.graphics.ImageBitmap

expect fun getPdfImage(
    path: String,
    page: Int,
    dpi: Float
): ImageBitmap