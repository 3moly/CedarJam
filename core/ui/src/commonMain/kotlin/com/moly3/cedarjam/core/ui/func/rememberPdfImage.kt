package com.moly3.cedarjam.core.ui.func

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.moly3.cedarjam.core.domain.func.hiddenDirectory
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.service.IFileHasher
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.koin.mp.KoinPlatform.getKoin

object PdfPreviewLoader {

    private val mutexMap = mutableMapOf<String, Mutex>()

    suspend fun loadOrGenerate(
        fullPath: String,
        fileToSave: String
    ): String {

        val mutex = mutexMap.getOrPut(fileToSave) { Mutex() }

        return mutex.withLock {

            // If another coroutine already generated it
            if (SystemFileSystem.exists(Path(fileToSave))) {
                return fileToSave
            }

            val bitmap = getPdfImage(
                fullPath,
                page = 0,
                density = 0.1f
            ) ?: error("Failed to render PDF")

            bitmap.saveAsPng(fileToSave)

            fileToSave
        }
    }
}

@Composable
fun rememberPdfImage(
    workspaceFullPath: String?,
    fullPath: String?
): String? {

    var previewPath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(fullPath, workspaceFullPath) {
        val loader: PdfPreviewLoader = PdfPreviewLoader
        if (fullPath == null || workspaceFullPath == null) {
            previewPath = null
            return@LaunchedEffect
        }

        val fs = getKoin().get<IFileHasher>()
        launch(io) {
            val hash = fs.getFileHash(fullPath)
            val fileToSave = pathWrapper(
                workspaceFullPath,
                hiddenDirectory,
                "image_cache",
                "pdf_preview_$hash.png"
            ).pathString

            previewPath = loader.loadOrGenerate(
                fullPath,
                fileToSave
            )
        }
    }

    return previewPath
}