package com.moly3.cedarjam.core.domain.model

sealed interface NavigateToFile {
    data class RelativePath(val value: String) : NavigateToFile
    data class File(val value: FileTreeNode) : NavigateToFile
}