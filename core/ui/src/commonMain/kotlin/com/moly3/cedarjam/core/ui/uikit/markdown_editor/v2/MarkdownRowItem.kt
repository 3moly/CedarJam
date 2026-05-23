package com.moly3.cedarjam.core.ui.uikit.markdown_editor.v2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.domain.features.mdprops.DividerSyntax
import com.moly3.cedarjam.core.domain.features.mdprops.MarkdownRow
import com.moly3.cedarjam.core.domain.features.mdprops.RowFocusManager
import com.moly3.cedarjam.core.domain.features.mdprops.RowType
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalTextStyle
import com.moly3.cedarjam.core.ui.uikit.CJDivider
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.CJTextField

/**
 * One editable block in the document. This is the heart of the editor and owns:
 *
 *  - Per-row [TextFieldValue] state (text + caret), synced to the model.
 *  - The slash ("/") menu trigger and dispatch.
 *  - Enter vs Shift+Enter behaviour (see [handleEnter]).
 *  - Arrow-key navigation between rows via [RowFocusManager].
 *  - Backspace-at-start merging into the previous row.
 *  - Type-specific rendering (headings, code, quote, image, divider, lists).
 */
@Composable
fun MarkdownRowItem(
    row: MarkdownRow,
    index: Int,
    isLast: Boolean,
    focusManager: RowFocusManager,
    callbacks: RowCallbacks,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
) {
    // Tint applied to the whole row while it is part of a block selection.
    val selectionModifier: Modifier =
        if (isSelected) {
            Modifier.background(
                LocalAppTheme.current.colors.backgroundPrimary.copy(alpha = 0.14f),
                RoundedCornerShape(4.dp),
            )
        } else {
            Modifier
        }

    // NOTE: divider rows are NOT special-cased away here any more. They flow
    // through the same focus / navigation / key-handling path as every other
    // row so arrows can land on them. A divider just renders differently when
    // blurred (a horizontal line) vs focused (its raw "---" source) — see
    // RowTextField's decorationBox.

    val requester = remember { FocusRequester() }

    // Register/unregister this row with the focus manager for lazy-list focus.
    DisposableEffect(row.id) {
        focusManager.register(row.id, requester)
        onDispose { focusManager.unregister(row.id) }
    }

    // Local editing state. We keep caret info here; the model only stores text.
    var fieldValue by remember(row.id) {
        mutableStateOf(TextFieldValue(text = row.text, selection = TextRange(row.text.length)))
    }
    // Keep local text in sync if the model text changed externally (e.g. merge).
    LaunchedEffect(row.text) {
        if (row.text != fieldValue.text) {
            val caret = fieldValue.selection.end.coerceAtMost(row.text.length)
            fieldValue = fieldValue.copy(
                text = row.text,
                selection = TextRange(caret),
            )
        }
    }

    // Slash menu state for this row.
    var slashActive by remember(row.id) { mutableStateOf(false) }
    var slashQuery by remember(row.id) { mutableStateOf("") }

    // Claim a pending focus request when (re)composed.
    LaunchedEffect(Unit) {
        if (focusManager.isPending(row.id)) {
            val caret = focusManager.pendingCaret(row.id)
            runCatching { requester.requestFocus() }
            fieldValue = when (caret) {
                RowFocusManager.CaretTarget.Start -> fieldValue.caretAtStart()
                else -> fieldValue.caretAtEnd()
            }
            focusManager.consumePending(row.id)
        }
    }

    Column(modifier = modifier.fillMaxWidth().then(selectionModifier)) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {

            RowLeadingDecoration(row = row, index = index)
            val offset = remember { mutableStateOf(IntOffset.Zero) }
            Column(modifier = Modifier.weight(1f)) {
                RowTextField(
                    modifier = Modifier.onGloballyPositioned {
                        offset.value = IntOffset(0, it.size.height)
                    },
                    row = row,
                    fieldValue = fieldValue,
                    requester = requester,
                    onValueChange = { newValue ->
                        val result = processValueChange(
                            old = fieldValue,
                            new = newValue,
                            row = row,
                            slashActive = slashActive,
                        )
                        slashActive = result.slashActive
                        slashQuery = result.slashQuery
                        fieldValue = result.value
                        if (result.value.text != row.text) {
                            // Typing is an edit — drop any block selection.
                            callbacks.onClearSelection()
                            callbacks.onTextChange(row.id, result.value.text)
                        }
                    },
                    onFocusChange = { focused ->
                        if (row.type == RowType.Divider) {
                            if (focused) {
                                // Reveal editable source. A divider decoded from
                                // text already carries "---"; a freshly created
                                // one may be blank — show the canonical rule so
                                // there is something to edit.
                                if (fieldValue.text.isBlank()) {
                                    val seeded = DividerSyntax.CANONICAL
                                    fieldValue = TextFieldValue(
                                        text = seeded,
                                        selection = TextRange(seeded.length),
                                    )
                                    callbacks.onTextChange(row.id, seeded)
                                }
                            } else {
                                // Blurred: keep as divider if still valid syntax,
                                // otherwise the editor demotes it to a paragraph.
                                callbacks.onDividerBlur(row.id)
                            }
                        }
                    },
                    onKeyEvent = { keyEvent ->
                        handleKeyEvent(
                            keyEvent = keyEvent,
                            row = row,
                            fieldValue = fieldValue,
                            slashActive = slashActive,
                            callbacks = callbacks,
                            onLocalValueChange = { updated ->
                                // Used by Shift+Enter so the caret lands right
                                // after the inserted newline within this row.
                                fieldValue = updated
                                if (updated.text != row.text) {
                                    callbacks.onTextChange(row.id, updated.text)
                                }
                            },
                        )
                    },
                )

                // Slash menu anchored to this row.
                if (slashActive) {
                    SlashMenu(
                        offset = offset.value,
                        query = slashQuery,
                        onSelect = { selectedType ->
                            // Strip the "/query" token, switch the row type.
                            val stripped = stripSlashToken(fieldValue)
                            slashActive = false
                            slashQuery = ""
                            if (selectedType == RowType.Divider && stripped.text.isBlank()) {
                                // A divider created via the slash menu seeds its
                                // canonical "---" source immediately, so blurring
                                // without typing keeps it a divider (instead of
                                // demoting an empty row to a paragraph).
                                val seeded = TextFieldValue(
                                    text = DividerSyntax.CANONICAL,
                                    selection = TextRange(DividerSyntax.CANONICAL.length),
                                )
                                fieldValue = seeded
                                callbacks.onTextChange(row.id, seeded.text)
                            } else {
                                fieldValue = stripped
                                callbacks.onTextChange(row.id, stripped.text)
                            }
                            callbacks.onTypeChange(row.id, selectedType)
                        },
                        onDismiss = {
                            slashActive = false
                            slashQuery = ""
                        },
                    )
                }
            }
        }
    }
}

/* ==================================================================================
 * Text field
 * ================================================================================ */

@Composable
private fun RowTextField(
    modifier: Modifier,
    row: MarkdownRow,
    fieldValue: TextFieldValue,
    requester: FocusRequester,
    onValueChange: (TextFieldValue) -> Unit,
    onKeyEvent: (androidx.compose.ui.input.key.KeyEvent) -> Boolean,
    onFocusChange: (Boolean) -> Unit = {},
) {
    val theme = LocalAppTheme.current
    val isCode = row.type == RowType.Code
    val isDivider = row.type == RowType.Divider
    val style = row.type.textStyle()

    var isFocused by remember { mutableStateOf(false) }

    val container = when {
        isCode -> Modifier
            .fillMaxWidth()
            .background(
                theme.colors.backgroundPrimary,
                RoundedCornerShape(8.dp),
            )
            .padding(12.dp)

        else -> Modifier.fillMaxWidth().padding(vertical = 2.dp)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (row.type == RowType.Image && fieldValue.text.isNotBlank()) {
            ImagePreview(url = fieldValue.text)
        }

        Box(modifier = container) {
            CJTextField(
                value = fieldValue,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(requester)
                    .onFocusChanged {
                        if (it.isFocused != isFocused) {
                            isFocused = it.isFocused
                            onFocusChange(it.isFocused)
                        }
                    }
                    .onPreviewKeyEvent(onKeyEvent),
                textStyle = style,
                singleLine = false,
                imeAction = if (row.type.isMultiline) ImeAction.Default else ImeAction.None,
                decorationBox = { inner ->
                    when {
                        // Blurred divider -> show the horizontal rule. The text
                        // field's own content (inner) is still composed so the
                        // field stays focusable & measurable, just hidden under
                        // the line via a Box overlay.
                        isDivider && !isFocused -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                // Hidden text content (kept for focus/measure).
                                Box(Modifier.height(0.dp)) { inner() }

                                CJDivider(color = theme.colors.backgroundPrimary)
                            }
                        }
                        // Empty focused row -> placeholder hint behind the caret.
                        fieldValue.text.isEmpty() && isFocused -> {
                            CJText(
                                text = row.type.placeholder(),
                                style = style,
                                color = theme.colors.secondaryFont
                            )
                            inner()
                        }

                        else -> inner()
                    }
                },
            )
        }
    }
}

/* ==================================================================================
 * Image preview
 * ================================================================================ */

/**
 * Renders the image for an [RowType.Image] row.
 *
 * Compose Multiplatform has no built-in network image loader, so the actual
 * rendering is delegated to a host-provided composable via [LocalMarkdownImageLoader].
 * Wire it up with Coil 3 (`coil-compose`, which is KMP-ready) or any other loader,
 * e.g. in your app:
 *
 * ```
 * CompositionLocalProvider(
 *     LocalMarkdownImageLoader provides { url, modifier ->
 *         AsyncImage(model = url, contentDescription = null, modifier = modifier)
 *     },
 * ) { MarkdownEditor(...) }
 * ```
 *
 * Without a loader provided, a neutral placeholder showing the URL is drawn.
 */
@Composable
private fun ImagePreview(url: String) {
    val loader = LocalMarkdownImageLoader.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    ) {
        if (loader != null) {
            loader(url, Modifier.fillMaxWidth())
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        LocalAppTheme.current.colors.backgroundPrimary,
                        RoundedCornerShape(8.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                CJText(
                    "🖼  $url",
                )
            }
        }
    }
}

/* ==================================================================================
 * Leading decoration: bullets, numbers, quote bar, code language label
 * ================================================================================ */

@Composable
private fun RowLeadingDecoration(row: MarkdownRow, index: Int) {
    when (row.type) {
        RowType.BulletList -> {
            CJText(
                "•",
                modifier = Modifier.padding(end = 8.dp, top = 2.dp),
//                style = typography.bodyLarge,
            )
        }

        RowType.NumberedList -> {
            CJText(
                "${index + 1}.",
                modifier = Modifier.padding(end = 8.dp, top = 2.dp),
//                style = typography.bodyMedium,
//                color = colorScheme.onSurfaceVariant,
            )
        }

        RowType.Quote -> {
            Box(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .width(3.dp)
                    .height(24.dp)
                    .background(LocalAppTheme.current.primaryColor),
            )
        }

        else -> Spacer(Modifier.width(0.dp))
    }
}

/* ==================================================================================
 * Value-change processing — detects "/" to open the slash menu
 * ================================================================================ */

private data class ValueChangeResult(
    val value: TextFieldValue,
    val slashActive: Boolean,
    val slashQuery: String,
)

/**
 * Inspects a text edit and decides whether the slash menu should be open and what
 * the current query is. The menu opens when the user types "/" at the start of an
 * (otherwise empty) row, and tracks everything typed after it as the query.
 */
private fun processValueChange(
    old: TextFieldValue,
    new: TextFieldValue,
    row: MarkdownRow,
    slashActive: Boolean,
): ValueChangeResult {
    // Code blocks never trigger the slash menu — "/" is just a character there.
    if (row.type == RowType.Code) {
        return ValueChangeResult(new, slashActive = false, slashQuery = "")
    }

    val text = new.text
    val caret = new.selection.start

    // Find a slash token immediately preceding the caret on the current line.
    val lineStart = text.lastIndexOf('\n', startIndex = (caret - 1).coerceAtLeast(0))
        .let { if (it < 0) 0 else it + 1 }
    val lineUpToCaret = text.substring(lineStart, caret.coerceIn(0, text.length))

    // Opens the menu only when the line is "/something" with no spaces.
    val isSlashCommand = lineUpToCaret.startsWith("/") &&
            !lineUpToCaret.contains(' ') &&
            !lineUpToCaret.contains('\n')

    return if (isSlashCommand) {
        ValueChangeResult(
            value = new,
            slashActive = true,
            slashQuery = lineUpToCaret.removePrefix("/"),
        )
    } else {
        ValueChangeResult(new, slashActive = false, slashQuery = "")
    }
}

/** Removes the trailing "/query" token from the current line. */
private fun stripSlashToken(value: TextFieldValue): TextFieldValue {
    val text = value.text
    val caret = value.selection.start
    val lineStart = text.lastIndexOf('\n', startIndex = (caret - 1).coerceAtLeast(0))
        .let { if (it < 0) 0 else it + 1 }
    val before = text.substring(0, lineStart)
    val after = text.substring(caret.coerceIn(0, text.length))
    val merged = before + after
    return TextFieldValue(text = merged, selection = TextRange(before.length))
}

/* ==================================================================================
 * Key handling — Enter / Shift+Enter / arrows / backspace
 * ================================================================================ */

private fun handleKeyEvent(
    keyEvent: androidx.compose.ui.input.key.KeyEvent,
    row: MarkdownRow,
    fieldValue: TextFieldValue,
    slashActive: Boolean,
    callbacks: RowCallbacks,
    onLocalValueChange: (TextFieldValue) -> Unit,
): Boolean {
    if (keyEvent.type != KeyEventType.KeyDown) return false

    // While the slash menu is open, let it (not us) own the keys.
    if (slashActive && keyEvent.key in setOf(Key.DirectionUp, Key.DirectionDown, Key.Enter)) {
        return false
    }

    val ctrl = keyEvent.isCtrlPressed || keyEvent.isMetaPressed

    // --- Copy block selection as raw Markdown (Ctrl/Cmd+C) ----------------------
    // Only intercept when the field has NO in-row text selection; otherwise the
    // user is copying text within the field and the platform should handle it.
    if (ctrl && keyEvent.key == Key.C && fieldValue.selection.collapsed) {
        callbacks.onCopySelection(row.id)
        return true
    }

    if (keyEvent.isShiftPressed && keyEvent.key == Key.Tab) {
        return handleShiftTab(fieldValue, callbacks, onLocalValueChange)
    }
    if (ctrl && keyEvent.key == Key.Z) {
        if (keyEvent.isShiftPressed) callbacks.onRedo() else callbacks.onUndo()
        return true
    }
    if (ctrl && keyEvent.key == Key.Y) {
        callbacks.onRedo()
        return true
    }

    return when (keyEvent.key) {
        Key.Tab -> handleTab(fieldValue, callbacks, onLocalValueChange)
        Key.Enter -> {
            // Editing keys end any block selection.
            callbacks.onClearSelection()
            handleEnter(
                keyEvent.isShiftPressed,
                row,
                fieldValue,
                callbacks,
                onLocalValueChange,
            )
        }

        Key.Backspace -> {
            callbacks.onClearSelection()
            if (fieldValue.text.isEmpty() || fieldValue.caretAtDocumentStart()) {
                callbacks.onMergeWithPrevious(row.id)
                true
            } else {
                false
            }
        }

        Key.DirectionUp -> {
            if (fieldValue.caretOnFirstLine()) {
                if (keyEvent.isShiftPressed) {
                    callbacks.onExtendSelection(row.id, NavDirection.Up)
                } else {
                    callbacks.onNavigate(row.id, NavDirection.Up)
                }
                true
            } else false
        }

        Key.DirectionDown -> {
            if (fieldValue.caretOnLastLine()) {
                if (keyEvent.isShiftPressed) {
                    callbacks.onExtendSelection(row.id, NavDirection.Down)
                } else {
                    callbacks.onNavigate(row.id, NavDirection.Down)
                }
                true
            } else false
        }

        else -> false
    }
}


private fun handleShiftTab(
    fieldValue: TextFieldValue,
    callbacks: RowCallbacks,
    onLocalValueChange: (TextFieldValue) -> Unit,
): Boolean {
    val text = fieldValue.text
    val selection = fieldValue.selection

    // 1. Находим начало и конец строк, в которых находится курсор/выделение
    val startLineIndex =
        if (selection.min == 0) 0 else text.lastIndexOf('\n', selection.min - 1) + 1
    val endLineIndex = text.indexOf('\n', selection.max).let { if (it == -1) text.length else it }

    // Разбиваем текст на 3 части: до выделенных строк, сами строки и после
    val textBefore = text.substring(0, startLineIndex)
    val textToProcess = text.substring(startLineIndex, endLineIndex)
    val textAfter = text.substring(endLineIndex)

    var newMin = selection.min
    var newMax = selection.max
    var currentOriginalOffset = startLineIndex

    // 2. Убираем отступы из затронутых строк и пересчитываем выделение
    val modifiedLines = textToProcess.split('\n').joinToString("\n") { line ->
        // Определяем количество удаляемых символов: 1 таб или до 4 пробелов
        val removeCount = when {
            line.startsWith("\t") -> 1
            line.startsWith("    ") -> 4
            line.startsWith("   ") -> 3
            line.startsWith("  ") -> 2
            line.startsWith(" ") -> 1
            else -> 0
        }

        // Сдвигаем начальную позицию выделения
        if (currentOriginalOffset < selection.min) {
            newMin -= minOf(removeCount, selection.min - currentOriginalOffset)
        }

        // Сдвигаем конечную позицию выделения
        if (currentOriginalOffset < selection.max) {
            newMax -= minOf(removeCount, selection.max - currentOriginalOffset)
        }

        // Обновляем оффсет для следующей строки (длина оригинальной строки + перенос \n)
        currentOriginalOffset += line.length + 1

        // Возвращаем строку без отступа
        line.substring(removeCount)
    }

    // 3. Формируем новый текст и безопасный Range для курсора
    val newText = textBefore + modifiedLines + textAfter
    val newSelection = TextRange(maxOf(0, newMin), maxOf(0, newMax))

    // 4. Полноценно загружаем измененный объект в callback
    onLocalValueChange(
        fieldValue.copy(
            text = newText,
            selection = newSelection
        )
    )

    return true
}

private fun handleTab(
    fieldValue: TextFieldValue,
    callbacks: RowCallbacks,
    onLocalValueChange: (TextFieldValue) -> Unit,
): Boolean {
    val text = fieldValue.text
    val selection = fieldValue.selection

    // Вы можете использовать "\t" или нужное количество пробелов
    val tabString = "    "

    val selectedText = text.substring(selection.min, selection.max)

    if (selectedText.contains('\n') || selectedText.isNotEmpty()) {
        // --- СЦЕНАРИЙ 1: Многострочное выделение ---

        // Находим границы строк
        val startLineIndex =
            if (selection.min == 0) 0 else text.lastIndexOf('\n', selection.min - 1) + 1
        val endLineIndex =
            text.indexOf('\n', selection.max).let { if (it == -1) text.length else it }

        val textBefore = text.substring(0, startLineIndex)
        val textToProcess = text.substring(startLineIndex, endLineIndex)
        val textAfter = text.substring(endLineIndex)

        var newMin = selection.min
        var newMax = selection.max
        var currentOriginalOffset = startLineIndex

        // Добавляем отступ в начало каждой строки
        val modifiedLines = textToProcess.split('\n').joinToString("\n") { line ->
            // Сдвигаем выделение вправо
            if (currentOriginalOffset <= selection.min) newMin += tabString.length
            if (currentOriginalOffset < selection.max) newMax += tabString.length

            currentOriginalOffset += line.length + 1

            tabString + line
        }

        onLocalValueChange(
            fieldValue.copy(
                text = textBefore + modifiedLines + textAfter,
                selection = TextRange(newMin, newMax)
            )
        )
    } else {
        // --- СЦЕНАРИЙ 2: Обычный ввод или выделение на одной строке ---

        val textBefore = text.substring(0, selection.min)
        val textAfter = text.substring(selection.max)

        onLocalValueChange(
            fieldValue.copy(
                // Вставляем tabString на место курсора/выделения
                text = textBefore + tabString + textAfter,
                // Ставим курсор сразу после вставленного отступа
                selection = TextRange(selection.min + tabString.length)
            )
        )
    }

    return true
}

/**
 * Enter behaviour. The rule is the same for every row type:
 *  - **Shift+Enter** inserts a newline *inside* the current row and consumes the
 *    event, so no new row is created. This is what makes the multiline
 *    [RowType.Code] block usable, and also gives soft line breaks elsewhere.
 *  - **Plain Enter** splits the row at the caret: text before the caret stays,
 *    text after it moves into a freshly created row below, which gets focus.
 *
 * Returns true if the event was consumed.
 */
private fun handleEnter(
    shiftPressed: Boolean,
    row: MarkdownRow,
    fieldValue: TextFieldValue,
    callbacks: RowCallbacks,
    onLocalValueChange: (TextFieldValue) -> Unit,
): Boolean {
    return if (shiftPressed) {
        // Insert "\n" at the caret and stay in this row, moving the caret past it.
        // Consuming the event prevents the editor creating + navigating to a new row.
        val caret = fieldValue.selection.start
        val newText = fieldValue.text.substring(0, caret) + "\n" +
                fieldValue.text.substring(caret)
        onLocalValueChange(
            fieldValue.copy(text = newText, selection = TextRange(caret + 1)),
        )
        true
    } else {
        // Plain Enter: split the row at the caret, create + focus the next row.
        val caret = fieldValue.selection.start
        val before = fieldValue.text.substring(0, caret)
        val after = fieldValue.text.substring(caret)
        callbacks.onSplitRow(row.id, before, after)
        true
    }
}

/* ==================================================================================
 * Per-type styling
 * ================================================================================ */

@Composable
private fun RowType.textStyle(): TextStyle {
    val textStyle = LocalTextStyle.current

    return when (this) {
        RowType.Heading1 -> textStyle.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        )

        RowType.Heading2 -> textStyle.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )

        RowType.Heading3 -> textStyle.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        )

        RowType.Quote -> textStyle.copy(
            fontWeight = FontWeight.Normal,
//            color = scheme.onSurfaceVariant,
        )

        RowType.Code -> textStyle.copy()
        // Divider source ("---") edits in a monospace face, like a code snippet.
        RowType.Divider -> textStyle.copy(
//            color = scheme.onSurfaceVariant,
        )

        else -> textStyle
    }
}

private fun RowType.placeholder(): String = when (this) {
    RowType.Paragraph -> "Type '/' for commands"
    RowType.Heading1 -> "Heading 1"
    RowType.Heading2 -> "Heading 2"
    RowType.Heading3 -> "Heading 3"
    RowType.BulletList -> "List item"
    RowType.NumberedList -> "List item"
    RowType.Quote -> "Quote"
    RowType.Code -> "// code"
    RowType.Image -> "Paste image URL…"
    RowType.Divider -> "--- (divider)"
}