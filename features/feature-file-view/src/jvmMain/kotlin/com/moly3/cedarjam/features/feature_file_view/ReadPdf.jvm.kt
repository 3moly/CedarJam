package com.moly3.cedarjam.features.feature_file_view

//actual fun ReadPdf(fullpath: String, index: Int): Painter? {
//    return try {
//        val document: Document = Document().apply {
//            setFile(fullpath)
//        }
//        val image = document.getPageImage(
//            // pageNumber =
//            index,
//            // renderHintType =
//            GraphicsRenderingHints.SCREEN,
//            // pageBoundary =
//            Page.BOUNDARY_CROPBOX,
//            // userRotation =
//            0f,
//            // userZoom =
//            5f
//        ) as BufferedImage
//        val bmp = image.toComposeImageBitmap()
//        image.flush()
//        BitmapPainter(bmp)
//    } catch (exc: Exception) {
//        null
//    }
//}