package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.FileTypeExt
import com.moly3.cedarjam.core.domain.model.TimeMachine
import com.moly3.cedarjam.core.domain.model.toFileType
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.func.dpSize
import com.moly3.cedarjam.core.ui.func.rememberPdfImage
import com.moly3.cedarjam.pages.page_home.ui.internal.TimeMachineAnnotation
import com.moly3.lazyflow.FlowItemSize
import com.moly3.lazyflow.FlowOrientation
import com.moly3.lazyflow.items
import com.moly3.lazyflow.ui.LazyFlow
import com.moly3.lazyflow.ui.LazyFlowState
import vector.Data

@Composable
fun TimeMachineItem(
    modifier: Modifier,
    workspaceFullPath: String,
    item: TimeMachine,
    onClick: () -> Unit
) {
    NeumorphicShape(
        modifier = modifier,
        buttonShape = RoundedCornerShape(16.dp),
        strength = 0.01f,
        pressedIconColor = Color.Yellow,
        unpressedIconColor = Color.Red,
        isShowBigGradient = false,
        shadowConfig = NeumorphicShadowConfig(
            lightShadowRadius = 0.1f,
            darkShadowRadius = 0.1f,
            pressedShadowRadius = 14f,
            pressedShadowOffset = Offset(0f, 0f),
            elevationStrength = 10f
        ),
        content = {
            Box(Modifier.fillMaxSize()) {
                when (item) {
                    is TimeMachine.Annotation -> {
                        TimeMachineAnnotation(
                            workspaceFullPath = workspaceFullPath,
                            item = item
                        )
                    }

                    is TimeMachine.Collection -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Image(
                                painter = rememberVectorPainter(Data),
                                contentDescription = null,
                                modifier = Modifier.height(75.dp),
                                contentScale = ContentScale.FillHeight,
                                colorFilter = ColorFilter.tint(LocalAppTheme.current.colors.icon)
                            )
                            CJText(text = item.collection.name)
                        }
                    }

                    is TimeMachine.FileNode -> {
                        when (item.file.name.extension.toFileType()) {
                            FileTypeExt.None -> {}
                            FileTypeExt.Pdf -> {
                                val pdfImg = rememberPdfImage(
                                    workspaceFullPath = workspaceFullPath,
                                    item.file.getFullPath()
                                )
                                if (pdfImg != null) {
                                    AsyncImage(
                                        modifier = Modifier
                                            .padding(top = 16.dp)
                                            .align(Alignment.TopCenter)
                                            .width(75.dp),
                                        model = pdfImg,
                                        contentDescription = null,
                                        contentScale = ContentScale.FillWidth
                                    )
                                }
                            }

                            FileTypeExt.Image -> {
                                AsyncImage(
                                    model = item.file.getFullPath(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    filterQuality = FilterQuality.Low,
                                    contentScale = ContentScale.Crop
                                )
                            }

                            FileTypeExt.Video -> {}
                            FileTypeExt.Text -> {

                            }

                            FileTypeExt.Mid -> {}
                            FileTypeExt.Canvas -> {}
                        }
                        Box(
                            Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(40.dp)
                                .background(
                                    LocalAppTheme.current.colors.icon.copy(
                                        alpha = 0.8f
                                    )
                                )
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            CJText(text = item.file.getFullName())
                        }

                    }

                    is TimeMachine.Row -> {
                        val isPdf = remember(item.row) {
                            val relativePath = item.row.fileRelativePath
                            relativePath?.contains(".pdf") ?: false
                        }
                        if (isPdf) {
                            val ful = remember(
                                item.row.fileRelativePath,
                                workspaceFullPath
                            ) {
                                pathWrapper(
                                    workspaceFullPath,
                                    item.row.fileRelativePath ?: ""
                                ).pathString
                            }
                            val pdfImg = rememberPdfImage(workspaceFullPath, ful)
                            if (pdfImg != null) {
                                AsyncImage(
                                    modifier = Modifier.align(Alignment.TopCenter).width(75.dp),
                                    model = pdfImg,
                                    contentDescription = null,
                                    contentScale = ContentScale.FillWidth
                                )
                            }
                        } else {
                            CJText(text = "Row ${item.row}")
                        }
                    }

                    is TimeMachine.Tag -> {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp)
                                .fillMaxHeight(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val hashColor = remember(item.tag.color) {
                                if (item.tag.color.luminance() > 0.5f) {
                                    Color.Black
                                } else {
                                    Color.White
                                }
                            }
                            Box(
                                Modifier
                                    .size(40.dp)
                                    .background(item.tag.color, shape = CircleShape)
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                CJText(
                                    text = "#",
                                    color = hashColor, // Important
                                    fontSize = 24.sp,
                                    modifier = Modifier
                                )
                            }
                            Column {
                                CJText(
                                    text = "#${item.tag.name}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }) {
        onClick()
    }
}

@Composable
fun TimeMachineList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    workspaceFullPath: String,
    scrollState: LazyFlowState,
    list: List<TimeMachine>,
    onClick: (TimeMachine) -> Unit
) {
    val density = LocalDensity.current
    val size1 = remember(density) {
        DpSize(150.dp, 150.dp)
    }
    val size2 = remember(density) {
        DpSize(250.dp, 150.dp)
    }
    LazyFlow(
        modifier = modifier.fillMaxSize(),
        state = scrollState,
        contentPadding = contentPadding,
        orientation = FlowOrientation.Row,
        horizontalGap = 8.dp,
        verticalGap = 8.dp
    ) {
        items(
            list,
            key = { it },
            size = {
                val dpSize = when (it) {
                    is TimeMachine.Annotation -> size2
                    is TimeMachine.Collection -> size1
                    is TimeMachine.FileNode -> size1
                    is TimeMachine.Row -> size1
                    is TimeMachine.Tag -> size2
                }
                FlowItemSize.Exact(dpSize)
            }) {
            val modifier = Modifier.size(it.dpSize())
            TimeMachineItem(
                modifier = modifier,
                item = it,
                workspaceFullPath = workspaceFullPath,
                onClick = {
                    onClick(it)
                }
            )
        }
    }
}