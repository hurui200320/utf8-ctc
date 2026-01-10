package info.skyblond.ctc

import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.engines.ChaCha7539Engine
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.macs.SipHash
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import org.bouncycastle.util.Pack
import java.security.SecureRandom

/**
 * Create a chacha20 key stream in the 0000-9999 range.
 *
 * @param key The encryption key, which must be 32 bytes (256 bits).
 * @param nonce The nonce, which must be 12 bytes (96 bits).
 * */
fun createChacha20CodeSeq(
    key: ByteArray, nonce: ByteArray
): Sequence<Int> {
    require(key.size == 32) { "Key must be 32 bytes (256 bits)" }
    require(nonce.size == 12) { "Nonce must be 12 bytes (96 bits)" }

    val engine = ChaCha7539Engine()
    engine.init(true, ParametersWithIV(KeyParameter(key), nonce))

    return sequence {
        // use zero as input so 0 xor key = key, we can get key stream
        val zeroInput = ByteArray(64)
        val output = ByteArray(64)
        while (true) {
            engine.processBytes(zeroInput, 0, zeroInput.size, output, 0)
            for (i in 0 until 64 step 2) {
                // step 2 so 2 bytes at a time, we get a short (0 - 65535)
                // big endian, doesn't matter
                val raw = (output[i].toInt() and 0xff) shl 8 or (output[i + 1].toInt() and 0xff)
                // reject if raw >= 60000
                if (raw < 60000) {
                    yield(raw % 10000)
                }
            }
        }
    }
}

/**
 * Encrypt with the given key stream.
 * */
fun Sequence<Int>.encrypt(keyStream: Sequence<Int>): Sequence<Int> =
    this.zip(keyStream)
        .map { (code, key) -> (code + key) % 10000 }

/**
 * Decrypt with the given key stream.
 * */
fun Sequence<Int>.decrypt(keyStream: Sequence<Int>): Sequence<Int> =
    this.zip(keyStream)
        .map { (enc, key) -> (enc - key + 10000) % 10000 }

/**
 * Generate nonce.
 *
 * First 8 bytes (64bit) is current time in milliseconds.
 * The second 4 bytes (32bit) are random.
 * */
fun generateNonce(): ByteArray {
    val p1 = Pack.longToBigEndian(System.currentTimeMillis())
    val p2 = Pack.intToBigEndian(SecureRandom().nextInt())

    return p1 + p2
}

/**
 * Generate a 128bit key from the given 32-byte key for SipHash.
 * */
fun ByteArray.hkdfSipHashKey(): ByteArray {
    require(this.size == 32) { "Key must be 32 bytes (256 bits)" }
    val hkdf = HKDFBytesGenerator(SHA256Digest())
    val params = HKDFParameters(this, null, "SipHash".encodeToByteArray())
    hkdf.init(params)
    val output = ByteArray(16)
    hkdf.generateBytes(output, 0, 16)
    return output
}

/**
 * Calculate SipHash MAC for the give list of CTC code.
 * */
fun List<Int>.sipHash(key: ByteArray): ByteArray {
    require(key.size == 16) { "Key must be 16 bytes (128 bits)" }
    val mac = SipHash()
    mac.init(KeyParameter(key))

    for (i in this) {
        mac.update(Pack.shortToBigEndian( i.toShort()), 0, 2)
    }
    return Pack.longToBigEndian(mac.doFinal())
}