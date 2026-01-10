package info.skyblond.ctc

import org.bouncycastle.crypto.agreement.X25519Agreement
import org.bouncycastle.crypto.engines.ChaCha7539Engine
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import org.bouncycastle.crypto.prng.VMPCRandomGenerator
import org.bouncycastle.util.Pack
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.zip.Deflater
import java.util.zip.Inflater
import kotlin.math.absoluteValue

/**
 * Load the CTC lookup table from resources.
 *
 * Return: Code to Char.
 * */
fun loadCTC(): Map<Int, String> =
    object {}.javaClass.getResourceAsStream("/ctc.txt")!!.use { ins ->
        ins.reader().readLines().filterNot { it.isBlank() || it.startsWith("#") }
    }.associate {
        it.take(4).toInt() to it.drop(4)
    }

/**
 * Print in CTC code format: `XXXX  XXXX  XXXX`
 * */
fun List<Int>.toCTCString(): String =
    this.joinToString("  ") { "%04d".format(it) }

/**
 * Calculate the SHA3-256 hash of the [ByteArray].
 * */
fun ByteArray.sha3(): ByteArray {
    val digest = MessageDigest.getInstance("SHA3-256")
    return digest.digest(this)
}

/**
 * Do x25519 ECDH.
 * The [selfPrivateKeyBytes] and [remotePublicKeyBytes] must be 32 bytes (256 bits).
 * */
fun x25519(selfPrivateKeyBytes: ByteArray, remotePublicKeyBytes: ByteArray): ByteArray {
    val agreement = X25519Agreement()
    val result = ByteArray(agreement.agreementSize)
    agreement.init(X25519PrivateKeyParameters(selfPrivateKeyBytes))
    agreement.calculateAgreement(X25519PublicKeyParameters(remotePublicKeyBytes), result, 0)
    return result
}

/**
 * Convert [ByteArray] to hex string.
 * */
fun ByteArray.toHexString(): String =
    joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }.uppercase()

/**
 * Decode [ByteArray] from a hex string.
 * */
fun String.decodeHex(): ByteArray {
    require(length % 2 == 0) { "Must have an even length" }
    return chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}
