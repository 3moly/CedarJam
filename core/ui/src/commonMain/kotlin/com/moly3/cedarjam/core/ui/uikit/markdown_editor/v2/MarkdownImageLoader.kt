package com.moly3.cedarjam.core.ui.uikit.markdown_editor.v2

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

/**
 * A host-provided composable that renders an image from a URL.
 *
 * Compose Multiplatform ships no network image loader, so the editor stays
 * loader-agnostic: provide one via [LocalMarkdownImageLoader] (Coil 3, Kamel, …).
 * When none is provided, [com.moly3.cedarjam.core.domain.features.mdprops.RowType.Image] rows fall back to a URL placeholder.
 */
typealias MarkdownImageLoader = @Composable (url: String, modifier: Modifier) -> Unit

val LocalMarkdownImageLoader = staticCompositionLocalOf<MarkdownImageLoader?> { null }