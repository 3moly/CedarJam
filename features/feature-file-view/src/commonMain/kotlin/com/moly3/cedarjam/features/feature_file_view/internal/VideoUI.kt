package com.moly3.cedarjam.features.feature_file_view.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.model.FileTreeNode.Companion.getAllFilesByExtension
import com.moly3.cedarjam.core.domain.model.FileType
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalVideoPlayer
import com.moly3.cedarjam.core.ui.uikit.CJSlider
import com.moly3.cedarjam.core.ui.uikit.CJText
import io.github.kdroidfilter.composemediaplayer.SubtitleTrack
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState
import io.github.vinceglb.filekit.PlatformFile

@Composable
internal fun BoxScope.VideoUI(
    workspaceSession: WorkspaceSession,
    fl: FileType.Video
) {
    val files by workspaceSession.getFileNodes.collectAsState(UIState.Loading)
    val playerState = rememberVideoPlayerState()
    VideoPlayerSurface(
        playerState = playerState,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit // Default is ContentScale.Fit,
    )
//    val player = rememberMediampPlayer()
//    val scope = rememberCoroutineScope()
//    Column {
//        Button(onClick = {
//            scope.launch {
//                player.playUri("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4")
//            }
//        }) {
//            Text("Play")
//        }

//        MediampPlayerSurface(player, Modifier.fillMaxSize())
//    }
//}
//    Column(
//        modifier = Modifier.align(Alignment.BottomCenter),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        CJText(
//            modifier = Modifier.background(LocalAppTheme.current.colors.backgroundPrimary),
//            text = "metadata:\n" +
//                    "${playerState.metadata.title}\n" +
//                    "${playerState.positionText}\n"
//        )
//        CJSlider(
//            modifier = Modifier.padding(bottom = 32.dp),
//            value = playerState.sliderPos,
//            onValueChange = {
//                playerState.sliderPos = it
//                playerState.userDragging = true
//                println("Position changed: $it")
//            },
//            onValueChangeFinished = {
//                playerState.userDragging = false
//                playerState.seekTo(playerState.sliderPos)
//                println("Position finalized: ${playerState.sliderPos}")
//            },
//            valueRange = 0f..1000f
//        )
//    }
    LaunchedEffect(files, fl) {
        try {
            if (playerState.isPlaying) {
                playerState.stop()
                playerState.dispose()
            }
//            player.playUri("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4")
            //playerState.openFile(PlatformFile(fl.fileNode.getFullPath()))
//            playerState.openUri("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4")

            val srts = (files.getOrDefault(listOf())).getAllFilesByExtension("srt")
            val foundSrt =
                srts.firstOrNull { d -> d.getShortName() == fl.fileNode.getShortName() }
            if (foundSrt != null) {
//                        val files = workspaceEnv!!.getFilesFlow().value
                val track = SubtitleTrack(
                    label = "English Subtitles",
                    language = "en",
                    src = foundSrt.getFullPath() // Works with both .srt and .vtt files
                )
                playerState.dispose()
                playerState.selectSubtitleTrack(track)
            } else {
                //playerState.disableSubtitles()
            }


        } catch (exc: Exception) {
            println("exc: ${exc.message}")
        }
    }
}