package com.moly3.cedarjam.core.ui.func

import androidx.compose.ui.text.font.Font
import java.io.File

actual suspend fun getFontByPath(nodePath: String, key: String): Font {
    return Font(File(nodePath))
}