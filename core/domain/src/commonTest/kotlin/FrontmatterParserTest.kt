import com.moly3.cedarjam.core.domain.features.mdprops.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class FrontmatterParserTest {

    // --- basic structure -------------------------------------------------

    @Test
    fun noFrontmatterReturnsWholeBody() {
        val src = "# Just a heading\n\nSome text."
        val doc = FrontmatterParser.parse(src)
        assertTrue(doc.properties.isEmpty())
        assertEquals(src, doc.body)
        assertEquals(false, doc.hasFrontmatter)
    }

    @Test
    fun parsesSimpleFrontmatter() {
        val src = """
            ---
            title: My Note
            count: 5
            published: true
            ---
            Body content here.
        """.trimIndent()
        val doc = FrontmatterParser.parse(src)
        assertEquals("My Note", doc.text("title"))
        assertEquals(5.0, doc.number("count"))
        assertEquals(true, doc.bool("published"))
        assertEquals("Body content here.", doc.body)
        assertTrue(doc.hasFrontmatter)
    }

    @Test
    fun emptyFrontmatterBlockIsRecognized() {
        val src = "---\n---\nbody"
        val doc = FrontmatterParser.parse(src)
        assertTrue(doc.properties.isEmpty())
        assertEquals("body", doc.body)
        assertTrue(doc.hasFrontmatter) // block existed even though empty
    }

    @Test
    fun unclosedFrontmatterThrows() {
        val src = "---\ntitle: Broken\nno closing delimiter"
        assertFailsWith<FrontmatterParser.ParseException> {
            FrontmatterParser.parse(src)
        }
    }

    @Test
    fun delimiterMustBeOnFirstLine() {
        val src = "\n---\ntitle: Nope\n---\nbody"
        val doc = FrontmatterParser.parse(src)
        // Leading blank line => not treated as frontmatter.
        assertTrue(doc.properties.isEmpty())
        assertEquals(src, doc.body)
    }

    @Test
    fun handlesCrlfLineEndings() {
        val src = "---\r\ntitle: Windows\r\n---\r\nbody line"
        val doc = FrontmatterParser.parse(src)
        assertEquals("Windows", doc.text("title"))
        assertEquals("body line", doc.body)
    }

    @Test
    fun emptyBodyAfterFrontmatter() {
        val src = "---\ntitle: Only meta\n---"
        val doc = FrontmatterParser.parse(src)
        assertEquals("Only meta", doc.text("title"))
        assertEquals("", doc.body)
    }

    @Test
    fun bodyPreservesInternalBlankLinesAndMarkdown() {
        val src = "---\nk: v\n---\n# Title\n\nPara one.\n\n- a\n- b\n"
        val doc = FrontmatterParser.parse(src)
        assertEquals("# Title\n\nPara one.\n\n- a\n- b\n", doc.body)
    }

    // --- comments & blanks ----------------------------------------------

    @Test
    fun ignoresCommentsAndBlankLines() {
        val src = """
            ---
            # this is a comment
            title: Has Comments

            author: Jane  # trailing comment
            ---
            body
        """.trimIndent()
        val doc = FrontmatterParser.parse(src)
        assertEquals("Has Comments", doc.text("title"))
        assertEquals("Jane", doc.text("author"))
    }

    @Test
    fun hashInsideQuotesIsNotComment() {
        val src = "---\nurl: \"https://example.com/page#section\"\n---\nb"
        val doc = FrontmatterParser.parse(src)
        assertEquals("https://example.com/page#section", doc.text("url"))
    }

    // --- lists -----------------------------------------------------------

    @Test
    fun parsesFlowList() {
        val src = "---\ntags: [kotlin, multiplatform, parsing]\n---\nb"
        val doc = FrontmatterParser.parse(src)
        assertEquals(listOf("kotlin", "multiplatform", "parsing"), doc.stringList("tags"))
    }

    @Test
    fun parsesBlockList() {
        val src = """
            ---
            tags:
              - alpha
              - beta
              - gamma
            ---
            b
        """.trimIndent()
        val doc = FrontmatterParser.parse(src)
        assertEquals(listOf("alpha", "beta", "gamma"), doc.stringList("tags"))
    }

    @Test
    fun emptyFlowListIsEmpty() {
        val src = "---\ntags: []\n---\nb"
        val doc = FrontmatterParser.parse(src)
        val v = doc.properties["tags"]
        assertIs<PropertyValue.ListValue>(v)
        assertTrue(v.items.isEmpty())
    }

    @Test
    fun listWithMixedTypesKeepsTypes() {
        val src = "---\nmixed: [1, two, true, null]\n---\nb"
        val doc = FrontmatterParser.parse(src)
        val v = doc.properties["mixed"] as PropertyValue.ListValue
        assertEquals(PropertyValue.Number(1.0), v.items[0])
        assertEquals(PropertyValue.Text("two"), v.items[1])
        assertEquals(PropertyValue.Bool(true), v.items[2])
        assertEquals(PropertyValue.Null, v.items[3])
    }

    @Test
    fun nestedFlowList() {
        val src = "---\nmatrix: [[1, 2], [3, 4]]\n---\nb"
        val doc = FrontmatterParser.parse(src)
        val outer = doc.properties["matrix"] as PropertyValue.ListValue
        val row0 = outer.items[0] as PropertyValue.ListValue
        assertEquals(PropertyValue.Number(2.0), row0.items[1])
    }

    @Test
    fun blockListWithCommasInsideQuotes() {
        val src = "---\nnames: [\"Doe, John\", \"Roe, Jane\"]\n---\nb"
        val doc = FrontmatterParser.parse(src)
        assertEquals(listOf("Doe, John", "Roe, Jane"), doc.stringList("names"))
    }

    // --- maps ------------------------------------------------------------

    @Test
    fun parsesFlowMap() {
        val src = "---\nauthor: {name: Jane, age: 30}\n---\nb"
        val doc = FrontmatterParser.parse(src)
        val m = doc.properties["author"] as PropertyValue.MapValue
        assertEquals(PropertyValue.Text("Jane"), m.entries["name"])
        assertEquals(PropertyValue.Number(30.0), m.entries["age"])
    }

    @Test
    fun parsesNestedBlockMap() {
        val src = """
            ---
            author:
              name: Jane Doe
              contact:
                email: jane@example.com
                phone: 555-1234
            ---
            b
        """.trimIndent()
        val doc = FrontmatterParser.parse(src)
        val author = doc.properties["author"] as PropertyValue.MapValue
        assertEquals("Jane Doe", (author.entries["name"] as PropertyValue.Text).value)
        val contact = author.entries["contact"] as PropertyValue.MapValue
        assertEquals("jane@example.com", (contact.entries["email"] as PropertyValue.Text).value)
        assertEquals("555-1234", (contact.entries["phone"] as PropertyValue.Text).value)
    }

    @Test
    fun parsesBlockListOfMaps() {
        val src = """
            ---
            people:
              - name: Alice
                role: dev
              - name: Bob
                role: design
            ---
            b
        """.trimIndent()
        val doc = FrontmatterParser.parse(src)
        val people = doc.properties["people"] as PropertyValue.ListValue
        assertEquals(2, people.items.size)
        val first = people.items[0] as PropertyValue.MapValue
        assertEquals("Alice", (first.entries["name"] as PropertyValue.Text).value)
        assertEquals("dev", (first.entries["role"] as PropertyValue.Text).value)
        val second = people.items[1] as PropertyValue.MapValue
        assertEquals("Bob", (second.entries["name"] as PropertyValue.Text).value)
    }

    // --- obsidian-flavored cases ----------------------------------------

    @Test
    fun obsidianTagsAndAliasesExample() {
        val src = """
            ---
            tags:
              - project/active
              - kotlin
            aliases: [KMP Parser, mdprops]
            cssclass: wide
            created: 2024-01-15
            ---
            # Note body
        """.trimIndent()
        val doc = FrontmatterParser.parse(src)
        assertEquals(listOf("project/active", "kotlin"), doc.stringList("tags"))
        assertEquals(listOf("KMP Parser", "mdprops"), doc.stringList("aliases"))
        assertEquals("wide", doc.text("cssclass"))
        // Dates are kept as text for the caller to parse further.
        assertEquals("2024-01-15", doc.text("created"))
    }

    @Test
    fun checkboxPropertyIsBoolean() {
        val src = "---\ncomplete: false\nstarred: true\n---\nb"
        val doc = FrontmatterParser.parse(src)
        assertEquals(false, doc.bool("complete"))
        assertEquals(true, doc.bool("starred"))
    }

    @Test
    fun nullProperty() {
        val src = "---\ncover:\nrating: null\n---\nb"
        val doc = FrontmatterParser.parse(src)
        assertEquals(PropertyValue.Null, doc.properties["cover"])
        assertEquals(PropertyValue.Null, doc.properties["rating"])
    }

    @Test
    fun keysWithSpacesWhenQuoted() {
        val src = "---\n\"my key\": value\n---\nb"
        val doc = FrontmatterParser.parse(src)
        assertEquals("value", doc.text("my key"))
    }

    @Test
    fun valueContainingColon() {
        val src = "---\ntime: \"12:30:00\"\nnote: see this: thing\n---\nb"
        val doc = FrontmatterParser.parse(src)
        assertEquals("12:30:00", doc.text("time"))
        // Unquoted: only the first `: ` splits, rest stays in the value.
        assertEquals("see this: thing", doc.text("note"))
    }

    @Test
    fun preservesKeyInsertionOrder() {
        val src = "---\nz: 1\na: 2\nm: 3\n---\nb"
        val doc = FrontmatterParser.parse(src)
        assertEquals(listOf("z", "a", "m"), doc.properties.keys.toList())
    }

    // --- error handling --------------------------------------------------

    @Test
    fun missingColonThrows() {
        val src = "---\nthis line has no colon\n---\nb"
        assertFailsWith<FrontmatterParser.ParseException> {
            FrontmatterParser.parse(src)
        }
    }

    @Test
    fun unterminatedFlowListThrows() {
        val src = "---\ntags: [a, b, c\n---\nb"
        assertFailsWith<FrontmatterParser.ParseException> {
            FrontmatterParser.parse(src)
        }
    }

    // --- convenience accessors ------------------------------------------

    @Test
    fun stringListCoercesSingleText() {
        val src = "---\ntag: single\n---\nb"
        val doc = FrontmatterParser.parse(src)
        assertEquals(listOf("single"), doc.stringList("tag"))
    }

    @Test
    fun accessorsReturnNullForWrongTypeOrMissing() {
        val src = "---\ntitle: Text\n---\nb"
        val doc = FrontmatterParser.parse(src)
        assertEquals(null, doc.number("title"))   // wrong type
        assertEquals(null, doc.text("missing"))   // absent
        assertEquals(null, doc.bool("title"))     // wrong type
    }

    @Test
    fun parsePropertiesShortcut() {
        val props = FrontmatterParser.parseProperties("---\nk: v\n---\nbody")
        assertEquals(PropertyValue.Text("v"), props["k"])
    }
}