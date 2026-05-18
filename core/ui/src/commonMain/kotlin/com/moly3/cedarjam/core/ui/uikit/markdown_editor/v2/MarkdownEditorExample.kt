package com.moly3.cedarjam.core.ui.uikit.markdown_editor.v2

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.moly3.cedarjam.core.domain.features.mdprops.DocumentProperty
import com.moly3.cedarjam.core.domain.features.mdprops.MarkdownDocument
import com.moly3.cedarjam.core.domain.features.mdprops.MarkdownRow
import com.moly3.cedarjam.core.domain.features.mdprops.PropertyType
import com.moly3.cedarjam.core.domain.features.mdprops.RowType

/**
 * Minimal example of hosting [MarkdownEditor].
 *
 * The editor is fully state-hoisted: you pass in a [com.moly3.cedarjam.core.domain.features.mdprops.MarkdownDocument] and receive a
 * new one through `onDocumentChange` whenever anything changes — title, properties,
 * row text, row type, row added/removed. Persisting it (DB, file, network) is up to
 * the caller; the editor deliberately does none of that.
 */
@Composable
fun MarkdownEditorExample() {
    // The single source of truth. Swap `remember` for your ViewModel / store.
    var document by remember {
        mutableStateOf(
            MarkdownDocument(
                title = "Project notes",
                properties = listOf(
                    DocumentProperty(
                        name = "status",
                        type = PropertyType.Text,
                        values = listOf("draft")
                    ),
                    DocumentProperty(
                        name = "tags",
                        type = PropertyType.List,
                        values = listOf("kmp", "compose")
                    ),
                    DocumentProperty(
                        name = "pinned",
                        type = PropertyType.Checkbox,
                        values = listOf("true")
                    ),
                ),
                rows = listOf(
                    MarkdownRow(type = RowType.Heading1, text = "Getting started"),
                    MarkdownRow(type = RowType.Paragraph, text = "Type \"/\" to insert a block."),
                    MarkdownRow(
                        type = RowType.Code,
                        codeLanguage = "kotlin",
                        text = "fun main() {\n    println(\"Shift+Enter for new lines\")\n}",
                    ),
                    MarkdownRow(type = RowType.BulletList, text = "Enter continues the list"),
                    MarkdownRow(type = RowType.Paragraph, text = ""),
                ),
            ),
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        MarkdownEditor(
            document = document,
            onDocumentChange = { updated ->
                // This fires on every edit. Persist / sync here as needed.
                document = updated
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}