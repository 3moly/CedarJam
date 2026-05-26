package com.moly3.cedarjam.shared.di.metro

object CedarJamGraph {
    lateinit var instance: com.moly3.cedarjam.shared.di.metro.AppGraph
        private set

    val deps: com.moly3.cedarjam.shared.di.metro.CedarJamDependencies
        get() = instance.cedarJamDependencies

    fun init(graph: com.moly3.cedarjam.shared.di.metro.AppGraph) {
        instance = graph
    }
}
