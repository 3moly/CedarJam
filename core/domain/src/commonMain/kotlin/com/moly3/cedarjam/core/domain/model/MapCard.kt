package com.moly3.cedarjam.core.domain.model

import androidx.compose.ui.geometry.Offset
import com.moly3.cedarjam.core.domain.model.FileType.*
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import kotlinx.serialization.Serializable


fun findFiles(files: List<FileTreeNode>, searchFullPath: String): FileTreeNode? {
    var foundName: FileTreeNode? = null
    for (item in files) {
        if (item.getFullPath() == searchFullPath) {
            foundName = item
            break
        }
        if (item.isDirectory() && item.getChildrenOrNull()?.isNotEmpty() == true) {
            foundName = findFiles(item.getChildrenOrNull()!!, searchFullPath)
            if (foundName != null)
                break
        }
    }
    return foundName
}

fun FileTreeNode.toGetFileType(
    fileNode: FileTreeNode = this,
    filesRepository: IFilesRepository,
): FileType {
    return when (fileNode) {
        is FileTreeNode.Directory -> FileType.Unknown
        is FileTreeNode.File -> {
            when (fileNode.name.extension.toFileType()) {
                FileTypeExt.None -> FileType.Unknown
                FileTypeExt.Pdf -> PDF(fileNode, currentPage = 1)
                FileTypeExt.Image -> Image(fileNode)
                FileTypeExt.Canvas -> FileType.Canvas(fileNode = fileNode)
                FileTypeExt.Video -> Video(fileNode)
                FileTypeExt.Text -> {
                    try {
                        val text = filesRepository.getNodeText(fileNode)
                        Text(text.getValueOrNull() ?: "")
                    } catch (exc: Exception) {
                        Text("")
                    }
                }

                FileTypeExt.Mid -> FileType.MIDI(fileNode)
            }
        }
    }
}

@Serializable
sealed class FileDataJson {
    @Serializable
    data class Text(val text: String) : FileDataJson()

    @Serializable
    data class FileNode(val relativeToFilePath: String) : FileDataJson()
}

@Serializable
data class OffsetData(
    val x: Float,
    val y: Float
) {
    companion object {
        val Zero = OffsetData(0f, 0f)
    }
}

fun Offset.mapToOffsetData(): OffsetData {
    return OffsetData(
        x = this.x,
        y = this.y
    )
}

fun OffsetData.mapToOffset(): Offset {
    return Offset(
        x = this.x,
        y = this.y
    )
}