package com.moly3.cedarjam.core.ui.func

import androidx.compose.runtime.Composable
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientation

@Composable
actual fun rememberDeviceOrientation(): DeviceOrientation {
    val orientation = UIDevice.currentDevice.orientation

    return when (orientation) {
        UIDeviceOrientation.UIDeviceOrientationLandscapeLeft,
        UIDeviceOrientation.UIDeviceOrientationLandscapeRight -> DeviceOrientation.Landscape
        else -> DeviceOrientation.Portrait
    }
}