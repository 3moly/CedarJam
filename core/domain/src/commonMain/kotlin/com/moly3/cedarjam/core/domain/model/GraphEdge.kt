package com.moly3.cedarjam.core.domain.model

sealed class GraphEdgeType {
    object DirectoryContainsFile : GraphEdgeType()
    data class FileLink(
        val relation: String? = null,
        val kind: RelationKind = RelationKind.Association,
    ) : GraphEdgeType()
    object AnnotationOnFile : GraphEdgeType()
    object AnnotationOnRow : GraphEdgeType()
    object RowInCollection : GraphEdgeType()
    object RowOnFile : GraphEdgeType()
    object TagOnRow : GraphEdgeType()
    object TagOnFile : GraphEdgeType()
    object TagToTag : GraphEdgeType()
}

data class GraphEdge(
    val target1: String,   // "from"
    val target2: String,   // "to"
    val type: GraphEdgeType,
)