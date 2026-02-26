package com.moly3.cedarjam.core.domain.model

import com.moly3.cedarjam.core.domain.func.normalizeText


fun FileTreeNode.getGraphId(): String {
    return getRelativePath().getFileTreeNodeGraphId()
}

fun String.getFileTreeNodeGraphId(): String {
    return "filenode: ${this.normalizeText()}"
}

fun CollectionDTO.getGraphId(): String {
    return id.getCollectionGraphId()
}

fun AnnotationDTO.getGraphId(): String {
    return id.getAnnotationGraphId()
}

fun Long.getCollectionGraphId(): String {
    return "collection: $this"
}

fun Long.getAnnotationGraphId(): String {
    return "annotation: $this"
}

fun CollectionRowDTO.getGraphId(): String {
    return id.getCollectionRowGraphId()
}

fun Long.getCollectionRowGraphId(): String {
    return "collection_row: $this"
}

fun TagDTO.getGraphId(): String {
    return id.getTagGraphId()
}

fun Long.getTagGraphId(): String {
    return "tag: $this"
}