package info.skyblond.ctc

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.random.Random

class EncryptionTest {

    @Test
    fun `test chacha20 key stream`() {
        val key = Random.nextBytes(32)
        val nonce = Random.nextBytes(12)
        createChacha20CodeSeq(key, nonce)
            .take(10000)
            .forEach { assertTrue(it in 0..9999) }
    }

    @Test
    fun `test encrypt and decrypt`() {
        val key = Random.nextBytes(32)
        val nonce = Random.nextBytes(12)

        val data = (0..9999).toList()

        val encrypted = data.asSequence().encrypt(createChacha20CodeSeq(key, nonce)).toList()
        println(encrypted)
        val decrypted = encrypted.asSequence().decrypt(createChacha20CodeSeq(key, nonce)).toList()

        assertTrue(decrypted.toList() == data.toList())
    }

    @Test
    fun `test generate nonce`() {
        repeat(100) {
            val nonce = generateNonce()
            assertTrue(nonce.size == 12)
        }
    }
}