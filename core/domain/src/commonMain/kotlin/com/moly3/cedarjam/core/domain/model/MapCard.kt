package com.moly3.cedarjam.core.domain.model

import androidx.compose.ui.geometry.Offset
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
            when (fileNode.name.extension) {
                "md", "txt" -> {
                    try {
                        val text = filesRepository.getNodeText(fileNode)
                        FileType.Text(text.getValueOrNull() ?: "")
                    } catch (exc: Exception) {
                        FileType.Text("")
                    }
                }

                "canvas" -> {
                    FileType.Canvas(fileNode = fileNode)
                }

                "mid" -> {
                    FileType.MIDI(fileNode)
                }

                "pdf" -> {

                    FileType.PDF(fileNode, currentPage = 1)
                }

                "mp4" -> {
                    FileType.Video(fileNode)
                }

                "png", "jpeg", "jpg", "svg", "webp" -> {
                    FileType.Image(fileNode)
                }

                else -> {
                    FileType.Unknown
                }
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