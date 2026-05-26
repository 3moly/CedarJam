import com.moly3.cedarjam.core.domain.features.search.ItemType
import com.moly3.cedarjam.core.domain.features.search.SearchEngine
import com.moly3.cedarjam.core.domain.features.search.SearchSyntaxException
import com.moly3.cedarjam.core.domain.features.search.Searchable
import com.moly3.cedarjam.core.domain.features.search.Task
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SearchEngineTest {

    private val engine = SearchEngine()

    private val note1 = Searchable(
        type = ItemType.FILE,
        fileName = "meeting-notes.md",
        path = "Work/meeting-notes.md",
        content = "Discussed the project roadmap.\nNext meeting on Friday.",
        tags = setOf("work", "work/urgent"),
        properties = mapOf("status" to "Draft", "duration" to 3, "aliases" to null),
        tasks = listOf(Task("call Bob", done = false), Task("email Sue", done = true)),
    )
    private val note2 = Searchable(
        type = ItemType.FILE,
        fileName = "personal.md",
        path = "Personal/personal.md",
        content = "A personal meetup with friends.",
        tags = setOf("personal"),
        properties = mapOf("status" to "Published", "duration" to 8),
    )
    private val note3 = Searchable(
        type = ItemType.FILE,
        fileName = "personal staff.md",
        path = "Personal/personal.md",
        content = "A personal meetup with friends.",
        tags = setOf("personal"),
        properties = mapOf("status" to "Published", "duration" to 8),
    )
    private val dir1 = Searchable(ItemType.DIRECTORY, "Work", "Work", "")
    private val tag1 = Searchable(ItemType.TAG, "work", "#work", "")
    private val coll1 =
        Searchable(ItemType.COLLECTION, "Reading List", "collections/reading", "books to read")
    private val row1 = Searchable(ItemType.ROW, "Row 42", "table/row-42", "project milestone")
    private val ann1 = Searchable(
        ItemType.ANNOTATION, "highlight-1", "annotations/h1",
        "important project insight", tags = setOf("work"),
    )
    private val vault = listOf(note1, note2, dir1, tag1, coll1, row1, ann1)

    private fun match(q: String, item: Searchable) = engine.matches(engine.compile(q), item)

    // ---- boolean logic ----

    @Test fun booleanAnd() {
        assertTrue(match("project roadmap", note1))
        assertFalse(match("project banana", note1))
    }

    @Test fun booleanOr() = assertTrue(match("banana OR roadmap", note1))

    @Test fun negation() {
        assertTrue(match("project -banana", note1))
        assertFalse(match("project -roadmap", note1))
    }

    @Test fun grouping() {
        assertTrue(match("meeting (roadmap OR meetup) -banana", note1))
        assertTrue(match("meetup (work OR friends) -banana", note2))
    }

    @Test fun phrases() {
        assertTrue(match("\"project roadmap\"", note1))
        assertFalse(match("\"roadmap project\"", note1))
    }

    @Test fun emptyQueryMatchesAll() = assertTrue(match("", note1))

    // ---- operators ----

    @Test fun fileOperator() {
        assertTrue(match("file:meeting", note1))
        assertFalse(match("file:personal", note1))
        assertTrue(match("file:meeting-notes", note1)) // hyphen is literal here
        assertTrue(match("file:\"personal staff\"", note3)) // hyphen is literal here
    }

    @Test fun pathOperator() = assertTrue(match("path:Work", note1))

    @Test fun contentOperator() = assertTrue(match("content:Friday", note1))

    @Test fun tagOperator() {
        assertTrue(match("tag:#work", note1))
        assertTrue(match("tag:work", note1))            // leading # optional
        assertFalse(match("tag:personal", note1))
        assertFalse(match("tag:urgent", note1))         // nested leaf not matched
    }

    @Test fun lineOperator() {
        assertTrue(match("line:(meeting Friday)", note1))
        assertFalse(match("line:(roadmap Friday)", note1)) // different lines
    }

    @Test fun taskOperators() {
        assertTrue(match("task:call", note1))
        assertTrue(match("task-todo:call", note1))
        assertFalse(match("task-todo:email", note1))   // email task is done
        assertTrue(match("task-done:email", note1))
        assertTrue(match("task:(call OR email)", note1))
    }

    @Test fun caseSensitivity() {
        assertTrue(match("PROJECT", note1))             // default: ignore case
        assertTrue(match("match-case:project", note1))
        assertFalse(match("match-case:PROJECT", note1))
    }

    // ---- properties ----

    @Test fun propertyExistence() {
        assertTrue(match("[status]", note1))
        assertFalse(match("[missing]", note1))
    }

    @Test fun propertyValue() {
        assertTrue(match("[status:Draft]", note1))
        assertFalse(match("[status:Published]", note1))
        assertTrue(match("[status:Draft OR Published]", note1))
        assertTrue(match("[status:Draft OR Published]", note2))
    }

    @Test fun propertyNull() = assertTrue(match("[aliases:null]", note1))

    @Test fun propertyComparison() {
        assertTrue(match("[duration:<5]", note1))
        assertFalse(match("[duration:<5]", note2))
        assertTrue(match("[duration:>5]", note2))
        assertTrue(match("[duration:>=8]", note2))
    }

    // ---- regex ----

    @Test fun regex() {
        assertTrue(match("/Frid?ay/", note1))
        assertTrue(match("path:/Work/", note1))
    }

    // ---- the type: operator ----

    @Test fun typeSingle() {
        assertTrue(match("type:file", note1))
        assertFalse(match("type:file", dir1))
        assertTrue(match("type:directory", dir1))
        assertTrue(match("type:tag", tag1))
        assertTrue(match("type:collection", coll1))
        assertTrue(match("type:row", row1))
        assertTrue(match("type:annotation", ann1))
    }

    @Test fun typeGroup() {
        assertTrue(match("type:(file OR directory)", note1))
        assertTrue(match("type:(file OR directory)", dir1))
        assertFalse(match("type:(file OR directory)", tag1))
    }

    @Test fun typeCombinedWithClauses() {
        assertTrue(match("type:annotation project", ann1))
        assertFalse(match("type:annotation banana", ann1))
        assertTrue(match("type:annotation tag:work", ann1))
        assertTrue(match("project -type:directory", note1))
    }

    @Test fun typeInSearchFilter() {
        val hits = engine.search("type:(file OR annotation) project", vault)
        assertEquals(setOf("meeting-notes.md", "highlight-1"), hits.map { it.fileName }.toSet())
    }

    @Test fun typeRejectsInvalidValue() {
        assertFailsWith<SearchSyntaxException> { engine.compile("type:nonsense") }
        assertFailsWith<SearchSyntaxException> { engine.compile("type:(file OR bogus)") }
    }

    // ---- syntax errors ----

    @Test fun syntaxErrors() {
        for (bad in listOf("\"unterminated", "/unterminated", "a OR", "(a", "a)", "[unclosed")) {
            assertFailsWith<SearchSyntaxException>("should reject: $bad") {
                engine.compile(bad)
            }
        }
    }

    // ---- explain ----

    @Test fun explain() {
        assertEquals(
            "item type is one of {file, directory} AND the word 'project'",
            engine.explain("type:(file OR directory) project"),
        )
    }

    @Test fun explainBanana() {
        assertEquals(
            "NOT the word 'banana'",
            engine.explain("-banana"),
        )
    }
}