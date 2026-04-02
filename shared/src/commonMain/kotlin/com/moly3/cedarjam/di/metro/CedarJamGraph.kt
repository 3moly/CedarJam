package com.moly3.cedarjam.di.metro

object CedarJamGraph {
    lateinit var instance: AppGraph
        private set

    val deps: CedarJamDependencies
        get() = instance.cedarJamDependencies

    fun init(graph: AppGraph) {
        instance = graph
    }
}
