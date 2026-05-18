package features.markdown

import com.moly3.cedarjam.core.domain.features.mdprops.MarkdownDecoder
import com.moly3.cedarjam.core.domain.features.mdprops.MarkdownEncoder
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.test.assertEquals

class FileParser {

    @Test
    fun test() {
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
        val text2 = """
            ---
            title: "Project: Cedar — Q3 Review"
            aliases:
              - cedar-q3
              - Q3 Review
            status: in-progress
            published: false
            priority: 1
            progress: 0.75
            budget: 12_500
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
              - name: Ada Lovelace
                role: lead
              - name: Alan Turing
                role: reviewer
            scores:
              - 9
              - 8.5
              - 7
              - 10
            flags:
              - true
              - false
              - true
            mixed:
              - 42
              - hello
              - true
              - 
              - quoted, comma
            nested_meta:
              source: import
              confidence: high
              reviewed: true
            url: https://example.com/page#section
            hashtag_in_quotes: "this # is not a comment"
            colon_value: ratio is 3:1 today
            weird key with spaces: kept verbatim
            quoted:key: also kept
            empty_list: []
            empty_map: {}
            flow_map:
              x: 1
              y: 2
              label: origin
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

            #### Deeper heading (H4 — collapses to H3)

            ##### Even deeper (H5)

            ![Architecture diagram](https://example.com/assets/arch.png)

            A divider follows this line.

            ---

            ***

            ___

            Final paragraph after three different divider styles.

        """.trimIndent()
        val document = MarkdownDecoder.decode(text)
        val saveText = MarkdownEncoder.encode(document)
        val document2 = MarkdownDecoder.decode(text2)
        val saveText2 = MarkdownEncoder.encode(document2)
        assertEquals(document.title, document2.title)
        assertEquals(document.rows.size, document2.rows.size)
        assertEquals(document.properties.size, document2.properties.size)
        for(i in 0 until document.properties.size){
            val property = document.properties[i]
            val property2 = document2.properties[i]
            property.type shouldBe property2.type
            property.name shouldBe property2.name
            property.values shouldBe property2.values
        }
        for(i in 0 until document.rows.size){
            val property = document.rows[i]
            val property2 = document2.rows[i]
            property.type shouldBe property2.type
            property.text shouldBe property2.text
            property.codeLanguage shouldBe property2.codeLanguage
        }
        assertEquals(text.trimEnd(), saveText.trimEnd())
        assertEquals(text2.trimEnd(), saveText2.trimEnd())
    }
}