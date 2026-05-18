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
import com.moly3.cedarjam.core.domain.features.mdprops.DocumentHistory
import com.moly3.cedarjam.core.domain.features.mdprops.DocumentProperty
import com.moly3.cedarjam.core.domain.features.mdprops.MarkdownDecoder
import com.moly3.cedarjam.core.domain.features.mdprops.MarkdownDocument
import com.moly3.cedarjam.core.domain.features.mdprops.MarkdownEncoder
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
    val text = """
            ---
            title: "Project: Cedar — Q3 Review"
            aliases:
              - cedar-q3
              - Q3 Review
            status: in-progress
            published: false
            priority: 1
            progress: 0.75
            budget: 12500
            estimate: 1200
            ratio: -0.5
            zip: "01970"
            phone: 555-0173
            version: 2
            empty_field:
            nothing:
            tilde_null:
            created: 2024-07-01
            deadline: 2024-09-30 17:00:00
            last_edited: 2024-08-15T09:30
            tags:
              - review
              - quarterly
              - needs-follow-up
              - finance/budget
            contributors:
              - "name: Ada Lovelace, role: lead"
              - "name: Alan Turing, role: reviewer"
            scores:
              - "9"
              - "8.5"
              - "7"
              - "10"
            flags:
              - "true"
              - "false"
              - "true"
            mixed:
              - "42"
              - hello
              - "true"
              - ""
              - quoted, comma
            nested_meta: "source: import, confidence: high, reviewed: true"
            url: https://example.com/page#section
            hashtag_in_quotes: "this # is not a comment"
            colon_value: ratio is 3:1 today
            weird key with spaces: kept verbatim
            "quoted:key": also kept
            empty_list: []
            empty_map:
            flow_map: "x: 1, y: 2, label: origin"
            description: A multi-word unquoted scalar that should stay text
            ---
            
            # Project: Cedar — Q3 Review
            
            This is the **opening paragraph** with _emphasis_, `inline code`, and a [link](https://example.com). It also has a trailing sentence to make it multi-line in spirit.
            
            ## Goals for the quarter
            
            Some context before the list.
            
            - Ship the parser rewrite
            - Reduce p99 latency below 200ms
            - Onboard two new contributors
            - A bullet with a `:` colon and a #hashtag inside it
            
            ### Sub-goals (H3)
            
            1. Audit existing YAML edge cases
            2. Write the round-trip test suite
            3. Document the codec API
            
            > A blockquote line.
            
            > It continues on a second line.
            
            >
            
            A standalone paragraph between a quote and a code block.
            
            ```kotlin
            fun parse(input: String): Result {
                val trimmed = input.trim()
                return Result(trimmed, ok = trimmed.isNotEmpty())
            }
            ```
            
            ```
            plain fenced block with no language
            indentation    is preserved
            ```
            
            Here is a snippet that itself contains a fence:
            
            ````markdown
            ```bash
            echo "nested fence"
            ```
            ````
            
            ### Deeper heading (H4 — collapses to H3)
            
            ### Even deeper (H5)
            
            ![](https://example.com/assets/arch.png)
            
            A divider follows this line.
            
            ---
            
            ---
            
            ---
            
            Final paragraph after three different divider styles.
        """.trimIndent()
    val document2 = MarkdownDecoder.decode(text)
    var document by remember {
//        mutableStateOf(
//            MarkdownDocument(
//                title = "Project notes",
//                properties = listOf(
//                    DocumentProperty(
//                        name = "status",
//                        type = PropertyType.Text,
//                        values = listOf("draft")
//                    ),
//                    DocumentProperty(
//                        name = "tags",
//                        type = PropertyType.List,
//                        values = listOf("kmp", "compose")
//                    ),
//                    DocumentProperty(
//                        name = "pinned",
//                        type = PropertyType.Checkbox,
//                        values = listOf("true")
//                    ),
//                ),
//                rows = listOf(
//                    MarkdownRow(type = RowType.Heading1, text = "Getting started"),
//                    MarkdownRow(type = RowType.Paragraph, text = "Type \"/\" to insert a block."),
//                    MarkdownRow(
//                        type = RowType.Code,
//                        codeLanguage = "kotlin",
//                        text = "fun main() {\n    println(\"Shift+Enter for new lines\")\n}",
//                    ),
//                    MarkdownRow(type = RowType.BulletList, text = "Enter continues the list"),
//                    MarkdownRow(type = RowType.Paragraph, text = ""),
//                ),
//            ),
//        )
        mutableStateOf(document2)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        val history = remember { DocumentHistory(document) }
        var document by remember { mutableStateOf(history.current) }
        MarkdownEditor(
            document = document,
            onDocumentChange = { updated ->
                history.commitCoalescing(updated)   // or commit() for structural edits
                document = updated
            },
            modifier = Modifier.fillMaxSize(),
            history = history
        )
    }
}