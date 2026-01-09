package info.skyblond.ctc

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import kotlin.random.Random

class CompressionTest {
    @Test
    fun testCompression(){
        repeat(10) {
            val data = Random.nextBytes(4095)
            val compressed = compress(data)
            println(compressed.size)
            val decompressed = decompress(compressed)
            assertArrayEquals(data, decompressed)
        }
    }
}
