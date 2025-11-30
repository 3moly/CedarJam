package com.moly3.cedarjam.core.domain.func

import com.moly3.cedarjam.core.domain.model.Platform

actual fun getPlatform(): Platform {
  return Platform.Wasm
}