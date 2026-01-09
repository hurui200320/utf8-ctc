package info.skyblond.ctc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChecksumTest {
    @Test
    fun `test checksum`() {
        val code = "1234 5678 9012"
        val checksum = generateISO7064(code)
        println(checksum)
        assertEquals("44", checksum)
        assertTrue { verifyISO7064(code + checksum) }
        assertFalse { verifyISO7064(code + "00") }
    }
}