package com.moly3.cedarjam.core.ui.func

import androidx.compose.ui.graphics.ImageBitmap

expect suspend fun ImageBitmap.saveAsPng(path: String)