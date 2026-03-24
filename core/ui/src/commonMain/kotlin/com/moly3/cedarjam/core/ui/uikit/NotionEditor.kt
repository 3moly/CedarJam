package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import vector.collection.Note
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// ─────────────────────────────────────────────────────────────────────────────
// Model
// ─────────────────────────────────────────────────────────────────────────────

sealed interface BlockType {
    data object Paragraph : BlockType
    data object H1 : BlockType
    data object H2 : BlockType
    data object H3 : BlockType
    data object Image : BlockType
    data object Custom : BlockType
}

data class NotionBlock @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String = Uuid.random().toString(),
    val type: BlockType = BlockType.Paragraph,
    val text: String = "",
    val image: ImageBitmap? = null,
    val customContent: (@Composable () -> Unit)? = null,
)

// ─────────────────────────────────────────────────────────────────────────────
// Slash command entries
// ─────────────────────────────────────────────────────────────────────────────

private data class SlashCommand(
    val label: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val type: BlockType,
)

private val slashCommands = listOf(
    SlashCommand("Text", "Plain paragraph", Note, BlockType.Paragraph),
    SlashCommand("Heading 1", "Large section title", Note, BlockType.H1),
    SlashCommand("Heading 2", "Medium section title", Note, BlockType.H2),
    SlashCommand("Heading 3", "Small section title", Note, BlockType.H3),
    SlashCommand("Image", "Embed an image", Note, BlockType.Image),
)

// ─────────────────────────────────────────────────────────────────────────────
// State
// ─────────────────────────────────────────────────────────────────────────────

@Stable
class NotionEditorState(initialBlocks: List<NotionBlock> = listOf(NotionBlock())) {

    var blocks by mutableStateOf(initialBlocks)
        private set

    var selectedIds by mutableStateOf(emptySet<String>())
        private set

    val hasSelection get() = selectedIds.isNotEmpty()

    // ── Focus registry ───────────────────────────────────────────────────────

    private val focusRequesters = mutableMapOf<String, FocusRequester>()

    fun registerFocusRequester(id: String, requester: FocusRequester) {
        focusRequesters[id] = requester
    }

    fun unregisterFocusRequester(id: String) {
        focusRequesters.remove(id)
    }

    fun focusBlock(id: String) {
        focusRequesters[id]?.requestFocus()
    }

    fun focusNextBlock(currentId: String) {
        val idx = blocks.indexOfFirst { it.id == currentId }
        val nextId = blocks.getOrNull(idx + 1)?.id ?: return
        focusRequesters[nextId]?.requestFocus()
    }

    fun focusPreviousBlock(currentId: String) {
        val idx = blocks.indexOfFirst { it.id == currentId }
        val prevId = blocks.getOrNull(idx - 1)?.id ?: return
        focusRequesters[prevId]?.requestFocus()
    }

    // ── Selection ────────────────────────────────────────────────────────────

    fun toggleSelection(id: String) {
        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
    }

    fun selectRange(fromId: String, toId: String) {
        val ids = blocks.map { it.id }
        val a = ids.indexOf(fromId)
        val b = ids.indexOf(toId)
        if (a < 0 || b < 0) return
        val lo = minOf(a, b)
        val hi = maxOf(a, b)
        selectedIds = ids.subList(lo, hi + 1).toSet()
    }

    fun clearSelection() {
        selectedIds = emptySet()
    }

    // ── Block editing ────────────────────────────────────────────────────────

    fun updateText(id: String, text: String) {
        blocks = blocks.map { if (it.id == id) it.copy(text = text) else it }
        ensureTrailingEmptyBlock()
    }

    fun changeType(id: String, type: BlockType) {
        blocks = blocks.map { if (it.id == id) it.copy(type = type) else it }
        ensureTrailingEmptyBlock()
    }

    fun changeTypeForSelected(type: BlockType) {
        blocks = blocks.map { if (it.id in selectedIds) it.copy(type = type) else it }
        clearSelection()
        ensureTrailingEmptyBlock()
    }

    @OptIn(ExperimentalUuidApi::class)
    fun addBlockAfter(id: String, block: NotionBlock = NotionBlock()): String {
        val idx = blocks.indexOfFirst { it.id == id }
        if (idx < 0) return block.id
        blocks = blocks.toMutableList().also { it.add(idx + 1, block) }
        ensureTrailingEmptyBlock()
        return block.id
    }

    fun removeBlock(id: String) {
        if (blocks.size <= 1) return
        blocks = blocks.filter { it.id != id }
        ensureTrailingEmptyBlock()
    }

    fun deleteSelected() {
        blocks = blocks.filter { it.id !in selectedIds }
        clearSelection()
        if (blocks.isEmpty()) blocks = listOf(NotionBlock())
        ensureTrailingEmptyBlock()
    }

    @OptIn(ExperimentalUuidApi::class)
    fun copySelected(): List<NotionBlock> =
        blocks.filter { it.id in selectedIds }.map { it.copy(id = Uuid.random().toString()) }

    fun pasteAfterSelection(copies: List<NotionBlock>) {
        val lastSelected = blocks.indexOfLast { it.id in selectedIds }
        if (lastSelected < 0) {
            blocks = blocks + copies; return
        }
        blocks = blocks.toMutableList().also { it.addAll(lastSelected + 1, copies) }
        ensureTrailingEmptyBlock()
    }

    fun moveBlock(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        val list = blocks.toMutableList()
        val item = list.removeAt(fromIndex)
        list.add(toIndex.coerceIn(0, list.size), item)
        blocks = list
        ensureTrailingEmptyBlock()
    }

    // ── Always keep a blank paragraph at the end ─────────────────────────────

    @OptIn(ExperimentalUuidApi::class)
    private fun ensureTrailingEmptyBlock() {
        val last = blocks.lastOrNull()
        if (last == null || last.text.isNotEmpty() || last.type != BlockType.Paragraph) {
            blocks = blocks + NotionBlock()
        }
    }
}

@Composable
fun rememberNotionEditorState(initial: List<NotionBlock> = listOf(NotionBlock())) =
    remember { NotionEditorState(initial) }

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun blockAtOffset(rects: Map<String, Rect>, offset: Offset): String? =
    rects.entries.firstOrNull { (_, rect) -> rect.contains(offset) }?.key

private fun blockAtPoint(rects: Map<String, Rect>, offset: Offset): String? {
    if (rects.isEmpty()) return null
    val sorted = rects.entries.sortedBy { it.value.top }
    val strict = sorted.firstOrNull { (_, rect) ->
        offset.y >= rect.top && offset.y < rect.bottom &&
                offset.x >= rect.left && offset.x <= rect.right
    }
    if (strict != null) return strict.key
    return sorted.minByOrNull { (_, rect) ->
        kotlin.math.abs((rect.top + rect.bottom) / 2f - offset.y)
    }?.key
}

private fun PointerInputChange.isInBounds(size: IntSize): Boolean {
    val pos = position
    return pos.x >= 0f && pos.y >= 0f && pos.x <= size.width && pos.y <= size.height
}

// ─────────────────────────────────────────────────────────────────────────────
// Editor root
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NotionEditor(
    state: NotionEditorState,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    selectionColor: Color = Color(0xFF2383E2).copy(alpha = 0.18f),
    selectionBorderColor: Color = Color(0xFF2383E2),
) {
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current
    var clipboard by remember { mutableStateOf<List<NotionBlock>>(emptyList()) }

    val blockRects = remember { mutableStateMapOf<String, Rect>() }

    var dragAnchorId by remember { mutableStateOf<String?>(null) }
    var dragCurrentId by remember { mutableStateOf<String?>(null) }
    var isDragging by remember { mutableStateOf(false) }

    Box(modifier = modifier.background(backgroundColor)) {

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val hitId = blockAtPoint(blockRects, down.position)
                            ?: return@awaitEachGesture

                        var dragStarted = false

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            if (!change.pressed) break

                            val delta = change.position - down.position
                            if (!dragStarted &&
                                (kotlin.math.abs(delta.x) > viewConfiguration.touchSlop ||
                                        kotlin.math.abs(delta.y) > viewConfiguration.touchSlop)
                            ) {
                                dragStarted = true
                                isDragging = true
                                dragAnchorId = hitId
                                dragCurrentId = hitId
                                state.clearSelection()
                                state.selectRange(hitId, hitId)
                            }

                            if (dragStarted) {
                                change.consume()
                                val hovered = blockAtPoint(blockRects, change.position)
                                if (hovered != null && hovered != dragCurrentId) {
                                    dragCurrentId = hovered
                                    state.selectRange(dragAnchorId!!, hovered)
                                }
                            }
                        }

                        if (dragStarted) {
                            isDragging = false
                            dragAnchorId = null
                            dragCurrentId = null
                        }
                    }
                },
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(state.blocks, key = { it.id }) { block ->
                NotionBlockRow(
                    block = block,
                    state = state,
                    isSelected = block.id in state.selectedIds,
                    isDragAnchor = block.id == dragAnchorId,
                    selectionColor = selectionColor,
                    selectionBorderColor = selectionBorderColor,
                    onTextChange = { state.updateText(block.id, it) },
                    onEnterPressed = { state.addBlockAfter(block.id) },
                    onBlockClick = { if (state.hasSelection) state.toggleSelection(block.id) },
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        state.toggleSelection(block.id)
                    },
                    onDragHandleDrag = { from, to -> state.moveBlock(from, to) },
                    blockIndex = state.blocks.indexOf(block),
                    totalBlocks = state.blocks.size,
                    onRectChanged = { rect -> blockRects[block.id] = rect }
                )
            }
        }

        if (state.hasSelection) {
            SelectionToolbar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .zIndex(10f),
                onDelete = { state.deleteSelected() },
                onCopy = { clipboard = state.copySelected(); state.clearSelection() },
                onPaste = { if (clipboard.isNotEmpty()) state.pasteAfterSelection(clipboard) },
                onChangeType = { type -> state.changeTypeForSelected(type) },
                onClearSelection = { state.clearSelection() }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Single block row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NotionBlockRow(
    block: NotionBlock,
    state: NotionEditorState,
    isSelected: Boolean,
    isDragAnchor: Boolean,
    selectionColor: Color,
    selectionBorderColor: Color,
    onTextChange: (String) -> Unit,
    onEnterPressed: () -> Unit,
    onBlockClick: () -> Unit,
    onLongPress: () -> Unit,
    onDragHandleDrag: (from: Int, to: Int) -> Unit,
    blockIndex: Int,
    totalBlocks: Int,
    onRectChanged: (Rect) -> Unit,
) {
    var isHovered by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { onRectChanged(it.boundsInRoot()) }
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .background(
                when {
                    isDragAnchor -> selectionBorderColor.copy(alpha = 0.12f)
                    isSelected -> selectionColor
                    else -> Color.Transparent
                },
                RoundedCornerShape(6.dp)
            )
            .border(
                width = when {
                    isDragAnchor -> 2.dp
                    isSelected -> 1.5.dp
                    else -> 0.dp
                },
                color = when {
                    isDragAnchor -> selectionBorderColor
                    isSelected -> selectionBorderColor
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(6.dp)
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        isHovered = event.changes.any { it.isInBounds(size) }
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onBlockClick() },
                    onLongPress = { onLongPress() }
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        DragHandle(
            blockIndex = blockIndex,
            totalBlocks = totalBlocks,
            block = block,
            state = state,
            onDrag = onDragHandleDrag,
            visible = isHovered,
        )

        Spacer(Modifier.width(4.dp))

        Box(modifier = Modifier.weight(1f).padding(vertical = 4.dp)) {
            when (block.type) {
                BlockType.Paragraph -> EditableText(
                    block = block, state = state,
                    onTextChange = onTextChange, onEnterPressed = onEnterPressed,
                    style = TextStyle(fontSize = 15.sp, color = Color(0xFF1A1A1A))
                )

                BlockType.H1 -> EditableText(
                    block = block, state = state,
                    onTextChange = onTextChange, onEnterPressed = onEnterPressed,
                    style = TextStyle(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                )

                BlockType.H2 -> EditableText(
                    block = block, state = state,
                    onTextChange = onTextChange, onEnterPressed = onEnterPressed,
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                )

                BlockType.H3 -> EditableText(
                    block = block, state = state,
                    onTextChange = onTextChange, onEnterPressed = onEnterPressed,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    )
                )

                BlockType.Image -> ImageBlock(block.image)
                BlockType.Custom -> block.customContent?.invoke()
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Editable text
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EditableText(
    block: NotionBlock,
    state: NotionEditorState,
    onTextChange: (String) -> Unit,
    onEnterPressed: () -> Unit,
    style: TextStyle,
) {
    val focusRequester = remember { FocusRequester() }
    var pendingFocusNext by remember { mutableStateOf(false) }
    var showSlashMenu by remember { mutableStateOf(false) }
    var slashQuery by remember { mutableStateOf("") }
    var selectedCommandIndex by remember { mutableStateOf(0) }
    var isFocused by remember { mutableStateOf(false) }

    val filteredCommands = remember(slashQuery) {
        if (slashQuery.isEmpty()) slashCommands
        else slashCommands.filter { it.label.contains(slashQuery, ignoreCase = true) }
    }

    DisposableEffect(block.id) {
        state.registerFocusRequester(block.id, focusRequester)
        onDispose { state.unregisterFocusRequester(block.id) }
    }

    LaunchedEffect(state.blocks.size) {
        if (pendingFocusNext) {
            pendingFocusNext = false
            kotlinx.coroutines.delay(30)
            state.focusNextBlock(block.id)
        }
    }

    Box {
        BasicTextField(
            value = block.text,
            onValueChange = { newValue ->
                val prev = block.text
                val typed = newValue.lastOrNull()

                if (typed == '/' && (prev.isEmpty() || prev.lastOrNull() == ' ')) {
                    showSlashMenu = true
                    slashQuery = ""
                    selectedCommandIndex = 0
                    onTextChange(newValue)
                } else if (showSlashMenu) {
                    val slashIndex = newValue.lastIndexOf('/')
                    if (slashIndex < 0) {
                        showSlashMenu = false
                    } else {
                        slashQuery = newValue.substring(slashIndex + 1)
                        selectedCommandIndex = 0
                    }
                    onTextChange(newValue)
                } else {
                    onTextChange(newValue)
                }
            },
            textStyle = style,
            keyboardActions = KeyboardActions(onDone = {
                onEnterPressed()
                pendingFocusNext = true
            }),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused }
                .focusRequester(focusRequester)
                .onKeyEvent { event ->
                    val isBackspace =
                        event.type == KeyEventType.Unknown && event.utf16CodePoint == 8

                    if (event.type != KeyEventType.KeyDown && !isBackspace) return@onKeyEvent false

                    if (event.type == KeyEventType.KeyDown && event.key == Key.Backspace) {
                        return@onKeyEvent block.text.isEmpty()
                    }

                    if (showSlashMenu && filteredCommands.isNotEmpty()) {
                        when (event.key) {
                            Key.DirectionDown -> {
                                selectedCommandIndex =
                                    (selectedCommandIndex + 1) % filteredCommands.size; return@onKeyEvent true
                            }

                            Key.DirectionUp -> {
                                selectedCommandIndex =
                                    (selectedCommandIndex - 1 + filteredCommands.size) % filteredCommands.size; return@onKeyEvent true
                            }

                            Key.Enter -> {
                                applySlashCommand(
                                    filteredCommands[selectedCommandIndex],
                                    block,
                                    slashQuery,
                                    state,
                                    onTextChange
                                ) { showSlashMenu = false }
                                return@onKeyEvent true
                            }

                            Key.Escape -> {
                                showSlashMenu = false; return@onKeyEvent true
                            }

                            else -> {}
                        }
                    }

                    when {
                        isBackspace && block.text.isEmpty() -> {
                            showSlashMenu = false
                            state.focusPreviousBlock(block.id)
                            state.removeBlock(block.id)
                            true
                        }

                        event.key == Key.Enter && event.isShiftPressed -> {
                            if (showSlashMenu) showSlashMenu = false
                            onTextChange(block.text + "\n")
                            true
                        }

                        event.key == Key.Enter && !event.isShiftPressed -> {
                            if (showSlashMenu) showSlashMenu = false
                            else {
                                onEnterPressed(); pendingFocusNext = true
                            }
                            true
                        }

                        event.key == Key.DirectionUp && !showSlashMenu -> {
                            state.focusPreviousBlock(block.id); true
                        }

                        event.key == Key.DirectionDown && !showSlashMenu -> {
                            state.focusNextBlock(block.id); true
                        }

                        else -> false
                    }
                },
            decorationBox = { inner ->
                if (block.text.isEmpty() && isFocused) {
                    Text("Type something…", style = style.copy(color = Color(0xFFAAAAAA)))
                }
                inner()
            }
        )

        if (showSlashMenu) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(0, style.fontSize.value.toInt() + 24),
                properties = PopupProperties(focusable = false),
                onDismissRequest = { showSlashMenu = false }
            ) {
                SlashCommandMenu(
                    commands = filteredCommands,
                    selectedIndex = selectedCommandIndex,
                    onSelect = { command ->
                        applySlashCommand(
                            command,
                            block,
                            slashQuery,
                            state,
                            onTextChange
                        ) { showSlashMenu = false }
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Slash command menu UI
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SlashCommandMenu(
    commands: List<SlashCommand>,
    selectedIndex: Int,
    onSelect: (SlashCommand) -> Unit,
) {
    if (commands.isEmpty()) return

    Surface(
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 12.dp,
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E5E5)),
        modifier = Modifier.width(240.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            Text(
                text = "BLOCKS",
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF999999),
                    letterSpacing = 0.8.sp
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
            commands.forEachIndexed { index, command ->
                val isHighlighted = index == selectedIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isHighlighted) Color(0xFFF0F0F0) else Color.Transparent)
                        .clickable { onSelect(command) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier.size(32.dp)
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = command.icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFF444444)
                        )
                    }
                    Column {
                        Text(
                            command.label,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1A1A1A)
                            )
                        )
                        Text(
                            command.description,
                            style = TextStyle(fontSize = 12.sp, color = Color(0xFF999999))
                        )
                    }
                }
            }
        }
    }
}

private fun applySlashCommand(
    command: SlashCommand,
    block: NotionBlock,
    slashQuery: String,
    state: NotionEditorState,
    onTextChange: (String) -> Unit,
    onCloseMenu: () -> Unit,
) {
    val slashIndex = block.text.lastIndexOf('/')
    val cleanText = if (slashIndex >= 0) block.text.substring(0, slashIndex) else block.text
    onTextChange(cleanText)
    state.changeType(block.id, command.type)
    onCloseMenu()
}

// ─────────────────────────────────────────────────────────────────────────────
// Image block
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ImageBlock(image: ImageBitmap?) {
    if (image != null) {
        Image(
            bitmap = image,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
            contentScale = ContentScale.Fit
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("No image", color = Color(0xFFAAAAAA))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Drag handle
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DragHandle(
    blockIndex: Int,
    totalBlocks: Int,
    block: NotionBlock,
    state: NotionEditorState,
    onDrag: (from: Int, to: Int) -> Unit,
    visible: Boolean,
) {
    var isDragging by remember { mutableStateOf(false) }
    var accumulatedDelta by remember { mutableStateOf(0f) }
    var showMenu by remember { mutableStateOf(false) }
    val rowHeightPx = with(LocalDensity.current) { 48.dp.toPx() }

    Box {
        Icon(
            imageVector = Note,
            contentDescription = "Block options",
            tint = if (isDragging) Color(0xFF2383E2) else Color(0xFF999999),
            modifier = Modifier
                .size(20.dp)
                .alpha(if (visible || isDragging) 1f else 0f)
                .pointerInput(blockIndex) {
                    detectTapGestures(onTap = { showMenu = true })
                }
                .pointerInput(blockIndex) {
                    detectDragGestures(
                        onDragStart = { isDragging = true; accumulatedDelta = 0f },
                        onDragEnd = { isDragging = false; accumulatedDelta = 0f },
                        onDragCancel = { isDragging = false; accumulatedDelta = 0f },
                        onDrag = { _, dragAmount ->
                            accumulatedDelta += dragAmount.y
                            val steps = (accumulatedDelta / rowHeightPx).toInt()
                            if (steps != 0) {
                                val targetIndex = (blockIndex + steps).coerceIn(0, totalBlocks - 1)
                                onDrag(blockIndex, targetIndex)
                                accumulatedDelta -= steps * rowHeightPx
                            }
                        }
                    )
                }
        )

        if (showMenu) {
            BlockOptionsMenu(
                block = block,
                state = state,
                onDismiss = { showMenu = false }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Block options menu (drag handle click)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun BlockOptionsMenu(
    block: NotionBlock,
    state: NotionEditorState,
    onDismiss: () -> Unit,
) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
        modifier = Modifier.width(220.dp)
    ) {
        // ── Turn into ────────────────────────────────────────────────────
        Text(
            text = "TURN INTO",
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF999999),
                letterSpacing = 0.8.sp
            ),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )

        listOf(
            Triple(Note, "Text", BlockType.Paragraph),
            Triple(Note, "Heading 1", BlockType.H1),
            Triple(Note, "Heading 2", BlockType.H2),
            Triple(Note, "Heading 3", BlockType.H3),
            Triple(Note, "Image", BlockType.Image),
        ).forEach { (icon, label, type) ->
            val isCurrent = block.type == type
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        icon, contentDescription = null, modifier = Modifier.size(16.dp),
                        tint = if (isCurrent) Color(0xFF2383E2) else Color(0xFF555555)
                    )
                },
                text = {
                    Text(
                        label, style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isCurrent) Color(0xFF2383E2) else Color(0xFF1A1A1A)
                        )
                    )
                },
                trailingIcon = {
                    if (isCurrent) Icon(
                        Note, contentDescription = null,
                        modifier = Modifier.size(14.dp), tint = Color(0xFF2383E2)
                    )
                },
                onClick = { state.changeType(block.id, type); onDismiss() }
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // ── Actions ──────────────────────────────────────────────────────
        Text(
            text = "ACTIONS",
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF999999),
                letterSpacing = 0.8.sp
            ),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )

        // Add block below
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    Note, contentDescription = null,
                    modifier = Modifier.size(16.dp), tint = Color(0xFF555555)
                )
            },
            text = {
                Text(
                    "Add block below",
                    style = TextStyle(fontSize = 14.sp, color = Color(0xFF1A1A1A))
                )
            },
            onClick = {
                state.addBlockAfter(block.id)
                state.focusNextBlock(block.id)
                onDismiss()
            }
        )

        // Duplicate
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    Note, contentDescription = null,
                    modifier = Modifier.size(16.dp), tint = Color(0xFF555555)
                )
            },
            text = {
                Text(
                    "Duplicate",
                    style = TextStyle(fontSize = 14.sp, color = Color(0xFF1A1A1A))
                )
            },
            onClick = {
                val copy = block.copy(id = Uuid.random().toString())
                state.addBlockAfter(block.id, copy)
                onDismiss()
            }
        )

        // Delete
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    Note, contentDescription = null,
                    modifier = Modifier.size(16.dp), tint = Color(0xFFE53935)
                )
            },
            text = {
                Text(
                    "Delete",
                    style = TextStyle(fontSize = 14.sp, color = Color(0xFFE53935))
                )
            },
            onClick = {
                state.focusPreviousBlock(block.id)
                state.removeBlock(block.id)
                onDismiss()
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Selection toolbar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SelectionToolbar(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onChangeType: (BlockType) -> Unit,
    onClearSelection: () -> Unit,
) {
    var showTypeMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 8.dp,
        color = Color(0xFF2C2C2C)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            ToolbarButton(Note, "Clear", onClearSelection)
            ToolbarDivider()
            ToolbarButton(Note, "Delete", onDelete)
            ToolbarButton(Note, "Copy", onCopy)
            ToolbarButton(Note, "Paste", onPaste)
            ToolbarDivider()
            Box {
                ToolbarButton(Note, "Type") { showTypeMenu = true }
                DropdownMenu(expanded = showTypeMenu, onDismissRequest = { showTypeMenu = false }) {
                    listOf(
                        "Paragraph" to BlockType.Paragraph,
                        "Heading 1" to BlockType.H1,
                        "Heading 2" to BlockType.H2,
                        "Heading 3" to BlockType.H3,
                    ).forEach { (label, type) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = { onChangeType(type); showTypeMenu = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolbarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
        Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun ToolbarDivider() {
    Box(Modifier.width(1.dp).height(20.dp).background(Color(0xFF555555)))
}

//class NotionMarkdownSerializer {
//    fun decode(blocks: List<NotionBlock>): String {
//
//    }
//
//    fun encode(text: String): List<NotionBlock> {
//
//    }
//}