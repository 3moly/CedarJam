package com.moly3.cedarjam.core.domain.func

import com.moly3.cedarjam.core.domain.util.JsPathWrapper
import com.moly3.cedarjam.core.domain.util.IPathWrapper

actual fun pathWrapper(vararg parts: String): IPathWrapper =
    JsPathWrapper.fromParts(parts)