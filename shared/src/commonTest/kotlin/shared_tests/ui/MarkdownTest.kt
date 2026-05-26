package shared_tests.ui

import com.moly3.cedarjam.core.domain.features.mdprops.MarkdownDecoder
import com.moly3.cedarjam.core.domain.features.mdprops.MarkdownEncoder
import com.moly3.cedarjam.core.domain.func.formatFileSize
import com.moly3.cedarjam.core.storage.func.createSystemFilesManager
import com.moly3.cedarjam.shared.func.convertToWebp
import com.moly3.cedarjam.ui.Res
import com.moly3.cedarjam.ui.create_new_workspace
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import kotlin.test.Test

class MarkdownTest {



    @Test
    fun test() {
        val expectedMarkdownText = """
            ---
            title: "Project: Cedar — Q3 Review"
            aliases:
              - cedar-q3
              - Q3 Review
            status: in-progress
            published: false
            estimate: 1200
            tags:
              - review
              - quarterly
              - needs-follow-up
              - finance/budget
            description: A multi-word unquoted scalar that should stay text
            ---
            
            # Project: Cedar — Q3 Review
            
            ## hahaha
        """.trimIndent()

        val document = MarkdownDecoder.decode(expectedMarkdownText)
        val actualMarkdownText = MarkdownEncoder.encode(document)

        expectedMarkdownText.trim() shouldBe actualMarkdownText.trim()
    }
}