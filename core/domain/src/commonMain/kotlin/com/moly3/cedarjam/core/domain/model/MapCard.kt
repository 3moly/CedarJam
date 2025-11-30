package com.moly3.cedarjam.core.domain.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import kotlinx.serialization.Serializable

//data class MapCard(
//    val id: Long,
//    val position: WorldPosition,
//    val size: Offset,
//    val data: FileData? = null,
//    val color: Color? = null
//)


sealed class FileData {
    data class Text(val text: String) : FileData()

    data class FNode(val relativeToFilePath: String, val fileType: FileType) : FileData()
}

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

                "png", "jpeg", "jpg", "svg" -> {
                    FileType.Image(fileNode)
                }

                else -> {
                    FileType.Unknown
                }
            }
        }
    }
}

//suspend fun FileDataJson.toFileNodeJson(
//    workspaceSession: WorkspaceSession,
//    workspace: WorkspacePresentation,
//    filesRepository: IFilesRepository,
//    appEnvironment: IAppEnvironment,
//    fileNodes: List<FileTreeNode>
//): FileData {
//    return when (this) {
//        is FileDataJson.FileNode -> {
//            val relativePath = this.relativeToFilePath
//
//            try {
//                val path = pathWrapper(workspace.absolutePath, relativePath)
//                val foundFile: FileTreeNode? = findFiles(fileNodes, path.toString())
//
//                if (foundFile != null) {
//
//                    FileData.FNode(
//                        relativePath, foundFile.toGetFileType(
////                            filePageType = FilePageType.Default,
//                            filesRepository = filesRepository,
//                        )
//                    )
//                } else {
//                    FileData.FNode(relativePath, FileType.Unknown)
//                }
//            } catch (exc: Exception) {
//                FileData.FNode(relativePath, FileType.Unknown)
//            }
//        }
//
//        is FileDataJson.Text -> FileData.Text(this.text)
//    }
//}
//
//fun FileData.toFileNodeJson(workspace: WorkspacePresentation): FileDataJson {
//    return when (this) {
//        is FileData.FNode -> {
////            val relativePath = this.fileNode
////                ?.getFullPathStr()
////                ?.replaceFirst(workspace.fullpath, "")
////                ?.replaceFirst("/", "")
////
////            if (relativePath == null) {
////                val msg = ""
////            }
//
//            FileDataJson.FileNode(this.relativeToFilePath)
//        }
//
//        is FileData.Text -> {
//            FileDataJson.Text(this.text)
//        }
//    }
//}

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

//@Serializable
//data class MapCardJson(
//    val id: Long,
//    val position: WorldPosition,
//    val size: OffsetData,
//    val color: String? = null,
//    val fileData: FileDataJson? = null
//)