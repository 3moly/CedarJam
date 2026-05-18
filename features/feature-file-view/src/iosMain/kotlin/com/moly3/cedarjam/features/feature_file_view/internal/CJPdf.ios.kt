package com.moly3.cedarjam.features.feature_file_view.internal

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import co.touchlab.kermit.Logger
import com.moly3.cedarjam.core.domain.model.AnnotationDTO
import com.moly3.cedarjam.core.domain.model.request.CreateAnnotationRequest
import com.moly3.cedarjam.core.ui.compositions.LocalSystemDensity
import com.moly3.cedarjam.features.feature_file_view.ObsPdfDocument
import io.github.vinceglb.filekit.utils.toNSData
import kotlinx.cinterop.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import platform.CoreGraphics.*
import platform.Foundation.*
import platform.PDFKit.*
import platform.UIKit.*

import kotlinx.serialization.Serializable
import platform.darwin.NSObject
import platform.objc.sel_registerName

@Serializable
data class PdfAnnotationDto(
    val id: String,
    val pageIndex: Int,
    val type: AnnotationType,
    val rect: PdfRect,
    val text: String? = null,
    val color: String? = null, // hex
)

@Serializable
enum class AnnotationType {
    HIGHLIGHT,
    NOTE,
    UNDERLINE,
    STRIKEOUT,
    CUSTOM
}

@Serializable
data class PdfRect(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

//const val PDFDisplayBoxMediaBox: Long = 0L
//const val PDFDisplayBoxCropBox: Long = 1L
//const val PDFDisplayBoxBleedBox: Long = 2L
//const val PDFDisplayBoxTrimBox: Long = 3L
//const val PDFDisplayBoxArtBox: Long = 4L
@OptIn(ExperimentalForeignApi::class)
fun exportSelection(
    pdfView: PDFView,

    ): PdfAnnotationDto? {
    val selection = pdfView.currentSelection ?: return null
    val page = selection.pages.firstOrNull() as? PDFPage ?: return null
    val document = pdfView.document ?: return null
    val pageIndex = document.indexForPage(page).toInt()
    val (pageW, pageH) = page.boundsForBox(1L).useContents {
        size.width to size.height
    }
    Logger.e { "pageW: ${pageW} pageH: ${pageH}" }
    // 2. Get Selection Bounds & Normalize INSIDE the useContents block
    val rect = selection.boundsForPage(page).useContents {
        // 'this' is the CGRect. Extract values here while memory is valid.
        PdfRect(
            x = (origin.x / pageW).toFloat(),
            y = (origin.y / pageH).toFloat(),
            width = (size.width / pageW).toFloat(),
            height = (size.height / pageH).toFloat()
        )
    }
    val dto = PdfAnnotationDto(
        id = NSUUID().UUIDString,
        pageIndex = pageIndex,
        type = AnnotationType.HIGHLIGHT,
        rect = rect,
        text = selection.string,
        color = "#FFEB3B"
    )
    return dto
}

@OptIn(ExperimentalForeignApi::class)
fun PDFAnnotation.toPdfAnnotationDto(pageIndex: Int, page: PDFPage): PdfAnnotationDto? {
    // Get page dimensions for normalization
    val (pageW, pageH) = page.boundsForBox(1L).useContents {
        size.width to size.height
    }

    // Get annotation bounds and normalize
    val rect = this.bounds.useContents {
        PdfRect(
            x = (origin.x / pageW).toFloat(),
            y = (origin.y / pageH).toFloat(),
            width = (size.width / pageW).toFloat(),
            height = (size.height / pageH).toFloat()
        )
    }

    // Determine annotation type
    val annotationType = when (this.type) {
        PDFAnnotationSubtypeHighlight -> AnnotationType.HIGHLIGHT
        PDFAnnotationSubtypeUnderline -> AnnotationType.UNDERLINE
        PDFAnnotationSubtypeStrikeOut -> AnnotationType.STRIKEOUT
        PDFAnnotationSubtypeText -> AnnotationType.NOTE
        else -> AnnotationType.CUSTOM
    }

    // Get color as hex string
//    val colorHex = this.color?.let { color ->
//        val r = memScoped {
//            val red = alloc<CGFloatVar>()
//            val green = alloc<CGFloatVar>()
//            val blue = alloc<CGFloatVar>()
//            val alpha = alloc<CGFloatVar>()
//
//            color.getRed(red.ptr, green.ptr, blue.ptr, alpha.ptr)
//
//            val rInt = (red.value * 255).toInt()
//            val gInt = (green.value * 255).toInt()
//            val bInt = (blue.value * 255).toInt()
//
//            "#%02X%02X%02X".format(rInt, gInt, bInt)
//        }
//        red
//    } ?: "#FFEB3B"

    // Get modification date or use current time
//    val createdAt = this.modificationDate?.timeIntervalSince1970?.let {
//        (it * 1000).toLong()
//    } ?: (NSDate().timeIntervalSince1970 * 1000).toLong()

    return PdfAnnotationDto(
        id = this.contents ?: NSUUID().UUIDString, // Use contents as ID or generate new UUID
        pageIndex = pageIndex,
        type = annotationType,
        rect = rect,
        text = this.contents,
        color = ""
    )
}

@OptIn(ExperimentalForeignApi::class)
fun clearAllAnnotations(pdfView: PDFView) {
    val document = pdfView.document ?: return

    val pageCount = document.pageCount.toInt()
    for (i in 0 until pageCount) {
        val page = document.pageAtIndex(i.toULong()) ?: continue

        // Make a copy to avoid mutation while iterating
        val annotations = page.annotations.toList()
        for (annotation in annotations) {
            if (annotation is PDFAnnotation) {
                page.removeAnnotation(annotation)
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
fun applyHighlight(dto: AnnotationDTO, pdfView: PDFView) {
    val page = pdfView.document?.pageAtIndex(dto.dataPoint.toULong()) ?: return

    val rect = page.boundsForBox(1L).useContents {
        val bounds = this
        cValue<CGRect> {
            origin.x = dto.x * bounds.size.width
            origin.y = dto.y * bounds.size.height
            size.width = dto.width * bounds.size.width
            size.height = dto.height * bounds.size.height
        }
    }

    val annotation = PDFAnnotation(
        bounds = rect,
        forType = PDFAnnotationSubtypeHighlight,
        withProperties = null
    )
    annotation.color = UIColor.yellowColor().colorWithAlphaComponent(0.35)
    annotation.contents = dto.description

    page.addAnnotation(annotation)
}

@OptIn(ExperimentalForeignApi::class)
class AnnotatablePDFView : PDFView {
    constructor(frame: CValue<CGRect>) : super(frame)
    constructor(coder: NSCoder) : super(coder)

    // Store annotation state for the current page only

    var onAddAnnotation: (PdfAnnotationDto) -> Unit = {}
    private var currentPageAnnotations: List<PDFAnnotation> = emptyList()
    private var lastCheckedPageIndex: Int = -1

    override fun buildMenuWithBuilder(builder: UIMenuBuilderProtocol) {

        val highlightAction = UIAction.actionWithTitle(
            title = "Highlight",
            image = UIImage.systemImageNamed("highlighter"),
            identifier = null,
            handler = { _ ->
                val dto = exportSelection(this)
                if (dto != null) {
                    Logger.e { "applyHighlight: ${dto}" }
                    onAddAnnotation(dto)
                }
                resetMenu()
                clearSelection()
            }
        )

        val annotationMenu = UIMenu.menuWithTitle(
            title = "",
            image = null,
            identifier = null,
            options = UIMenuOptionsDisplayInline,
            children = listOf(highlightAction)
        )

        //builder.removeMenuForIdentifier(UIMenuLookup)
        builder.removeMenuForIdentifier(UIMenuServices)

        builder.insertChildMenu(
            annotationMenu,
            atStartOfMenuForIdentifier = UIMenuLookup
        )
    }

    override fun canBecomeFirstResponder(): Boolean = true

    private fun resetMenu() {
        // Return to the primary menu state
        UIMenuController.sharedMenuController.menuItems = listOf(
            UIMenuItem("Highlight", sel_registerName("highlightSelection:")),
            UIMenuItem("Color...", sel_registerName("showColorMenu:")),
            UIMenuItem("Note", sel_registerName("addNote:"))
        )
    }

    override fun canPerformAction(action: COpaquePointer?, withSender: Any?): Boolean {
        val sel = NSStringFromSelector(action)
        val hasSelection = currentSelection != null
        return when (sel) {
            "highlightSelection:", "showColorMenu:", "addNote:",
            "setYellow:", "setGreen:", "setPink:" -> hasSelection

            else -> false
        }
    }

    fun refreshAnnotations() {
        val currentPage = this.currentPage ?: return
        val document = this.document ?: return
        val pageIndex = document.indexForPage(currentPage).toInt()
        currentPageAnnotations = currentPage.annotations.mapNotNull {
            (it as? PDFAnnotation)
        }
        lastCheckedPageIndex = pageIndex
    }

    fun checkForAnnotationChanges(onDeleteAnnotation: (PdfAnnotationDto) -> Unit) {
        val currentPage = this.currentPage ?: return
        val document = this.document ?: return
        val pageIndex = document.indexForPage(currentPage).toInt()

        // Reset cache if page changed
        if (pageIndex != lastCheckedPageIndex) {
            currentPageAnnotations = currentPage.annotations.mapNotNull {
                (it as? PDFAnnotation)
            }
            lastCheckedPageIndex = pageIndex
            return
        }

        // Get current annotations on this page
        val newAnnotations = currentPage.annotations.mapNotNull {
            (it as? PDFAnnotation)
        }

        // Check for removed annotations
        val removed = currentPageAnnotations.filter { it !in newAnnotations }
        removed.forEach { annotationText ->

            val annotationDto = annotationText.toPdfAnnotationDto(pageIndex, currentPage)
            if (annotationDto != null) {
                onDeleteAnnotation(annotationDto)
            }
            Logger.e { "🗑️ Annotation removed on page $pageIndex: ${annotationDto}" }
            //onAnnotationRemoved?.invoke(annotationText, pageIndex)
        }

//        // Check for added annotations
//        val added = newAnnotations.filter { it !in currentPageAnnotations }
//        added.forEach { annotationText ->
//            Logger.e { "➕ Annotation added on page $pageIndex: $annotationText" }
//            onAnnotationAdded?.invoke(annotationText, pageIndex)
//        }

        // Update cache
        currentPageAnnotations = newAnnotations
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CJPdf(
    modifier: Modifier,
    currentPage: Int,
    pdf: ObsPdfDocument,
    annotations: ImmutableList<AnnotationDTO>,
    filePath: String,
    onAddAnnotation: (CreateAnnotationRequest) -> Unit,
    onDeleteAnnotation: (AnnotationDTO) -> Unit
) {
    CompositionLocalProvider(
        LocalDensity provides LocalSystemDensity.current
    ) {
        val pdfView = remember {
            val customFrame = cValue<CGRect> {
                origin.x = 0.0
                origin.y = 0.0
                size.width = 100.0
                size.height = 100.0
            }
            AnnotatablePDFView(customFrame).apply {
                this.onAddAnnotation = {
                    onAddAnnotation(
                        CreateAnnotationRequest(
                            dataPath = filePath,
                            dataPoint = (it.pageIndex + 1).toDouble(),
                            description = it.text ?: "-",
                            x = it.rect.x,
                            y = it.rect.y,
                            width = it.rect.width,
                            height = it.rect.height,
                            rowId = null
                        )
                    )
                }

                autoScales = false

                displayMode = 0L
            }
        }
        LaunchedEffect(pdfView, annotations) {
            while (true) {
                delay(300)
                pdfView.checkForAnnotationChanges { annotation ->
                    val foundAnnotation =
                        annotations.firstOrNull { d ->
                            d.description == annotation.text &&
                                    d.width == annotation.rect.width &&
                                    d.height == annotation.rect.height &&
                                    d.x == annotation.rect.x &&
                                    d.y == annotation.rect.y
                        }
                    Logger.e { "annotation deletion process: is found ${foundAnnotation}" }
                    if (foundAnnotation != null) {
                        onDeleteAnnotation(foundAnnotation)
                    }
                }
            }
        }

        val lastAnnotations = remember { mutableStateOf<List<AnnotationDTO>>(listOf()) }
        val pdfData by pdf.pdfDataState
        LaunchedEffect(annotations, pdfView, pdfData) {
            //clearAllAnnotations(pdfView)
            if (pdfData != null) {
                for (item in annotations.filter { d -> !lastAnnotations.value.any { x -> x.id == d.id } }) {
                    applyHighlight(item, pdfView)
                }
                lastAnnotations.value = annotations
            }
        }

        LaunchedEffect(currentPage, pdfData) {
            val page = pdfData?.pageAtIndex((currentPage - 1L).toULong())
            if (page != null) {
                pdfView.goToPage(page)
                pdfView.refreshAnnotations()
            }
        }
        UIKitView(
            properties = UIKitInteropProperties(),
            factory = {
                pdfView.becomeFirstResponder()
                pdfView
            },
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