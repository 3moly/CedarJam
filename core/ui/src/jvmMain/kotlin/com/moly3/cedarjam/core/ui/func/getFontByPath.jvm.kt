package com.moly3.cedarjam.core.ui.func

import androidx.compose.ui.text.font.Font
import java.io.File

actual suspend fun getFontByPath(nodePath: String, key: String): Font {
    val file = File(nodePath)
    return androidx.compose.ui.text.platform.Font(identity = "default_font_${key}",{
        file.readBytes()
    })
}