package com.moly3.cedarjam.features.feature_file_view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.platform.LocalDensity
import co.touchlab.kermit.Logger
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.ui.func.bufferedImageToImageBitmap
import kotlinx.coroutines.launch
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import org.icepdf.core.pobjects.Document
import org.icepdf.core.pobjects.Page
import org.icepdf.core.util.GraphicsRenderingHints
import java.awt.image.BufferedImage
import java.io.File

@Composable
actual fun getObsPdfDocument(absolutePath: String): ObsPdfDocument? {

//    val documentState = remember {
//        mutableStateOf<PDDocument?>(null)
//    }
    val documentState = remember {
        mutableStateOf<Document?>(null)
    }
//    val renderState = remember {
//        mutableStateOf<PDFRenderer?>(null)
//    }

    val scope = rememberCoroutineScope()
    LaunchedEffect(absolutePath) {
        scope.launch(io) {
            documentState.value=  Document().apply {
                setFile(absolutePath)
            }
        }
    }
    val density = LocalDensity.current.density
    return remember(documentState.value, density) {
        object : ObsPdfDocument {
            override fun isActive(): Boolean {
                return true
            }

            override fun getNumberOfPages(): Int {
                return documentState.value?.pageTree?.numberOfPages ?: 0
            }

            override fun getPagePainter(index: Int): Painter? {
                return try {
                    val image = documentState.value?.getPageImage(
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
//                    val image: BufferedImage =
//                        renderState.value!!.renderImageWithDPI(index, (density * 160f))
//                    val bmp = bufferedImageToImageBitmap(image)
//                    BitmapPainter(bmp)
                } catch (exc: Exception) {
                    Logger.e { exc.toString() }
                    null
                }
            }

//            override fun getPageText(index: Int): String? {
//                return document.getPageText(index).toString()
//            }

            override fun getPdfData(): PdfData? {
                return null
            }

            override val pdfDataState: State<PdfData?>
                get() = TODO("Not yet implemented")
        }
    }
}