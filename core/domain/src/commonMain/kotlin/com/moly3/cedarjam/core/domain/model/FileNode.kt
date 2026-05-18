package com.moly3.cedarjam.core.domain.model

import com.moly3.cedarjam.core.domain.func.hiddenDirectory
import com.moly3.cedarjam.core.domain.func.normalizeText
import com.moly3.cedarjam.core.domain.func.pathWrapper
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable

@Serializable
data class FileName(
    val name: String,
    val extension: String?
)

fun String?.toFileType(): FileTypeExt {
    return when (this?.lowercase()) {
        "png", "jpeg", "jpg", "heic", "svg", "webp" -> {//, "svg", "webp"
            FileTypeExt.Image
        }

        "mp4", "mov" -> FileTypeExt.Video
        "pdf" -> FileTypeExt.Pdf

        "md", "txt" -> FileTypeExt.Text
        "canvas" -> FileTypeExt.Canvas

        else -> FileTypeExt.None
    }
}

enum class FileTypeExt {
    None,
    Pdf,
    Image,
    Mid,
    Video,
    Canvas,
    Text
}

@Serializable
sealed class FileTreeNode {

    fun isDirectory(): Boolean {
        return this is Directory
    }

    fun getChildrenOrNull(): List<FileTreeNode>? {
        return when (this) {
            is Directory -> this.children
            is File -> null
        }
    }

    abstract val fileSize: Long
    abstract val modifiedTime: Long
    abstract val createdTime: Long
    abstract val workspaceFullPath: String
    abstract val parentRelativePath: String
    abstract fun getShortName(): String
    abstract fun getFullName(): String
    abstract fun getExtension(): String?
    abstract fun getFullPath(): String
    abstract fun getRelativePath(): String

    @Serializable
    data class File(
        val name: FileName,
        override val workspaceFullPath: String,
        override val parentRelativePath: String,
        override val createdTime: Long = 0L,
        override val modifiedTime: Long = 0L,
        override val fileSize: Long = 0L,
    ) : FileTreeNode() {
        override fun getShortName(): String {
            return name.name
        }

        override fun getFullName(): String {
            if (name.extension.isNullOrEmpty()) {
                return name.name
            }
            return buildString {
                append(name.name)
                append(".")
                append(name.extension)
            }
        }

        override fun getExtension(): String? {
            return name.extension
        }

        override fun getRelativePath(): String {
            return pathWrapper(parentRelativePath, getFullName()).pathString.normalizeText()
        }

        override fun getFullPath(): String {
            return pathWrapper(
                workspaceFullPath,
                parentRelativePath,
                getFullName()
            ).pathString.normalizeText()
        }
    }

    @Serializable
    data class Directory(
        val name: String,
        override val parentRelativePath: String,
        override val workspaceFullPath: String,
        val children: List<FileTreeNode>,
        override val createdTime: Long = 0L,
        override val modifiedTime: Long = 0L,
        override val fileSize: Long,
    ) : FileTreeNode() {
        companion object {
            fun create(
                workspacePath: String,
                fullPath: Path,
                children: List<FileTreeNode> = listOf()
            ): Directory {
                return create(workspacePath, fullPath.toString(), children = children)
            }

            fun create(
                workspacePath: String,
                relativePath: String,
                children: List<FileTreeNode> = listOf()
            ): Directory {
                val name = if (relativePath.contains("/")) {
                    relativePath.substringAfterLast("/")
                } else {
                    ""
                }
                val parentPath = if (relativePath.contains("/")) {
                    relativePath.substringBeforeLast("/")
                } else {
                    relativePath.substringBeforeLast("/")
                }
                return Directory(
                    name = name,
                    workspaceFullPath = workspacePath,
                    parentRelativePath = parentPath,
                    children = children,
                    fileSize = children.sumOf { x -> x.fileSize }
                )
            }
        }

        override fun getRelativePath(): String {
            return pathWrapper(parentRelativePath, getFullName()).pathString
        }

        override fun getShortName(): String {
            return name
        }

        override fun getFullName(): String {
            return name
        }

        override fun getExtension(): String? {
            return null
        }


        override fun getFullPath(): String {
            return pathWrapper(
                workspaceFullPath,
                getRelativePath(),
            ).toString()
        }
    }

    companion object {
        fun List<FileTreeNode>.getHiddenNodes(
            directoryName: String
        ): List<FileTreeNode> {
            val files = this.firstOrNull { d ->
                d.getFullName() == hiddenDirectory && d.isDirectory()
            }?.getChildrenOrNull()?.firstOrNull { d ->
                d.getFullName() == directoryName && d.isDirectory()
            }?.getChildrenOrNull() ?: listOf()
            return files
        }

        fun List<FileTreeNode>.getAllDirectories(): List<Directory> {
            val directories = mutableListOf<Directory>()
            for (item in this) {
                when (item) {
                    is Directory -> {
                        directories.add(item)
                        directories.addAll(item.children.getAllDirectories())
                    }

                    is File -> {}
                }
            }
            return directories
        }

        fun List<FileTreeNode>.getAllFilesByExtension(
            extension: String?,
            excludeExtensions: List<String> = listOf()
        ): List<File> {
            val textFiles = mutableListOf<File>()
            for (item in this) {
                when (item) {
                    is Directory -> {
                        textFiles.addAll(
                            item.children.getAllFilesByExtension(
                                extension,
                                excludeExtensions = excludeExtensions
                            )
                        )
                    }

                    is File -> {
                        if (excludeExtensions.contains(item.name.extension)) {
                            val msg = ""
                        } else if (item.name.extension == extension || extension == null) {
                            textFiles.add(item)
                        }
                    }
                }
            }
            return textFiles
        }

        fun List<FileTreeNode>.getAll(isSkipOwnNode: Boolean = false): List<FileTreeNode> {
            val files = mutableListOf<FileTreeNode>()
            for (item in this) {
                if (!isSkipOwnNode) {
                    files.add(item)
                }
                when (item) {
                    is Directory -> {
                        files.addAll(item.children.getAll())
                    }

                    is File -> {
                    }
                }
            }
            return files
        }

        fun List<FileTreeNode>.hideHiddenDirectory(): List<FileTreeNode> {
            return this.filter { x -> x.getFullName() != hiddenDirectory }
        }
    }
}