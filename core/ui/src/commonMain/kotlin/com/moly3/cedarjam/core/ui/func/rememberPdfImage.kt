package com.moly3.cedarjam.core.ui.func

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import com.moly3.cedarjam.core.domain.io
import kotlinx.coroutines.launch
import kotlinx.io.files.Path

@Composable
fun rememberPdfImage(
    fullPath: String?
): ImageBitmap? {
    var imgBitmap by remember {
        mutableStateOf<ImageBitmap?>(null)
    }
    LaunchedEffect(fullPath) {
        launch(io) {
            val path = Path(
                fullPath ?: ""
            ).toString()
            try {
                imgBitmap = if (fullPath != null) {
                    getPdfImage(
                        path,
                        page = 0,
                        dpi = 100f
                    )
                } else {
                    null
                }
            } catch (exc: Exception) {
                co.touchlab.kermit.Logger.w { "getPdfImage result: ${exc.message}" }
            }
        }
    }
    return imgBitmap
}