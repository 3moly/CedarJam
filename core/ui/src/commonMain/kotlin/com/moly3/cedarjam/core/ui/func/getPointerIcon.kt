package com.moly3.cedarjam.core.ui.func

import androidx.compose.ui.input.pointer.PointerIcon

expect fun getPointerIcon(type: PointerIconType): PointerIcon

enum class PointerIconType {
    Default,
    Hand,
    ResizeHorizontal,
    ResizeVertical,
    ResizeTopLeft,
    ResizeTopRight,
    ResizeBottomLeft,
    ResizeBottomRight
}