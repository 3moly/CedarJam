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

fun CollectionId.getCollectionGraphId(): String {
    return "collection: ${this.value}"
}

fun AnnotationId.getAnnotationGraphId(): String {
    return "annotation: ${this.value}"
}

fun CollectionRowDTO.getGraphId(): String {
    return id.getCollectionRowGraphId()
}

fun RowId.getCollectionRowGraphId(): String {
    return "collection_row: ${this.value}"
}

fun TagDTO.getGraphId(): String {
    return id.getTagGraphId()
}

fun TagId.getTagGraphId(): String {
    return "tag: ${this.value}"
}