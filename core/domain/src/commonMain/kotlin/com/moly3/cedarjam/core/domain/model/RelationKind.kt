package com.moly3.cedarjam.core.domain.model

enum class RelationKind(val keyword: String) {
    Association("association"),
    Aggregation("aggregation"),
    Composition("composition"),
    Inheritance("inheritance"),
    Realization("realization"),
    Dependency("dependency"),
    Reference("reference");

    companion object {
        private val byKeyword = entries.associateBy { it.keyword }
        fun fromKeyword(s: String?): RelationKind =
            s?.lowercase()?.let { byKeyword[it] } ?: Association
    }
}