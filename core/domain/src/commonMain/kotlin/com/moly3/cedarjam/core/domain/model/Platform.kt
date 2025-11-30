package com.moly3.cedarjam.core.domain.model

sealed class Platform {
    data object Jvm: Platform()

    data object Android : Platform()
    data object Ios : Platform()
    data object Wasm : Platform()
}