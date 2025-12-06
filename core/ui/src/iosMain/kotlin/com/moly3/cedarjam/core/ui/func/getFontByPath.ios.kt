package com.moly3.cedarjam.core.ui.func

import androidx.compose.ui.text.font.Font
import org.jetbrains.skiko.loadBytesFromPath

actual suspend fun getFontByPath(nodePath: String, key: String): Font {
    return androidx.compose.ui.text.platform.Font(
        identity = "cedarjam_default_font_${key}",
        loadBytesFromPath(nodePath)
    )
}