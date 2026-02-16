package com.moly3.cedarjam.core.ui.func

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

@Composable
actual fun rememberDeviceOrientation(): DeviceOrientation {
    val configuration = LocalConfiguration.current

    return when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> DeviceOrientation.Landscape
        else -> DeviceOrientation.Portrait
    }
}