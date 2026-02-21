package com.moly3.cedarjam.pages.page_home.ui.internal

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.FileTypeExt
import com.moly3.cedarjam.core.domain.model.toFileType
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.func.navigationBarsPaddingCJ
import com.moly3.cedarjam.core.ui.func.rememberPdfImage
import com.moly3.cedarjam.core.ui.func.wstatusBarsPaddingCJ
import com.moly3.cedarjam.core.ui.uikit.CJSearchTextField
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.JustMenuContent
import com.moly3.cedarjam.core.ui.uikit.NeumorphicShadowConfig
import com.moly3.cedarjam.core.ui.uikit.NeumorphicShape
import com.moly3.cedarjam.core.ui.uikit.UIStateContentNoBox
import com.moly3.cedarjam.core.ui.uikit.v7.LazyFlowRowV8
import com.moly3.cedarjam.pages.page_home.Intent
import com.moly3.cedarjam.pages.page_home.State
import com.moly3.cedarjam.pages.page_home.model.TimeMachine
import kotlin.time.ExperimentalTime


fun DpSize.toPx(density: Density): com.moly3.cedarjam.core.ui.uikit.v7.FlowItemSize {
    val dpSize = this
    return com.moly3.cedarjam.core.ui.uikit.v7.FlowItemSize(
        widthPx = with(density) { dpSize.width.roundToPx() },
        heightPx = with(density) { dpSize.height.roundToPx() }
    )
}

fun TimeMachine.dpSize(): DpSize {
    return when (this) {
        is TimeMachine.Collection -> DpSize(150.dp, 150.dp)
        is TimeMachine.FileNode -> DpSize(150.dp, 150.dp)
        is TimeMachine.Row -> DpSize(150.dp, 150.dp)

        is TimeMachine.Tag -> DpSize(250.dp, 50.dp)
    }
}

@OptIn(ExperimentalTime::class)
@Composable
internal fun PageContent(
    workspaceFullPath: String,
    state: State,
    onIntent: (Intent) -> Unit
) {
    UIStateContentNoBox(state = state.timeMachinesState) { timeMachines ->
        Column(modifier = Modifier.fillMaxSize()) {
            CJSearchTextField(
                modifier = Modifier.wstatusBarsPaddingCJ().padding(horizontal = 16.dp).fillMaxWidth(),
                isSearchIcon = true,
                placeholderText = "Search...",
                value = state.searchTextFieldValue,
                onValueChange = {
                    onIntent(Intent.SetSearchText(it))
                }
            )
            val rowSize = remember { mutableStateOf<IntSize>(IntSize.Zero) }
            val density = LocalDensity.current
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyFlowRowV8(
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize(),
                    afterScrollModifier = Modifier.padding(vertical = 16.dp).navigationBarsPaddingCJ()
                        .onGloballyPositioned({
                            rowSize.value = it.size
                        }),
                    items = timeMachines,
                    key = { it -> it },
                    horizontalGap = 8.dp,
                    verticalGap = 8.dp,
                    itemSize = { it.dpSize().toPx(density) }) {

                    val modifier = Modifier.size(it.dpSize())

                    NeumorphicShape(
                        modifier = modifier,
                        buttonShape = RoundedCornerShape(16.dp),
                        strength = 0.01f,
                        pressedColor = Color.Yellow,
                        unpressedColor = Color.Red,
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
                                when (it) {
                                    is TimeMachine.Collection -> {
                                        CJText(text = "Collection")
                                    }

                                    is TimeMachine.FileNode -> {
                                        when (it.file.name.extension.toFileType()) {
                                            FileTypeExt.None -> {}
                                            FileTypeExt.Pdf -> {
                                                val pdfImg = rememberPdfImage(it.file.getFullPath())
                                                if (pdfImg != null) {
                                                    Image(
                                                        bitmap = pdfImg,
                                                        contentDescription = null,
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                            }

                                            FileTypeExt.Image -> {
                                                AsyncImage(
                                                    model = it.file.getFullPath(),
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }

                                            FileTypeExt.Video -> {}
                                            FileTypeExt.Text -> {

                                            }
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
                                            CJText(text = it.file.getFullName())
                                        }

                                    }

                                    is TimeMachine.Row -> {
                                        val isPdf = remember(it.row) {
                                            val relativePath = it.row.fileRelativePath
                                            relativePath?.contains(".pdf") ?: false
                                        }
                                        if (isPdf) {
                                            val pdfImg = rememberPdfImage(
                                                pathWrapper(
                                                    workspaceFullPath,
                                                    it.row.fileRelativePath ?: ""
                                                ).pathString
                                            )
                                            if (pdfImg != null) {
                                                Image(
                                                    bitmap = pdfImg,
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        } else {
                                            CJText(text = "Row ${it.row}")
                                        }
                                    }

                                    is TimeMachine.Tag -> {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                                .fillMaxHeight(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            val hashColor = remember(it.tag.color) {
                                                if (it.tag.color.luminance() > 0.5f) {
                                                    Color.Black
                                                } else {
                                                    Color.White
                                                }
                                            }
                                            Box(
                                                Modifier
                                                    .size(40.dp)
                                                    .background(it.tag.color, shape = CircleShape)
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
                                                    text = "#${it.tag.name}",
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
                        onIntent(Intent.OpenTimeMachine(it))
                    }
                    LaunchedEffect(Unit) {
                        co.touchlab.kermit.Logger.w { "kekeke: ${it}" }
                    }
                }
            }
        }
    }
    JustMenuContent {
        onIntent(Intent.OpenWorkspaceSettings)
    }
}