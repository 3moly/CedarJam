package com.moly3.cedarjam.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalImageLoader
import com.moly3.cedarjam.core.ui.compositions.LocalTextStyle
import com.moly3.cedarjam.core.ui.uikit.CJText

import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun FlowerCard(
    modifier: Modifier,
    painter: Painter? = null,
    blurRadius: Int = 4,
    webLink: String? = null,
    youtubeLink: String? = null,
    documentImage: ImageBitmap? = null,
    isOneWord: Boolean = false,
    text: String,
    currentProgress: Int? = null,
    progressMax: Int? = null,
    onWebClick: (String) -> Unit = {},
    onClick: () -> Unit,
) {

    val hazeState = rememberHazeState(blurEnabled = false)
    val hazeStyle = remember {
        HazeStyle(
            backgroundColor = Color.Black,
            tints = listOf(HazeTint(Color.Green.copy(0.1f))),
            blurRadius = blurRadius.dp,
            noiseFactor = HazeDefaults.noiseFactor
        )
    }
    Box(
        modifier = modifier
            .size(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxSize().hazeSource(state = hazeState, zIndex = 1f)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState),
                contentAlignment = Alignment.Center
            ) {
                if (painter != null) {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )
                }
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Green.copy(alpha = 0.1f))
                    .hazeEffect(state = hazeState, style = hazeStyle)
            )
        }
        if (youtubeLink != null) {
            Image(
                rememberAsyncImagePainter(
                    model = youtubeLink,
                    imageLoader = LocalImageLoader.current
                ), contentDescription = null
            )
        } else if (documentImage != null) {
            Image(
                bitmap = documentImage,
                contentDescription = null
            )
        }
        if (webLink != null) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(32.dp)
                    .align(Alignment.TopStart)
                    .background(Color.Blue, shape = RoundedCornerShape(4.dp))
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        onWebClick(webLink)
                    }
            ) {
                Image(
                    modifier = Modifier.size(12.dp),
                    painter = rememberVectorPainter(vectors.Settings),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color.Green)
                )
            }
        }
        if (isOneWord) {
            CJText(
                text = currentProgress.toString(),
                fontSize = 50.sp
            )
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(Color.Green.copy(alpha = 0.1f))
                    .hazeEffect(state = hazeState, style = hazeStyle)
                    .padding(8.dp)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CJText(text, color = Color.Green)
                }
                if (progressMax != null && currentProgress != null) {
                    if (progressMax > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CJText(
                                text = currentProgress.toString(),
                                color = Color.Green,
                                style = LocalTextStyle.current,
                                fontSize = 10.sp
                            )
                            CJText(
                                text = progressMax.toString(),
                                color = Color.Green,
                                style = LocalTextStyle.current,
                                fontSize = 10.sp
                            )
                        }
//                       todo LinearProgressIndicator(
//                            progress = { currentProgress / progressMax.toFloat() },
//                            modifier = Modifier.fillMaxWidth(),
//                            color = ProgressIndicatorDefaults.linearColor,
//                            trackColor = ProgressIndicatorDefaults.linearTrackColor,
//                            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
//                            drawStopIndicator = {}
//                        )
                    }
                }
            }
        }
    }
}