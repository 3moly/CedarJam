package com.moly3.cedarjam.core.domain.model


fun FileTreeNode.getGraphId(): String {
    return getFullPath().getFileTreeNodeGraphId()
}

fun String.getFileTreeNodeGraphId(): String {
    return "filenode: $this"
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