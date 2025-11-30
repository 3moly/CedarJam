package com.moly3.cedarjam.core.domain.util

interface IPathWrapper {
    val pathString: String

    fun parent(): IPathWrapper?
    fun name(): String
}