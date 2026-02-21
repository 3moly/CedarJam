package com.moly3.cedarjam.pages.page_collection_row.ui.internal

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.moly3.cedarjam.pages.page_collection_row.Intent
import com.moly3.cedarjam.pages.page_collection_row.State
import com.moly3.cedarjam.core.ui.func.getPdfImage
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJTextField
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.FileMenuContent
import com.moly3.cedarjam.core.ui.uikit.magmaTextFieldMinTextSize
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import kotlinx.io.files.Path

@Composable
internal fun PageContent(state: State, onIntent: (Intent) -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    var isIOPressed by remember { mutableStateOf(false) }

    val appTheme = LocalAppTheme.current
    val hazeState = rememberHazeState(blurEnabled = true)
    val hazeStyle = remember(appTheme) {
        HazeStyle(
            backgroundColor = appTheme.colors.backgroundSecondary.copy(alpha = 0.8f),
            tints = listOf(HazeTint(appTheme.colors.backgroundSecondary.copy(0.5f))),
            blurRadius = 16.dp,
            noiseFactor = HazeDefaults.noiseFactor
        )
    }
    val isEditState = remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().background(Color.DarkGray)) {
        if (state.collectionRow != null) {
            var imgBitmap by remember {
                mutableStateOf<ImageBitmap?>(null)
            }
            LaunchedEffect(state.collectionRow.fileRelativePath, state.workspace) {
                launch(io) {
                    try {
                        imgBitmap = if (state.collectionRow.fileRelativePath != null) {
                            getPdfImage(
                                Path(
                                    "state.workspace?.fullpath",
                                    state.collectionRow.fileRelativePath!!
                                ).toString(),
                                page = 0,
                                dpi = 100f
                            )
                        } else {
                            null
                        }
                    } catch (exc: Exception) {
                    }
                }
            }
            val scrollState = rememberScrollState()
            val textNameState = remember {
                TextFieldState(state.collectionRow.name)
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    val startSize = 14f
                    ConstraintLayout(modifier = Modifier.align(Alignment.TopCenter)) {
                        val (preText, text) = createRefs()
                        Row(modifier = Modifier.constrainAs(preText) {
                            end.linkTo(text.start, margin = 4.dp)
                        }, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            CJText("collections", color = Color.Gray, fontSize = startSize.sp)
                            CJText("/", color = Color.Gray, fontSize = startSize.sp)
                            CJText(
                                "${state.collection?.name}",
                                color = Color.Gray,
                                fontSize = startSize.sp,
                                modifier = Modifier.clickable {
                                    onIntent(Intent.OpenCollection)
                                }
                            )
                            CJText("/", color = Color.Gray, fontSize = startSize.sp)
                        }
                        val isBigText =
                            remember(
                                scrollState.canScrollBackward,
                                textNameState.text.length,
                                state.collectionRow.fileRelativePath
                            ) {
                                if (scrollState.canScrollBackward || !state.collectionRow.fileRelativePath.isNullOrEmpty())
                                    false
                                else
                                    textNameState.text.length < 6
                            }

                        val animatedFontSize by animateFloatAsState(if (isBigText) 110f else magmaTextFieldMinTextSize)
                        val animatedWidthSize by animateDpAsState(if (isBigText) 100.dp else 10.dp)

                        CJTextField(
                            modifier = Modifier
                                .width(IntrinsicSize.Min)
                                .widthIn(min = animatedWidthSize)
                                .constrainAs(text) {
                                    top.linkTo(parent.top)
                                },
                            textStyle = TextStyle.Default.copy(
                                textAlign = TextAlign.Center,
                                fontSize = animatedFontSize.sp
                            ),
                            text = textNameState,
                            onDone = {
                                onIntent(Intent.Rename(textNameState.text.toString()))
                            }
                        )
                    }
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    if (imgBitmap != null) {
                        Image(
                            bitmap = imgBitmap!!,
                            contentDescription = null,
                            modifier = Modifier
                                .height(200.dp)
                                .align(Alignment.CenterHorizontally),
                            contentScale = ContentScale.FillHeight
                        )
                    } else {

                    }

                    CJButton(
                        text = "Import pdf",
                        onClick = {
                            onIntent(Intent.ImportPdf)
                        })
                    if (!isEditState.value) {
                        CJButton(
                            text = "Edit",
                            onClick = {
                                isEditState.value = !isEditState.value
                            })
                    }

                    if (isEditState.value) {
                        var webLinkTextState by remember(state.collectionRow) {
                            mutableStateOf(
                                TextFieldValue(
                                    state.collectionRow.webLink ?: ""
                                )
                            )
                        }
                        Column {
                            CJText(text = "web link:")
                            CJTextField(
                                modifier = Modifier.width(500.dp),
                                value = webLinkTextState,
                                onValueChanged = {
                                    webLinkTextState = it
                                }
                            )
                        }

                        Row {
                            CJButton(
                                text = "Update",
                                onClick = {
                                    onIntent(
                                        Intent.Update(
                                            collRow = state.collectionRow.copy(
                                                webLink = webLinkTextState.text
                                            )
                                        )
                                    )
                                    isEditState.value = false
                                })
                            CJButton(
                                text = "Cancel",
                                onClick = {
                                    isEditState.value = false
                                })
                        }
                    }
                }
            }
        }
    }
    FileMenuContent(
        modifier = Modifier.safeDrawingPadding().fillMaxSize(),
        borderModifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .hazeEffect(hazeState, hazeStyle)
        ,
        annotationsCount = 0,
        isIOSwitchPressed = isIOPressed,
        isOpenedMenu = isPressed,
        openWorkspaceSettings = {
            onIntent(Intent.OpenWorkspaceSettings)
        },
        onIOClick = {
            isIOPressed = !isIOPressed
        },
        onClick = {
            isPressed = !isPressed
        }
    )
}