package com.moly3.cedarjam.core.ui.compositions

import androidx.compose.runtime.compositionLocalOf
import io.github.kdroidfilter.composemediaplayer.VideoPlayerState
import io.github.kdroidfilter.composemediaplayer.createVideoPlayerState

val LocalVideoPlayer = compositionLocalOf { createVideoPlayerState() }
