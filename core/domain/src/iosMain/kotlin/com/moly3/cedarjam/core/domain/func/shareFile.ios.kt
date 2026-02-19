package com.moly3.cedarjam.core.domain.func

import co.touchlab.kermit.Logger
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

import platform.UIKit.*
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

fun topViewController(): UIViewController? {
    val windowScene = UIApplication.sharedApplication
        .connectedScenes
        .firstOrNull() as? UIWindowScene

    val window = windowScene
        ?.keyWindow()

    var topController = window?.rootViewController

    while (topController?.presentedViewController != null) {
        topController = topController.presentedViewController
    }

    return topController
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun shareFile(fullPath: String) {
    try {
        val fileUrl = NSURL.fileURLWithPath(fullPath)

        val controller = UIActivityViewController(
            activityItems = listOf(fileUrl),
            applicationActivities = null
        )

        val vc = topViewController() ?: return

        // 🔥 iPad fix
        controller.popoverPresentationController?.sourceView = vc.view
        controller.popoverPresentationController?.sourceRect = vc.view.bounds
        controller.popoverPresentationController?.permittedArrowDirections = 0u

        vc.presentViewController(
            controller,
            animated = true,
            completion = null
        )
//        val activityController = UIActivityViewController(
//            activityItems = listOf(fileUrl),
//            applicationActivities = null
//        )
//
//        topViewController()?.presentViewController(
//            activityController,
//            animated = true,
//            completion = null
//        )
    } catch (exc: Exception) {
        Logger.e { "exception: ${exc}" }
    }catch (e: Error){
        Logger.e { "exception: ${e}" }
    }
}
//import io.github.vinceglb.filekit.FileKit
//import io.github.vinceglb.filekit.PlatformFile
//import io.github.vinceglb.filekit.dialogs.FileKitShareSettings
//import io.github.vinceglb.filekit.dialogs.shareFile
//import kotlinx.cinterop.ExperimentalForeignApi
//import kotlinx.io.files.Path
//import platform.Foundation.NSFileManager
//import platform.Foundation.NSTemporaryDirectory
//import platform.Foundation.NSURL
//
//@OptIn(ExperimentalForeignApi::class)
//actual suspend fun shareFile(fullPath: String) {
//    val tempPath = NSTemporaryDirectory() + "exportShare.zip"
//    NSFileManager.defaultManager.copyItemAtPath(fullPath, tempPath, null)
//
//    val fileUrl = NSURL.fileURLWithPath(tempPath)
//    FileKit.shareFile(PlatformFile(Path(fileUrl.path()?:"")), shareSettings = FileKitShareSettings())
//}
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual suspend fun shareBytes(bytes: ByteArray) {
    val nsData = bytes.usePinned {
        NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong())
    }

//    val fileUrl = NSURL.fileURLWithPath(fullPath)

    dispatch_async(dispatch_get_main_queue()) {
        val controller = UIActivityViewController(
            activityItems = listOf(nsData),
            applicationActivities = null
        )

        val vc = topViewController() ?: return@dispatch_async

        // 🔥 iPad fix
        controller.popoverPresentationController?.sourceView = vc.view
        controller.popoverPresentationController?.sourceRect = vc.view.bounds
        controller.popoverPresentationController?.permittedArrowDirections = 0u

        vc.presentViewController(
            controller,
            animated = true,
            completion = null
        )
    }
}