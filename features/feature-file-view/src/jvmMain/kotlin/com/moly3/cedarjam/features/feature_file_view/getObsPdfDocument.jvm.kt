package com.moly3.cedarjam.features.feature_file_view

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import co.touchlab.kermit.Logger
import org.icepdf.core.pobjects.Document
import org.icepdf.core.pobjects.Page
import org.icepdf.core.util.GraphicsRenderingHints
import java.awt.image.BufferedImage

actual fun getObsPdfDocument(absolutePath: String): ObsPdfDocument? {
    try {
        val document: Document = Document().apply {
            setFile(absolutePath)
        }
        return object : ObsPdfDocument {
            override fun getNumberOfPages(): Int {
                return document.numberOfPages
            }

            override fun getPagePainter(index: Int): Painter? {
                return try {
                    val image = document.getPageImage(
                        // pageNumber =
                        index,
                        // renderHintType =
                        GraphicsRenderingHints.SCREEN,
                        // pageBoundary =
                        Page.BOUNDARY_CROPBOX,
                        // userRotation =
                        0f,
                        // userZoom =
                        2f
                    ) as BufferedImage
                    val bmp = image.toComposeImageBitmap()
                    image.flush()
                    BitmapPainter(bmp)
                } catch (exc: Exception) {
                    Logger.e { exc.toString() }
                    null
                }
            }

            override fun getPageText(index: Int): String? {
                return document.getPageText(index).toString()
            }
        }
    } catch (exc: Exception) {
        return null
    }
}