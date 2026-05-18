package com.moly3.cedarjam.core.domain.func

import com.moly3.dataviz.core.graph.hull.GroupSettings
import com.moly3.dataviz.core.graph.model.GraphSettings
import com.moly3.dataviz.core.graph.model.GraphViewSettings

fun GraphSettings.isGraphSettingsNudge(other: GraphSettings): Boolean {
    return this.view.isGraphSettingsNudge(other.view) ||
            this.groupSettings.isNudge(other.groupSettings)
}

fun GroupSettings.isNudge(other: GroupSettings): Boolean{
    if (this.enabled != other.enabled)
        return true
    if (this.cohesionForce != other.cohesionForce)
        return true
    if (this.groupSeparation != other.groupSeparation)
        return true
    if (this.groupSeparationSoftening != other.groupSeparationSoftening)
        return true
    if (this.hullRecomputeIntervalMs != other.hullRecomputeIntervalMs)
        return true
    if (this.hullSettledIntervalMs != other.hullSettledIntervalMs)
        return true
    if (this.hullK != other.hullK)
        return true
    if (this.hullPadding != other.hullPadding)
        return true
    if (this.hullSmoothing != other.hullSmoothing)
        return true
//    if (this.hullStrokeWidth != other.hullStrokeWidth)
//        return true
//    if (this.hullFill != other.hullFill)
//        return true
//    if (this.hullFillAlpha != other.hullFillAlpha)
//        return true
//    if (this.hullLabelVisibilityZoomThreshold != other.hullLabelVisibilityZoomThreshold)
//        return true
//    if (this.hullLabelVisibilityZoomFadeWidth != other.hullLabelVisibilityZoomFadeWidth)
//        return true
//    if (this.hullLabelFontSizeSp != other.hullLabelFontSizeSp)
//        return true
//    if (this.hullLabelScaleWithZoom != other.hullLabelScaleWithZoom)
//        return true
//    if (this.hullLabelMinScale != other.hullLabelMinScale)
//        return true
//    if (this.hullLabelMaxScale != other.hullLabelMaxScale)
//        return true
//    if (this.hullLabelVerticalOffset != other.hullLabelVerticalOffset)
//        return true
    return false
}

fun GraphViewSettings.isGraphSettingsNudge(other: GraphViewSettings): Boolean {
    if (this.centerForce != other.centerForce)
        return true
    if (this.linkForce != other.linkForce)
        return true
    if (this.linkDistance != other.linkDistance)
        return true
    if (this.repelForce != other.repelForce)
        return true
    if (this.connectedRepulsionMultiplier != other.connectedRepulsionMultiplier)
        return true
    if (this.unconnectedRepulsionMultiplier != other.unconnectedRepulsionMultiplier)
        return true
    if (this.longDistanceLinkMultiplier != other.longDistanceLinkMultiplier)
        return true
    if (this.clusteringForce != other.clusteringForce)
        return true
    if (this.minMutualConnectionsForClustering != other.minMutualConnectionsForClustering)
        return true
    if (this.maxForce != other.maxForce)
        return true
    if (this.dampingFactor != other.dampingFactor)
        return true
    if (this.maxConnectionsForFullProcessing != other.maxConnectionsForFullProcessing)
        return true
    if (this.spatialOptimizationThreshold != other.spatialOptimizationThreshold)
        return true
    if (this.hubExpansionExponent != other.hubExpansionExponent)
        return true
    return false
}