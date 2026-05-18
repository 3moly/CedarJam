import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import com.moly3.cedarjam.core.domain.features.mdprops.*

class ScalarParserTest {

    @Test
    fun parsesPlainText() {
        assertEquals(PropertyValue.Text("Hello World"), ScalarParser.parse("Hello World"))
    }

    @Test
    fun trimsSurroundingWhitespace() {
        assertEquals(PropertyValue.Text("trimmed"), ScalarParser.parse("   trimmed   "))
    }

    @Test
    fun parsesIntegersAsNumber() {
        val v = ScalarParser.parse("42")
        assertIs<PropertyValue.Number>(v)
        assertEquals(42.0, v.value)
        assertEquals(42L, v.intValue)
    }

    @Test
    fun parsesNegativeAndFloatNumbers() {
        assertEquals(PropertyValue.Number(-7.0), ScalarParser.parse("-7"))
        assertEquals(PropertyValue.Number(3.14), ScalarParser.parse("3.14"))
        assertEquals(PropertyValue.Number(1500.0), ScalarParser.parse("1_500"))
        assertEquals(PropertyValue.Number(2.5e3), ScalarParser.parse("2.5e3"))
    }

    @Test
    fun intValueIsNullForFractionalNumbers() {
        val v = ScalarParser.parse("3.5") as PropertyValue.Number
        assertNull(v.intValue)
    }

    @Test
    fun leadingZeroNumbersStayText() {
        // ZIP codes / IDs must not be coerced.
        assertEquals(PropertyValue.Text("007"), ScalarParser.parse("007"))
        assertEquals(PropertyValue.Text("01234"), ScalarParser.parse("01234"))
    }

    @Test
    fun parsesBooleans() {
        for (t in listOf("true", "True", "TRUE", "yes", "Yes")) {
            assertEquals(PropertyValue.Bool(true), ScalarParser.parse(t), "failed on $t")
        }
        for (f in listOf("false", "False", "FALSE", "no", "No")) {
            assertEquals(PropertyValue.Bool(false), ScalarParser.parse(f), "failed on $f")
        }
    }

    @Test
    fun parsesNullTokens() {
        for (n in listOf("", "~", "null", "Null", "NULL")) {
            assertEquals(PropertyValue.Null, ScalarParser.parse(n), "failed on '$n'")
        }
    }

    @Test
    fun quotedNumbersStayText() {
        assertEquals(PropertyValue.Text("42"), ScalarParser.parse("\"42\""))
        assertEquals(PropertyValue.Text("true"), ScalarParser.parse("'true'"))
    }

    @Test
    fun doubleQuotedEscapesResolved() {
        assertEquals(PropertyValue.Text("line1\nline2"), ScalarParser.parse("\"line1\\nline2\""))
        assertEquals(PropertyValue.Text("a\"b"), ScalarParser.parse("\"a\\\"b\""))
    }

    @Test
    fun singleQuotedDoublesAreLiteralQuote() {
        assertEquals(PropertyValue.Text("it's here"), ScalarParser.parse("'it''s here'"))
    }

    @Test
    fun wordsThatStartLikeNumbersStayText() {
        assertEquals(PropertyValue.Text("3 apples"), ScalarParser.parse("3 apples"))
        assertEquals(PropertyValue.Text("12px"), ScalarParser.parse("12px"))
    }

    @Test
    fun nanIsText() {
        assertEquals(PropertyValue.Text("nan"), ScalarParser.parse("nan"))
    }
}