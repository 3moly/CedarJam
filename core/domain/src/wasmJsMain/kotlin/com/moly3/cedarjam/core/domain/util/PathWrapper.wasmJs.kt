package com.moly3.cedarjam.core.domain.util

class JsPathWrapper(private val segments: List<String>) : IPathWrapper {
    override val pathString: String get() = segments.joinToString("/")

    override fun parent(): IPathWrapper? =
        if (segments.isEmpty()) null else JsPathWrapper(segments.dropLast(1))

    override fun name(): String = segments.lastOrNull() ?: ""

    override fun toString(): String {
        return pathString
    }
    companion object {
        fun fromParts(parts: Array<out String>): JsPathWrapper =
            JsPathWrapper(parts.flatMap { it.split("/") }.filter { it.isNotEmpty() })
    }
}