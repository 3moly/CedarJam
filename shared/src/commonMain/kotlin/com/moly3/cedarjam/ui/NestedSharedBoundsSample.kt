package com.moly3.cedarjam.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.ui.vectors.BarLeft
import com.moly3.cedarjam.core.ui.vectors.FolderAdd
import com.moly3.cedarjam.core.ui.vectors.TrashEmpty

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NestedSharedBoundsSample() {
    // Nested shared bounds sample.
    val selectionColor = Color(0xff3367ba)
    var expanded by remember { mutableStateOf(true) }
    SharedTransitionLayout(
        Modifier
            .fillMaxSize()
            .clickable {
                expanded = !expanded
            }
            .background(Color(0x88000000))
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = EnterTransition.None,
            exit = ExitTransition.None
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(20.dp)
                        .sharedBounds(
                            rememberSharedContentState(key = "container"),
                            this@AnimatedVisibility
                        )
                        .requiredHeightIn(max = 60.dp)
                        .clip(RoundedCornerShape(50)),
                ) {
                    Row(
                        Modifier
                            .padding(10.dp),
                            // By using Modifier.skipToLookaheadSize(), we are telling the layout
                            // system to layout the children of this node as if the animations had
                            // all finished. This avoid re-laying out the Row with animated width,
                            // which is _sometimes_ desirable. Try removing this modifier and
                            // observe the effect.
                            //.skipToLookaheadSize(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Image(
                            rememberVectorPainter(FolderAdd),
                            contentDescription = "Share",
//                            modifier = Modifier.padding(
//                                top = 10.dp,
//                                bottom = 10.dp,
//                                start = 10.dp,
//                                end = 20.dp
//                            )
                        )
                        Image(
                            rememberVectorPainter(BarLeft),
                            contentDescription = "Favorite",
//                            modifier = Modifier.padding(
//                                top = 10.dp,
//                                bottom = 10.dp,
//                                start = 10.dp,
//                                end = 20.dp
//                            )
                        )
                        Image(
                            rememberVectorPainter(TrashEmpty),
                            contentDescription = "Create",
//                            tint = Color.White,
                            modifier = Modifier
                                .sharedBounds(
                                    rememberSharedContentState(key = "icon_background"),
                                    this@AnimatedVisibility
                                )
                                .background(selectionColor, RoundedCornerShape(50))
//                                .padding(
//                                    top = 10.dp,
//                                    bottom = 10.dp,
//                                    start = 20.dp,
//                                    end = 20.dp
//                                )
                                .sharedElement(
                                    rememberSharedContentState(key = "icon"),
                                    this@AnimatedVisibility
                                ),

                        )
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = !expanded,
            enter = EnterTransition.None,
            exit = ExitTransition.None
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(30.dp)
                        .sharedBounds(
                            rememberSharedContentState(key = "container"),
                            this@AnimatedVisibility,
                            enter = EnterTransition.None,
                        )
                        .sharedBounds(
                            rememberSharedContentState(key = "icon_background"),
                            this@AnimatedVisibility,
                            enter = EnterTransition.None,
                            exit = ExitTransition.None,
                            clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(30.dp))
                        ).clip(RoundedCornerShape(30.dp)).background(selectionColor, shape = RoundedCornerShape(30.dp)),
                ) {
                    Image(
                        painter = rememberVectorPainter(TrashEmpty),
                        contentDescription = "Create",
//                        tint = Color.White,
                        modifier = Modifier
                            .padding(30.dp)
                            .size(40.dp)
                            .sharedElement(
                                rememberSharedContentState(key = "icon"),
                                this@AnimatedVisibility
                            )
                    )
                }
            }
        }
    }
}