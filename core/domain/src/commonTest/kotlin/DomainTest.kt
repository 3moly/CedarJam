package core.domain

import kotlin.test.Test
import kotlin.test.assertTrue

class DomainTest {

    @Test
    fun test() {
        val name = "Kotlin в действии, 2-е изд..pdf"
        val bytes = name.encodeToByteArray()
        val nameDecoded = bytes.decodeToString()
        assertTrue {
            name == nameDecoded
        }
    }
}