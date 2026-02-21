import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.domain.func.getPlatform
import com.moly3.cedarjam.core.domain.model.Platform
import com.moly3.cedarjam.core.domain.model.SyncStatus
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalHazeState
import com.moly3.cedarjam.core.ui.compositions.LocalHazeStyle
import com.moly3.cedarjam.core.ui.onPointerEvent
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.vectors.ChevronDownDuo
import com.moly3.cedarjam.core.ui.vectors.FileAdd
import com.moly3.cedarjam.core.ui.vectors.FolderAdd
import com.moly3.cedarjam.core.ui.volumedBorderStroke
import dev.chrisbanes.haze.hazeEffect

private val LINE_WIDTH = 1.dp
private val RECT_RADIUS = 40f


@Composable
fun FileButton(
    modifier: Modifier,
    fileExtension: String?,
    isDirectory: Boolean,
    title: String,
    syncStatus: SyncStatus?,
    backColor: Color = LocalAppTheme.current.colors.backgroundSecondary,
    onCreateDirectoryClick: (() -> Unit)?,
    onCreateFileClick: (() -> Unit)?,
    isDragTarget: Boolean,
    isSelected: Boolean,
    isOpen: Boolean?,
    isRename: Boolean,
    isContextMenuTarget: Boolean,
    counter: Int?,
    onRename: (String) -> Unit = {},
    onClick: () -> Unit
) {
    val syncBorderColor = remember(syncStatus){
        when(syncStatus){
            SyncStatus.SYNCED -> Color.Transparent
            SyncStatus.DIRTY -> Color.Magenta
            SyncStatus.NEW -> Color.Green
            SyncStatus.DELETED -> Color.Red
            null -> Color.Transparent
        }
    }
//    if (syncStatus != null) {
//        CJText(
//            modifier = Modifier.padding(end = 8.dp),
//            text = syncStatus.toString(),
//            fontSize = 14.sp,
//            color = LocalAppTheme.current.colors.primaryFont,
//            maxLines = 1,
//            overflow = TextOverflow.Ellipsis
//        )
//    }
    val primaryColor = LocalAppTheme.current.primaryColor
    var isHovered by remember { mutableStateOf(false) }

    val borderColor = remember(isRename, isSelected, isDragTarget, primaryColor) {
        if (isDragTarget)
            primaryColor
        else if (isRename)
            primaryColor
        else if (isSelected)
            primaryColor
        else
            null
    }
    val borderSize = 4
    val strokeWidth = 4f
    val dotSpacing = 10f // distance between dots

    val density = LocalDensity.current
    val pathEffect = with(density) {
        val dashOnInterval1 = (LINE_WIDTH * 4).toPx()
        val dashOffInterval1 = (LINE_WIDTH * 2).toPx()
        val dashOnInterval2 = (LINE_WIDTH / 4).toPx()
        val dashOffInterval2 = (LINE_WIDTH * 2).toPx()


        PathEffect.dashPathEffect(
            floatArrayOf(dashOnInterval1, dashOffInterval1, dashOnInterval2, dashOffInterval2),
            1f
        )
    }
    //PathEffect.dashPathEffect(
    //                                    floatArrayOf(strokeWidth, dotSpacing), 0f
    //                                )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .hazeEffect(state = LocalHazeState.current, LocalHazeStyle.current)
            .let {
                if (isContextMenuTarget) {
                    it.drawBehind {
                        drawRoundRect(
                            color = primaryColor,
                            size = Size(size.width, size.height),
                            style = Stroke(
                                width = 1.dp.toPx(),
                                cap = StrokeCap.Round,
                                pathEffect = pathEffect
                            ),
                        )
                    }
                } else if (borderColor != null) {
                    it.border(
                        1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(borderSize.dp)
                    )
                } else {
                    it.border(
                        border = volumedBorderStroke,
                        shape = RoundedCornerShape(borderSize.dp)
                    )
                }
            }
            .onPointerEvent(PointerEventType.Enter) {
                isHovered = true
            }
            .onPointerEvent(PointerEventType.Exit) {
                isHovered = false
            }
//          todo  .clickable {
//                onClick()
//            }
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val animatedRotation by animateFloatAsState(if (isOpen == true) 0f else -90f)
        FileButtonIcon(
            modifier = Modifier,
            backColor = backColor,
            borderColor = syncBorderColor,
            iconModifier = Modifier.rotate(animatedRotation),
            imageVector = if (isOpen != null) ChevronDownDuo else null
        )

        if (isRename) {
            val renameTextField = remember(title) {
                mutableStateOf(TextFieldValue(title, selection = TextRange(title.length)))
            }
            val focusRequester = remember { FocusRequester() }
            BasicTextField(
                modifier = Modifier.weight(1f).focusRequester(focusRequester),
                value = renameTextField.value,
                onValueChange = {
                    renameTextField.value = it
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    onRename(renameTextField.value.text)
                }),
                textStyle = TextStyle.Default.copy(
                    LocalAppTheme.current.colors.primaryFont,
                    fontSize = 14.sp
                ),
                singleLine = true,
                cursorBrush = SolidColor(primaryColor)
            )
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        } else {

            CJText(
                modifier = Modifier.weight(1f),
                text = title,
                fontSize = 14.sp,
                color = LocalAppTheme.current.colors.primaryFont,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (fileExtension != null) {
            CJText(
                modifier = Modifier,
                text = fileExtension,
                fontSize = 10.sp,
                color = LocalAppTheme.current.colors.primaryFont,
                maxLines = 1
            )
        }
        if (counter != null) {
            CJText(
                modifier = Modifier.background(LocalAppTheme.current.colors.backgroundSecondary)
                    .padding(4.dp),
                text = counter.toString(),
                fontSize = 10.sp,
                maxLines = 1
            )
        }

        if ((isHovered || getPlatform() !is Platform.Jvm) && isDirectory) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (onCreateDirectoryClick != null) {
                    FileButtonIcon(
                        modifier = Modifier.clickable {
                            onCreateDirectoryClick()
                        },
                        iconModifier = Modifier,
                        imageVector = if (isDirectory) FolderAdd else null
                    )
                }
                if (onCreateFileClick != null) {
                    FileButtonIcon(
                        modifier = Modifier.clickable {
                            onCreateFileClick()
                        },
                        iconModifier = Modifier,
                        imageVector = if (isDirectory) FileAdd else null
                    )
                }
            }
        }
    }
}

@Composable
private fun FileButtonIcon(
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    borderColor: Color = Color.Transparent,
    backColor: Color = LocalAppTheme.current.colors.backgroundSecondary,
    imageVector: ImageVector?
) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier = modifier
            .size(20.dp)
            .background(
                backColor,
                shape = shape
            )
            .border(1.dp, borderColor, shape),
        contentAlignment = Alignment.Center
    ) {
        if (imageVector != null) {
            Image(
                imageVector = imageVector,
                contentDescription = null,
                modifier = iconModifier.size(16.dp),
                colorFilter = ColorFilter.tint(LocalAppTheme.current.colors.icon)
            )
        }
    }
}

@Preview
@Composable
fun FileButtonPreview() {
    FileButton(
        modifier = Modifier,
        title = "Flash cards",
        isDirectory = true,
        onClick = {},
        fileExtension = null,
        onCreateDirectoryClick = null,
        onCreateFileClick = null,
        isDragTarget = false,
        isSelected = false,
        isOpen = null,
        isRename = false,
        counter = null,
        onRename = {},
        isContextMenuTarget = false,
        syncStatus = null
    )
}