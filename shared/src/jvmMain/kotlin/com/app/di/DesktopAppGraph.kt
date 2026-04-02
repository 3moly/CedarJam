package com.app.di

import com.moly3.cedarjam.di.metro.AppGraph
import com.moly3.cedarjam.di.metro.createCedarJamAppGraph

/** @deprecated Use [createCedarJamAppGraph] from the shared module. */
@Deprecated("Use createCedarJamAppGraph()", ReplaceWith("createCedarJamAppGraph()"))
fun createDesktopAppGraph(): AppGraph = createCedarJamAppGraph()
