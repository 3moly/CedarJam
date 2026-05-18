package com.moly3.cedarjam.core.ui.func

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.func.getPlatform
import com.moly3.cedarjam.core.domain.model.Platform
import com.moly3.cedarjam.core.ui.compositions.LocalIsRelease
import kotlinx.datetime.format.Padding

const val topStatusBarPadding = 0
const val windowToolbarPadding = 40
const val bottomNavigationBarPadding = 56

private val DesktopStatusBarInsets
    @Composable
    get() = WindowInsets(
        top = with(LocalDensity.current) { topStatusBarPadding.dp.roundToPx() }
    )

private val DesktopWindowToolbarInsets
    @Composable
    get() = when (getPlatform()) {
        Platform.Android,
        Platform.Ios,
        Platform.Wasm -> WindowInsets()

        Platform.Jvm -> WindowInsets(
            top = with(LocalDensity.current) { windowToolbarPadding.dp.roundToPx() }
        )
    }

private val DesktopNavigationBarsInsets
    @Composable
    get() = if (!LocalIsRelease.current) {
        WindowInsets(
            bottom = with(LocalDensity.current) { bottomNavigationBarPadding.dp.roundToPx() }
        )
    } else
        WindowInsets()

@Composable
fun Modifier.compactOnlyStatusBar(): Modifier {
    return if (isCompactUI()) {
        this.statusBarsPaddingCJ()
    } else
        this.consumeWindowInsets(DesktopStatusBarInsets)
}

@Composable
fun Modifier.wstatusBarsPaddingCJ(): Modifier {
    return this.then(
        when (getPlatform()) {
            Platform.Jvm -> Modifier
                .windowInsetsPadding(DesktopStatusBarInsets)
                .consumeWindowInsets(DesktopStatusBarInsets)

            Platform.Wasm,
            Platform.Android,
            Platform.Ios -> Modifier.statusBarsPadding()
        }
    ).windowToolbarPaddingCJ()
}

@Composable
fun Modifier.statusBarsPaddingCJ(): Modifier {
    return this.then(
        when (getPlatform()) {
            Platform.Jvm -> Modifier
                .windowInsetsPadding(DesktopStatusBarInsets)
                .consumeWindowInsets(DesktopStatusBarInsets)

            Platform.Wasm,
            Platform.Android,
            Platform.Ios -> Modifier.statusBarsPadding()
        }
    )
}

//
@Composable
fun Modifier.consumeWindowToolbarPaddingCJ(): Modifier {
    return this.then(
        when (getPlatform()) {
            Platform.Jvm -> Modifier.consumeWindowInsets(DesktopWindowToolbarInsets)
            Platform.Wasm,
            Platform.Android,
            Platform.Ios -> Modifier
        }
    )
}

fun PaddingValues.plus(
    layoutDirection: LayoutDirection,
    paddingValues: PaddingValues
): PaddingValues {
    return PaddingValues(
        top = this.calculateTopPadding() + paddingValues.calculateTopPadding(),
        start = this.calculateStartPadding(layoutDirection) + paddingValues.calculateStartPadding(layoutDirection),
        end = this.calculateEndPadding(layoutDirection) + paddingValues.calculateEndPadding(layoutDirection),
        bottom = this.calculateBottomPadding() + paddingValues.calculateBottomPadding(),
    )
}

@Composable
fun Modifier.windowToolbarPaddingCJ(): Modifier {
    return this.then(
        if (isCompactUI()) {
            Modifier.windowInsetsPadding(DesktopWindowToolbarInsets)
        } else
            Modifier
    )
}

@Composable
fun Modifier.navigationBarsPaddingCJ(): Modifier {
    return this.then(
        when (getPlatform()) {
            Platform.Jvm -> Modifier
                .windowInsetsPadding(DesktopNavigationBarsInsets)
                .consumeWindowInsets(DesktopNavigationBarsInsets)

            Platform.Wasm,
            Platform.Android,
            Platform.Ios -> Modifier.navigationBarsPadding()
        }
    )
}

@Composable
fun navigationBarsPaddingValuesCJ(): PaddingValues {
    val insets = when (getPlatform()) {
        Platform.Jvm -> DesktopNavigationBarsInsets // Assuming this is a WindowInsets object
        Platform.Wasm,
        Platform.Android,
        Platform.Ios -> WindowInsets.navigationBars
    }

    // Converts the insets into PaddingValues that automatically update
    // when the screen metrics change.
    return insets.asPaddingValues()
}

@Composable
fun Modifier.imePaddingCJ(): Modifier {
    return this.then(
        when (getPlatform()) {
            Platform.Jvm -> Modifier
            Platform.Wasm,
            Platform.Android,
            Platform.Ios -> Modifier.imePadding()
        }
    )
}