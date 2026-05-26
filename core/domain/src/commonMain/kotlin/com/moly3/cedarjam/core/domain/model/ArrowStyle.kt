package com.moly3.cedarjam.core.domain.model

import com.moly3.dataviz.core.graph.model.ArrowHead
import com.moly3.dataviz.core.graph.model.ArrowStyle
import com.moly3.dataviz.core.graph.model.LineStyle


fun GraphEdgeType.arrowStyle(): ArrowStyle = when (this) {
    is GraphEdgeType.FileLink -> when (kind) {
        RelationKind.Association -> ArrowStyle(ArrowHead.Open, LineStyle.Solid)
        RelationKind.Aggregation -> ArrowStyle(ArrowHead.HollowDiamond, LineStyle.Solid)
        RelationKind.Composition -> ArrowStyle(ArrowHead.FilledDiamond, LineStyle.Solid)
        RelationKind.Inheritance -> ArrowStyle(ArrowHead.HollowTriangle, LineStyle.Solid)
        RelationKind.Realization -> ArrowStyle(ArrowHead.HollowTriangle, LineStyle.Dashed)
        RelationKind.Dependency -> ArrowStyle(ArrowHead.Open, LineStyle.Dashed)
        RelationKind.Reference -> ArrowStyle(ArrowHead.Open, LineStyle.Dotted)
    }

    GraphEdgeType.None -> ArrowStyle.Default
}