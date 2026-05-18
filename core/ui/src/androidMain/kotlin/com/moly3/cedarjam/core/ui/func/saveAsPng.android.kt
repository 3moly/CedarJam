package com.moly3.cedarjam.core.ui.func

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream

actual suspend fun ImageBitmap.saveAsPng(path: String) {
    withContext(Dispatchers.IO) {
        val bitmap = asAndroidBitmap()
        FileOutputStream(path).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 10, it)
        }
    }
}