package com.moly3.cedarjam.core.domain.func

import com.moly3.cedarjam.core.domain.util.IPathWrapper
import com.moly3.cedarjam.core.domain.util.PathWrapper
import kotlinx.io.files.Path

actual fun pathWrapper(vararg parts: String): IPathWrapper {
    return PathWrapper(Path(base = "", parts = parts))
}