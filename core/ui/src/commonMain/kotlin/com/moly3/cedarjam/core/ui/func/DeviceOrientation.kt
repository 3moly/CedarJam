package com.moly3.cedarjam.core.ui.func

import androidx.compose.runtime.Composable

enum class DeviceOrientation {
    Portrait,
    Landscape
}

@Composable
expect fun rememberDeviceOrientation(): DeviceOrientation