package com.moly3.cedarjam.core.domain.func

import com.moly3.cedarjam.core.domain.model.FileTreeNode
import java.io.File

actual fun openFileInExplorer(fileNode: FileTreeNode) {
    val file = File(fileNode.getFullPath())
    if (!file.exists()) {
        println("File does not exist: ${file.absolutePath}")
        return
    }

    val os = System.getProperty("os.name").lowercase()
    try {
        when {
            os.contains("mac") -> {
                // Reveal file in Finder
                ProcessBuilder("open", "-R", file.absolutePath)
                    .start()
            }
            os.contains("win") -> {
                // Reveal file in Windows Explorer
                ProcessBuilder("explorer", "/select,", file.absolutePath)
                    .start()
            }
            os.contains("nux") || os.contains("nix") -> {
                // On Linux, best effort: open containing folder
                ProcessBuilder("xdg-open", file.parent)
                    .start()
            }
            else -> {
                println("OS not supported for reveal: $os")
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}