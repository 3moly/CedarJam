package com.moly3.cedarjam.features.feature_file_view.internal

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import co.touchlab.kermit.Logger
import com.moly3.cedarjam.core.ui.compositions.LocalSystemDensity
import com.moly3.cedarjam.features.feature_file_view.ObsPdfDocument
import io.github.vinceglb.filekit.utils.toNSData
import kotlinx.cinterop.*
import platform.CoreGraphics.*
import platform.Foundation.*
import platform.PDFKit.*
import platform.UIKit.*

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CJPdf(
    modifier: Modifier,
    currentPage: Int,
    pdf: ObsPdfDocument,
    filePath: String
) {
    CompositionLocalProvider(
        LocalDensity provides LocalSystemDensity.current
    ) {
        val pdfView = remember {
            PDFView().apply {
                autoScales = false
                displayMode = 0L
//                displayMode = PDFDisplayMode.PDFDisplaySinglePageContinuous
//                displayDirection = PDFDisplayDirection.PDFDisplayDirectionVertical

                // Enable thumbnails if requested
//                if (enablePageThumbnails) {
//                    // You can add a thumbnail view separately
//                }

                // Setup page change notification
                NSNotificationCenter.defaultCenter.addObserverForName(
                    name = PDFViewPageChangedNotification,
                    `object` = this,
                    queue = NSOperationQueue.mainQueue
                ) { notification ->
                    if (notification != null) {
                        (notification.`object` as? PDFView)?.currentPage?.let { page ->
                            val document = (notification.`object` as? PDFView)?.document
//                            document?.indexForPage(page)?.let { index ->
//                                onPageChanged(index.toInt())
//                            }
                        }
                    }
                }
            }
        }
        LaunchedEffect(currentPage) {
            val page = pdf.getPdfData()?.pageAtIndex((currentPage - 1L).toULong())
            if (page != null) {
                pdfView.goToPage(page)
            }
        }
        //PDFDisplaySinglePage            = 0L
        //PDFDisplaySinglePageContinuous  = 1L
        //PDFDisplayTwoUp                 = 2L
        //PDFDisplayTwoUpContinuous       = 3L
        UIKitView(
            properties = UIKitInteropProperties(),
            factory = { pdfView },
            update = { view ->
                view.document = pdf.getPdfData()
            },
            onRelease = { view ->
                NSNotificationCenter.defaultCenter.removeObserver(view)
            },
            modifier = modifier
        )
    }
}