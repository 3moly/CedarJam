import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.*
import com.moly3.cedarjam.core.domain.func.nowInMs
import com.moly3.cedarjam.core.ui.onPointerEvent
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

// ─────────────────────────────────────────────────────────────
// DATA MODEL
// ─────────────────────────────────────────────────────────────

enum class BlockType {
    PARAGRAPH, HEADING1, HEADING2, HEADING3,
    BULLET_LIST, ORDERED_LIST, CHECKBOX,
    QUOTE, CODE_BLOCK, DIVIDER, IMAGE
}

data class EditorBlock(
    val id: String = Uuid.random().toString(),
    val type: BlockType = BlockType.PARAGRAPH,
    val content: String = "",
    val checked: Boolean = false,       // for CHECKBOX
    val imageUri: String? = null,       // for IMAGE
    val imageName: String? = null,      // for IMAGE display
    val orderIndex: Int = 0             // for ORDERED_LIST
)

data class EditorState(
    val blocks: List<EditorBlock> = listOf(EditorBlock()),
    val selectedBlockIds: Set<String> = emptySet(),
    val focusedBlockId: String? = null
)

// ─────────────────────────────────────────────────────────────
// THEME
// ─────────────────────────────────────────────────────────────

private val BgPrimary     = Color(0xFF1E1E2E)
private val BgSecondary   = Color(0xFF252535)
private val BgSurface     = Color(0xFF2A2A3E)
private val BgSelected    = Color(0xFF3D3D5C)
private val AccentPurple  = Color(0xFF7C6FCD)
private val AccentGreen   = Color(0xFF4CAF6E)
private val TextPrimary   = Color(0xFFE8E8F0)
private val TextSecondary = Color(0xFF9090A8)
private val TextMuted     = Color(0xFF5C5C78)
private val BorderColor   = Color(0xFF3A3A50)
private val SelectionOverlay = Color(0x557C6FCD)

// ─────────────────────────────────────────────────────────────
// EDITOR VIEWMODEL (pure state logic)
// ─────────────────────────────────────────────────────────────

class EditorViewModel {
    var state by mutableStateOf(EditorState(
        blocks = listOf(
            EditorBlock(type = BlockType.HEADING1, content = "My Obsidian Note"),
            EditorBlock(type = BlockType.PARAGRAPH, content = "Start typing your thoughts here. This editor supports multiple content types."),
            EditorBlock(type = BlockType.HEADING2, content = "Tasks"),
            EditorBlock(type = BlockType.CHECKBOX, content = "Buy groceries", checked = true),
            EditorBlock(type = BlockType.CHECKBOX, content = "Write documentation"),
            EditorBlock(type = BlockType.HEADING2, content = "Ideas"),
            EditorBlock(type = BlockType.BULLET_LIST, content = "Compose Multiplatform is great"),
            EditorBlock(type = BlockType.BULLET_LIST, content = "WYSIWYG editors improve productivity"),
            EditorBlock(type = BlockType.QUOTE, content = "The best tools disappear and let you think."),
            EditorBlock(type = BlockType.PARAGRAPH, content = "")
        )
    ))
        private set

    fun updateBlock(id: String, content: String) {
        state = state.copy(
            blocks = state.blocks.map { if (it.id == id) it.copy(content = content) else it }
        )
    }

    fun toggleCheck(id: String) {
        state = state.copy(
            blocks = state.blocks.map { if (it.id == id) it.copy(checked = !it.checked) else it }
        )
    }

    fun changeBlockType(id: String, type: BlockType) {
        state = state.copy(
            blocks = state.blocks.map { if (it.id == id) it.copy(type = type) else it }
        )
    }

    fun addBlockAfter(id: String, type: BlockType = BlockType.PARAGRAPH) {
        val idx = state.blocks.indexOfFirst { it.id == id }
        if (idx < 0) return
        val newBlock = EditorBlock(type = type)
        val newList = state.blocks.toMutableList()
        newList.add(idx + 1, newBlock)
        state = state.copy(blocks = newList, focusedBlockId = newBlock.id)
    }

    fun deleteBlock(id: String) {
        if (state.blocks.size <= 1) return
        val idx = state.blocks.indexOfFirst { it.id == id }
        val newList = state.blocks.toMutableList()
        newList.removeAt(idx)
        val focusIdx = (idx - 1).coerceAtLeast(0)
        state = state.copy(
            blocks = newList,
            focusedBlockId = newList.getOrNull(focusIdx)?.id,
            selectedBlockIds = state.selectedBlockIds - id
        )
    }

    fun deleteSelectedBlocks() {
        if (state.selectedBlockIds.isEmpty()) return
        val remaining = state.blocks.filter { it.id !in state.selectedBlockIds }
        val safeList = if (remaining.isEmpty()) listOf(EditorBlock()) else remaining
        state = state.copy(blocks = safeList, selectedBlockIds = emptySet(), focusedBlockId = safeList.first().id)
    }

    fun selectBlock(id: String, addToSelection: Boolean = false) {
        state = if (addToSelection) {
            val newSel = if (id in state.selectedBlockIds)
                state.selectedBlockIds - id
            else state.selectedBlockIds + id
            state.copy(selectedBlockIds = newSel, focusedBlockId = id)
        } else {
            state.copy(selectedBlockIds = setOf(id), focusedBlockId = id)
        }
    }

    fun selectAll() {
        state = state.copy(selectedBlockIds = state.blocks.map { it.id }.toSet())
    }

    fun clearSelection() {
        state = state.copy(selectedBlockIds = emptySet())
    }

    fun setFocus(id: String) {
        state = state.copy(focusedBlockId = id, selectedBlockIds = emptySet())
    }

    fun addImageBlock(afterId: String, name: String) {
        val idx = state.blocks.indexOfFirst { it.id == afterId }
        val newBlock = EditorBlock(type = BlockType.IMAGE, imageName = name, imageUri = "placeholder")
        val newList = state.blocks.toMutableList()
        newList.add(if (idx < 0) newList.size else idx + 1, newBlock)
        state = state.copy(blocks = newList, focusedBlockId = newBlock.id)
    }

    fun addDivider(afterId: String) {
        val idx = state.blocks.indexOfFirst { it.id == afterId }
        val newBlock = EditorBlock(type = BlockType.DIVIDER)
        val newList = state.blocks.toMutableList()
        newList.add(if (idx < 0) newList.size else idx + 1, newBlock)
        state = state.copy(blocks = newList)
    }

    fun moveBlockUp(id: String) {
        val idx = state.blocks.indexOfFirst { it.id == id }
        if (idx <= 0) return
        val newList = state.blocks.toMutableList()
        newList.add(idx - 1, newList.removeAt(idx))
        state = state.copy(blocks = newList)
    }

    fun moveBlockDown(id: String) {
        val idx = state.blocks.indexOfFirst { it.id == id }
        if (idx < 0 || idx >= state.blocks.size - 1) return
        val newList = state.blocks.toMutableList()
        newList.add(idx + 1, newList.removeAt(idx))
        state = state.copy(blocks = newList)
    }

    fun duplicateSelected() {
        if (state.selectedBlockIds.isEmpty()) return
        val newBlocks = state.blocks.toMutableList()
        val toInsert = mutableListOf<Pair<Int, EditorBlock>>()
        state.blocks.forEachIndexed { idx, block ->
            if (block.id in state.selectedBlockIds) {
                toInsert.add(idx + 1 + toInsert.size to block.copy(id = Uuid.random().toString()))
            }
        }
        toInsert.forEach { (idx, block) -> newBlocks.add(idx, block) }
        state = state.copy(blocks = newBlocks, selectedBlockIds = toInsert.map { it.second.id }.toSet())
    }
}

// ─────────────────────────────────────────────────────────────
// MAIN COMPOSABLE
// ─────────────────────────────────────────────────────────────

@Composable
fun ObsidianEditor() {
    val vm = remember { EditorViewModel() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var showTypeMenu by remember { mutableStateOf<String?>(null) }
    var showCommandPalette by remember { mutableStateOf(false) }

    MaterialTheme(colorScheme = darkColorScheme(
        background = BgPrimary, surface = BgSecondary,
        primary = AccentPurple, onBackground = TextPrimary
    )) {
        Box(
            Modifier
                .fillMaxSize()
                .background(BgPrimary)
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        when {
                            event.isCtrlPressed && event.key == Key.A -> { vm.selectAll(); true }
                            event.isCtrlPressed && event.key == Key.P -> { showCommandPalette = true; true }
                            event.key == Key.Escape -> { vm.clearSelection(); true }
                            event.key == Key.Delete && vm.state.selectedBlockIds.isNotEmpty() -> {
                                vm.deleteSelectedBlocks(); true
                            }
                            else -> false
                        }
                    } else false
                }
                .focusable()
        ) {
            Row(Modifier.fillMaxSize()) {
                // ── Sidebar ──────────────────────────────────
                EditorSidebar(
                    blocks = vm.state.blocks,
                    focusedId = vm.state.focusedBlockId,
                    onScrollTo = { id ->
                        val idx = vm.state.blocks.indexOfFirst { it.id == id }
                        if (idx >= 0) scope.launch { listState.animateScrollToItem(idx) }
                    }
                )

                // ── Main editor ──────────────────────────────
                Column(Modifier.weight(1f).fillMaxHeight()) {
                    EditorToolbar(
                        hasSelection = vm.state.selectedBlockIds.isNotEmpty(),
                        selectionCount = vm.state.selectedBlockIds.size,
                        onAddBlock = { vm.addBlockAfter(vm.state.focusedBlockId ?: vm.state.blocks.last().id) },
                        onDeleteSelected = vm::deleteSelectedBlocks,
                        onDuplicate = vm::duplicateSelected,
                        onSelectAll = vm::selectAll,
                        onClearSelection = vm::clearSelection,
                        onCommandPalette = { showCommandPalette = true }
                    )

                    // Selection count badge
                    if (vm.state.selectedBlockIds.isNotEmpty()) {
                        Row(
                            Modifier.fillMaxWidth().background(AccentPurple.copy(alpha = 0.15f))
                                .padding(horizontal = 24.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(vectors.Note, null, tint = AccentPurple, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${vm.state.selectedBlockIds.size} block(s) selected · Del to delete · Ctrl+D to duplicate",
                                color = AccentPurple, fontSize = 12.sp
                            )
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = vm::clearSelection) {
                                Text("Clear", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
                    ) {
                        items(vm.state.blocks, key = { it.id }) { block ->
                            val isSelected = block.id in vm.state.selectedBlockIds
                            val isFocused = block.id == vm.state.focusedBlockId

                            EditorBlockRow(
                                block = block,
                                isSelected = isSelected,
                                isFocused = isFocused,
                                onContentChange = { vm.updateBlock(block.id, it) },
                                onFocus = { vm.setFocus(block.id) },
                                onClickSelect = { additive -> vm.selectBlock(block.id, additive) },
                                onToggleCheck = { vm.toggleCheck(block.id) },
                                onEnter = { vm.addBlockAfter(block.id) },
                                onDelete = { vm.deleteBlock(block.id) },
                                onTypeMenuRequest = { showTypeMenu = block.id },
                                onMoveUp = { vm.moveBlockUp(block.id) },
                                onMoveDown = { vm.moveBlockDown(block.id) }
                            )
                        }
                    }
                }
            }

            // ── Block type picker ────────────────────────────
            if (showTypeMenu != null) {
                BlockTypePicker(
                    onDismiss = { showTypeMenu = null },
                    onSelect = { type ->
                        if (type == BlockType.IMAGE) {
                            vm.addImageBlock(showTypeMenu!!, "image_${nowInMs()}.png")
                        } else if (type == BlockType.DIVIDER) {
                            vm.addDivider(showTypeMenu!!)
                        } else {
                            vm.changeBlockType(showTypeMenu!!, type)
                        }
                        showTypeMenu = null
                    }
                )
            }

            // ── Command palette ──────────────────────────────
            if (showCommandPalette) {
                CommandPalette(
                    blocks = vm.state.blocks,
                    onDismiss = { showCommandPalette = false },
                    onSelect = { id ->
                        val idx = vm.state.blocks.indexOfFirst { it.id == id }
                        if (idx >= 0) scope.launch { listState.animateScrollToItem(idx) }
                        vm.setFocus(id)
                        showCommandPalette = false
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// SIDEBAR
// ─────────────────────────────────────────────────────────────

@Composable
fun EditorSidebar(
    blocks: List<EditorBlock>,
    focusedId: String?,
    onScrollTo: (String) -> Unit
) {
    Column(
        Modifier
            .width(220.dp)
            .fillMaxHeight()
            .background(BgSecondary)
            .border(width = 1.dp, color = BorderColor, shape = RectangleShape)
            .padding(vertical = 16.dp)
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).background(AccentPurple, RoundedCornerShape(50)))
            Spacer(Modifier.width(8.dp))
            Text("OUTLINE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp)
        }
        Divider(color = BorderColor, thickness = 1.dp)
        Spacer(Modifier.height(8.dp))

        blocks.filter { it.type in listOf(BlockType.HEADING1, BlockType.HEADING2, BlockType.HEADING3) }
            .forEach { block ->
                val indent = when (block.type) {
                    BlockType.HEADING1 -> 0.dp
                    BlockType.HEADING2 -> 12.dp
                    else -> 24.dp
                }
                val size = when (block.type) {
                    BlockType.HEADING1 -> 13.sp
                    BlockType.HEADING2 -> 12.sp
                    else -> 11.sp
                }
                val isFocused = block.id == focusedId
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(if (isFocused) AccentPurple.copy(0.15f) else Color.Transparent)
                        .clickable { onScrollTo(block.id) }
                        .padding(start = 16.dp + indent, end = 16.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isFocused) Box(Modifier.width(3.dp).height(14.dp).background(AccentPurple, RoundedCornerShape(2.dp)))
                    else Spacer(Modifier.width(3.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        block.content.ifBlank { "Untitled" },
                        color = if (isFocused) TextPrimary else TextSecondary,
                        fontSize = size,
                        fontWeight = if (block.type == BlockType.HEADING1) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

        Spacer(Modifier.weight(1f))
        Divider(color = BorderColor)
        Column(Modifier.padding(16.dp, 12.dp)) {
            Text("${blocks.size} blocks", color = TextMuted, fontSize = 11.sp)
            val wordCount = blocks.sumOf { it.content.split(" ").count { w -> w.isNotBlank() } }
            Text("~$wordCount words", color = TextMuted, fontSize = 11.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TOOLBAR
// ─────────────────────────────────────────────────────────────

@Composable
fun EditorToolbar(
    hasSelection: Boolean,
    selectionCount: Int,
    onAddBlock: () -> Unit,
    onDeleteSelected: () -> Unit,
    onDuplicate: () -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onCommandPalette: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(BgSecondary)
            .border(bottom = 1.dp, color = BorderColor)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("✦", color = AccentPurple, fontSize = 18.sp)
        Spacer(Modifier.width(12.dp))
        Text("Obsidian Editor", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))

        ToolbarBtn(vectors.Note, "New block", onClick = onAddBlock)
        ToolbarBtn(vectors.Note, "Command palette (Ctrl+P)", onClick = onCommandPalette)
        ToolbarBtn(vectors.Note, "Select all (Ctrl+A)", onClick = onSelectAll)

        if (hasSelection) {
            Spacer(Modifier.width(8.dp))
            Divider(Modifier.height(20.dp).width(1.dp), color = BorderColor)
            Spacer(Modifier.width(8.dp))
            ToolbarBtn(vectors.Note, "Duplicate", onClick = onDuplicate)
            ToolbarBtn(vectors.Note, "Delete selected", tint = Color(0xFFE57373), onClick = onDeleteSelected)
        }
    }
}

@Composable
fun ToolbarBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tooltip: String,
    tint: Color = TextSecondary,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
        Icon(icon, tooltip, tint = tint, modifier = Modifier.size(18.dp))
    }
}

// Extension for bottom border
fun Modifier.border(bottom: Dp, color: Color): Modifier = this.drawBehind {
    val y = size.height - bottom.toPx() / 2
    drawLine(color, Offset(0f, y), Offset(size.width, y), bottom.toPx())
}

// ─────────────────────────────────────────────────────────────
// BLOCK ROW
// ─────────────────────────────────────────────────────────────

@Composable
fun EditorBlockRow(
    block: EditorBlock,
    isSelected: Boolean,
    isFocused: Boolean,
    onContentChange: (String) -> Unit,
    onFocus: () -> Unit,
    onClickSelect: (additive: Boolean) -> Unit,
    onToggleCheck: () -> Unit,
    onEnter: () -> Unit,
    onDelete: () -> Unit,
    onTypeMenuRequest: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    var hovered by remember { mutableStateOf(false) }
    val bgColor = when {
        isSelected -> BgSelected
        isFocused  -> BgSurface
        hovered    -> BgSurface.copy(alpha = 0.5f)
        else       -> Color.Transparent
    }

    Row(
        Modifier
            .fillMaxWidth()
            .background(bgColor)
            .then(
                if (isSelected) Modifier.border(
                    width = 1.dp,
                    color = AccentPurple.copy(0.5f),
                    shape = RectangleShape
                ) else Modifier
            )
            .onPointerEvent(PointerEventType.Enter) { hovered = true }
            .onPointerEvent(PointerEventType.Exit) { hovered = false }
            .pointerInput(block.id) {
                detectTapGestures(
                    onTap = { onFocus() },
                    onLongPress = { onClickSelect(true) }
                )
            }
            .padding(horizontal = 40.dp, vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Block handle (left)
        Column(
            Modifier.width(32.dp).padding(top = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hovered || isSelected) {
                // Selection checkbox
                Box(
                    Modifier
                        .size(16.dp)
                        .background(
                            if (isSelected) AccentPurple else Color.Transparent,
                            RoundedCornerShape(3.dp)
                        )
                        .border(1.dp, if (isSelected) AccentPurple else TextMuted, RoundedCornerShape(3.dp))
                        .clickable { onClickSelect(true) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) Icon(vectors.Note, null, tint = Color.White, modifier = Modifier.size(10.dp))
                }

                Spacer(Modifier.height(4.dp))

                // Drag handle / type button
                Icon(
                    vectors.Note, null,
                    tint = TextMuted, modifier = Modifier.size(14.dp).clickable { onTypeMenuRequest() }
                )
            }
        }

        // Block content
        Box(Modifier.weight(1f).padding(start = 8.dp)) {
            when (block.type) {
                BlockType.DIVIDER -> HorizontalDivider(block)
                BlockType.IMAGE   -> ImageBlock(block)
                BlockType.CHECKBOX -> CheckboxBlock(block, onContentChange, onFocus, onToggleCheck, onEnter, onDelete)
                else -> TextBlock(block, onContentChange, onFocus, onEnter, onDelete)
            }
        }

        // Right controls (move up/down)
        if (hovered || isSelected) {
            Column(Modifier.padding(start = 4.dp, top = 2.dp)) {
                Icon(vectors.Note, "Up",
                    tint = TextMuted, modifier = Modifier.size(16.dp).clickable { onMoveUp() })
                Icon(vectors.Note, "Down",
                    tint = TextMuted, modifier = Modifier.size(16.dp).clickable { onMoveDown() })
            }
        } else {
            Spacer(Modifier.width(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// BLOCK RENDERERS
// ─────────────────────────────────────────────────────────────

@Composable
fun TextBlock(
    block: EditorBlock,
    onContentChange: (String) -> Unit,
    onFocus: () -> Unit,
    onEnter: () -> Unit,
    onDelete: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var textValue by remember(block.id) { mutableStateOf(TextFieldValue(block.content)) }

    val textStyle = when (block.type) {
        BlockType.HEADING1    -> TextStyle(color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 38.sp)
        BlockType.HEADING2    -> TextStyle(color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, lineHeight = 30.sp)
        BlockType.HEADING3    -> TextStyle(color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium, lineHeight = 26.sp)
        BlockType.QUOTE       -> TextStyle(color = TextSecondary, fontSize = 15.sp, fontStyle = FontStyle.Italic, lineHeight = 24.sp)
        BlockType.CODE_BLOCK  -> TextStyle(color = AccentGreen, fontSize = 13.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, lineHeight = 20.sp)
        BlockType.BULLET_LIST -> TextStyle(color = TextPrimary, fontSize = 15.sp, lineHeight = 24.sp)
        BlockType.ORDERED_LIST -> TextStyle(color = TextPrimary, fontSize = 15.sp, lineHeight = 24.sp)
        else                  -> TextStyle(color = TextPrimary, fontSize = 15.sp, lineHeight = 24.sp)
    }

    val prefix: @Composable (() -> Unit)? = when (block.type) {
        BlockType.QUOTE -> ({
            Box(Modifier.width(3.dp).height(20.dp).background(AccentPurple, RoundedCornerShape(2.dp)))
            Spacer(Modifier.width(12.dp))
        })
        BlockType.CODE_BLOCK -> ({
            Box(Modifier.fillMaxWidth().background(Color(0xFF1A1A2E), RoundedCornerShape(6.dp)).padding(12.dp)) {}
        })
        BlockType.BULLET_LIST -> ({
            Text("•", color = AccentPurple, fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp, top = 2.dp))
        })
        BlockType.ORDERED_LIST -> ({
            Text("${block.orderIndex + 1}.", color = AccentPurple, fontSize = 15.sp, modifier = Modifier.padding(end = 8.dp, top = 2.dp))
        })
        else -> null
    }

    val topPad = when (block.type) {
        BlockType.HEADING1 -> 20.dp
        BlockType.HEADING2 -> 14.dp
        BlockType.HEADING3 -> 10.dp
        else -> 2.dp
    }

    Row(Modifier.fillMaxWidth().padding(top = topPad, bottom = 2.dp), verticalAlignment = Alignment.Top) {
        prefix?.invoke()

        if (block.type == BlockType.CODE_BLOCK) {
            Box(
                Modifier.fillMaxWidth()
                    .background(Color(0xFF12122A), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                BasicTextField(
                    value = textValue,
                    onValueChange = { v -> textValue = v; onContentChange(v.text) },
                    textStyle = textStyle.copy(background = Color.Transparent),
                    modifier = Modifier.fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { if (it.isFocused) onFocus() }
                        .onKeyEvent { e ->
                            if (e.type == KeyEventType.KeyDown && e.key == Key.Enter && e.isShiftPressed) {
                                onEnter(); true
                            } else if (e.type == KeyEventType.KeyDown && e.key == Key.Backspace && block.content.isEmpty()) {
                                onDelete(); true
                            } else false
                        },
                    cursorBrush = SolidColor(AccentPurple)
                )
            }
        } else {
            BasicTextField(
                value = textValue,
                onValueChange = { v -> textValue = v; onContentChange(v.text) },
                textStyle = textStyle,
                modifier = Modifier.fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { if (it.isFocused) onFocus() }
                    .onKeyEvent { e ->
                        if (e.type == KeyEventType.KeyDown) {
                            when (e.key) {
                                Key.Enter -> { if (!e.isShiftPressed) { onEnter(); true } else false }
                                Key.Backspace -> if (block.content.isEmpty()) { onDelete(); true } else false
                                else -> false
                            }
                        } else false
                    },
                cursorBrush = SolidColor(AccentPurple),
                decorationBox = { inner ->
                    Box {
                        if (textValue.text.isEmpty()) {
                            Text(
                                when (block.type) {
                                    BlockType.HEADING1 -> "Heading 1"
                                    BlockType.HEADING2 -> "Heading 2"
                                    BlockType.HEADING3 -> "Heading 3"
                                    BlockType.QUOTE    -> "Quote..."
                                    else               -> "Type something..."
                                },
                                style = textStyle.copy(color = TextMuted)
                            )
                        }
                        inner()
                    }
                }
            )
        }
    }
}

@Composable
fun CheckboxBlock(
    block: EditorBlock,
    onContentChange: (String) -> Unit,
    onFocus: () -> Unit,
    onToggleCheck: () -> Unit,
    onEnter: () -> Unit,
    onDelete: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var textValue by remember(block.id) { mutableStateOf(TextFieldValue(block.content)) }

    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(18.dp)
                .background(
                    if (block.checked) AccentGreen else Color.Transparent,
                    RoundedCornerShape(4.dp)
                )
                .border(1.5.dp, if (block.checked) AccentGreen else TextMuted, RoundedCornerShape(4.dp))
                .clickable { onToggleCheck() },
            contentAlignment = Alignment.Center
        ) {
            if (block.checked) Icon(vectors.Note, null, tint = Color.White, modifier = Modifier.size(12.dp))
        }
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = textValue,
            onValueChange = { v -> textValue = v; onContentChange(v.text) },
            textStyle = TextStyle(
                color = if (block.checked) TextMuted else TextPrimary,
                fontSize = 15.sp,
                textDecoration = if (block.checked) TextDecoration.LineThrough else null,
                lineHeight = 22.sp
            ),
            modifier = Modifier.weight(1f)
                .focusRequester(focusRequester)
                .onFocusChanged { if (it.isFocused) onFocus() }
                .onKeyEvent { e ->
                    if (e.type == KeyEventType.KeyDown) {
                        when (e.key) {
                            Key.Enter -> { onEnter(); true }
                            Key.Backspace -> if (block.content.isEmpty()) { onDelete(); true } else false
                            else -> false
                        }
                    } else false
                },
            cursorBrush = SolidColor(AccentPurple),
            decorationBox = { inner ->
                Box {
                    if (textValue.text.isEmpty()) Text("To-do item...", color = TextMuted, fontSize = 15.sp)
                    inner()
                }
            }
        )
    }
}

@Composable
fun HorizontalDivider(block: EditorBlock) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).height(1.dp).background(
            Brush.horizontalGradient(listOf(Color.Transparent, BorderColor, BorderColor, Color.Transparent))
        ))
    }
}

@Composable
fun ImageBlock(block: EditorBlock) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(BgSurface, RoundedCornerShape(8.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(vectors.Note, null, tint = TextMuted, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(8.dp))
            Text(block.imageName ?: "Image", color = TextSecondary, fontSize = 13.sp)
            Text("Click to open • Drag to reposition", color = TextMuted, fontSize = 11.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// BLOCK TYPE PICKER
// ─────────────────────────────────────────────────────────────

data class BlockTypeEntry(val type: BlockType, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val shortcut: String)

@Composable
fun BlockTypePicker(onDismiss: () -> Unit, onSelect: (BlockType) -> Unit) {
    val entries = listOf(
        BlockTypeEntry(BlockType.PARAGRAPH,    "Paragraph",    vectors.Note,       "p"),
        BlockTypeEntry(BlockType.HEADING1,     "Heading 1",    vectors.Note,            "h1"),
        BlockTypeEntry(BlockType.HEADING2,     "Heading 2",    vectors.Note,            "h2"),
        BlockTypeEntry(BlockType.HEADING3,     "Heading 3",    vectors.Note,            "h3"),
        BlockTypeEntry(BlockType.BULLET_LIST,  "Bullet List",  vectors.Note,             "ul"),
        BlockTypeEntry(BlockType.ORDERED_LIST, "Numbered List",vectors.Note,"ol"),
        BlockTypeEntry(BlockType.CHECKBOX,     "Checkbox",     vectors.Note,         "[]"),
        BlockTypeEntry(BlockType.QUOTE,        "Quote",        vectors.Note,      ">"),
        BlockTypeEntry(BlockType.CODE_BLOCK,   "Code Block",   vectors.Note,             "```"),
        BlockTypeEntry(BlockType.IMAGE,        "Image",        vectors.Note,            "img"),
        BlockTypeEntry(BlockType.DIVIDER,      "Divider",      vectors.Note,   "---"),
    )

    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.6f)).clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) { onDismiss() }, contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.width(340.dp),
            color = BgSecondary,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 24.dp
        ) {
            Column(Modifier.padding(8.dp)) {
                Text("Turn into", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                entries.forEach { entry ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onSelect(entry.type) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(entry.icon, null, tint = AccentPurple, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(entry.label, color = TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Text(entry.shortcut, color = TextMuted, fontSize = 12.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// COMMAND PALETTE
// ─────────────────────────────────────────────────────────────

@Composable
fun CommandPalette(
    blocks: List<EditorBlock>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val results = remember(query, blocks) {
        if (query.isBlank()) blocks.filter { it.type in listOf(BlockType.HEADING1, BlockType.HEADING2, BlockType.HEADING3) }
        else blocks.filter { it.content.contains(query, ignoreCase = true) }
    }

    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(0.7f))
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDismiss() },
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            modifier = Modifier.padding(top = 80.dp).width(520.dp),
            color = BgSecondary,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 32.dp
        ) {
            Column {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(vectors.Note, null, tint = TextMuted, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        textStyle = TextStyle(color = TextPrimary, fontSize = 16.sp),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        cursorBrush = SolidColor(AccentPurple),
                        decorationBox = { inner ->
                            Box {
                                if (query.isEmpty()) Text("Search blocks...", color = TextMuted, fontSize = 16.sp)
                                inner()
                            }
                        }
                    )
                }
                Divider(color = BorderColor)
                if (results.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No results found", color = TextMuted, fontSize = 14.sp)
                    }
                } else {
                    Column(Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                        results.forEach { block ->
                            Row(
                                Modifier.fillMaxWidth().clickable { onSelect(block.id) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val (icon, color) = when (block.type) {
                                    BlockType.HEADING1, BlockType.HEADING2, BlockType.HEADING3 ->
                                        vectors.Note to AccentPurple
                                    BlockType.CHECKBOX -> vectors.Note to AccentGreen
                                    BlockType.IMAGE -> vectors.Note to Color(0xFF64B5F6)
                                    BlockType.QUOTE -> vectors.Note to Color(0xFFFFB74D)
                                    else -> vectors.Note to TextSecondary
                                }
                                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    block.content.ifBlank { "(empty)" },
                                    color = if (block.content.isBlank()) TextMuted else TextPrimary,
                                    fontSize = 14.sp, maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                Divider(color = BorderColor)
                Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    listOf("↑↓ Navigate", "↵ Jump", "Esc Close").forEach {
                        Text(it, color = TextMuted, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// ENTRY POINT (Desktop)
// ─────────────────────────────────────────────────────────────

// In your main.kt for Desktop:
// fun main() = application {
//     Window(
//         onCloseRequest = ::exitApplication,
//         title = "Obsidian Editor",
//         state = rememberWindowState(width = 1200.dp, height = 800.dp)
//     ) {
//         ObsidianEditor()
//     }
// }

// In your App.kt for Android/iOS:
// @Composable
// fun App() {
//     ObsidianEditor()
// }