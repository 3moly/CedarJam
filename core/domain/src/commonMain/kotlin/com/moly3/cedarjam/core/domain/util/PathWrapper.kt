package com.moly3.cedarjam.core.domain.util

import kotlinx.io.files.Path

class PathWrapper(private val path: Path) : IPathWrapper {
    override val pathString: String get() = path.toString()

    override fun parent(): IPathWrapper? =
        path.parent?.let { PathWrapper(it) }

    override fun name(): String = path.name

    override fun toString(): String {
        return pathString
    }
}