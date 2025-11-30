package com.moly3.cedarjam.core.domain.model

import com.moly3.cedarjam.core.domain.func.hiddenDirectory
import com.moly3.cedarjam.core.domain.func.pathWrapper
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable

@Serializable
data class FileName(
    val name: String,
    val extension: String?
)

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
    abstract val parentPath: String
    abstract fun getShortName(): String
    abstract fun getFullName(): String
    abstract fun getFullPath(): String

    @Serializable
    data class File(
        val name: FileName,
        override val parentPath: String,
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

        override fun getFullPath(): String {
            val fullName = getFullName()
            if (fullName.isEmpty())
                return parentPath

            return pathWrapper(
                parentPath,
                getFullName()
            ).toString()
        }
    }

    @Serializable
    data class Directory(
        val name: String,
        override val parentPath: String,
        val children: List<FileTreeNode>,
        override val createdTime: Long = 0L,
        override val modifiedTime: Long = 0L,
        override val fileSize: Long,
    ) : FileTreeNode() {
        companion object {
            fun create(fullPath: Path, children: List<FileTreeNode> = listOf()): Directory {
                return create(fullPath.toString(), children = children)
            }

            //, fileSize: Long = 0L
            fun create(fullPath: String, children: List<FileTreeNode> = listOf()): Directory {
                val name = if (fullPath.contains("/")) {
                    fullPath.substringAfterLast("/")
                } else {
                    ""
                }
                val parentPath = if (fullPath.contains("/")) {
                    fullPath.substringBeforeLast("/")
                } else {
                    fullPath.substringBeforeLast("/")
                }
                return Directory(
                    name = name,
                    parentPath = parentPath,
                    children = children,
                    fileSize = children.sumOf { x -> x.fileSize }
                )
            }
        }

        override fun getShortName(): String {
            return name
        }

        override fun getFullName(): String {
            return name
        }

        override fun getFullPath(): String {
            return pathWrapper(
                parentPath,
                getFullName()
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
            val textFiles = mutableListOf<FileTreeNode>()
            for (item in this) {
                if (!isSkipOwnNode)
                    textFiles.add(item)
                when (item) {
                    is Directory -> {
                        textFiles.addAll(item.children.getAll())
                    }

                    is File -> {
                    }
                }
            }
            return textFiles
        }

        fun List<FileTreeNode>.hideHiddenDirectory(): List<FileTreeNode> {
            return this.filter { x -> x.getFullName() != hiddenDirectory }
        }

        fun List<FileTreeNode>.sortByIsDirectory(): List<FileTreeNode> {
            return this.sortedWith(compareBy<FileTreeNode> { !it.isDirectory() }
                .thenBy { it.getFullName() })
        }

        fun List<FileTreeNode>.hideDsStore(): List<FileTreeNode> {
            return this.filter { x -> x.getFullName().lowercase() != ".DS_Store".lowercase() }
                .sortedWith(compareBy<FileTreeNode> { !it.isDirectory() }
                    .thenBy { it.getFullName() })
        }
    }
}