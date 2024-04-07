package info.skyblond.ctc

import org.bouncycastle.crypto.agreement.X25519Agreement
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
    object {}.javaClass.getResourceAsStream("/ctc.txt")!!.use {
        it.reader().readLines()
    }.associate {
        it.take(4).toInt() to it.drop(4)
    }

/**
 * Print in CTC code format: `XXXX, XXXX, XXXX`
 * */
fun List<Int>.toCTCString(): String =
    this.joinToString { "%04d".format(it) }

/**
 * Compress the [input] using max level ([Deflater.BEST_COMPRESSION]).
 * */
fun compress(input: ByteArray): ByteArray {
    val buffer = ByteArray(1024)
    val baos = ByteArrayOutputStream()
    val deflater = Deflater(Deflater.BEST_COMPRESSION)
    deflater.setInput(input)
    deflater.finish()
    while (!deflater.finished()) {
        val n: Int = deflater.deflate(buffer)
        baos.write(buffer, 0, n)
    }
    deflater.end()
    return baos.toByteArray()
}

/**
 * Decompress the [input].
 * */
fun decompress(input: ByteArray): ByteArray {
    val buffer = ByteArray(1024)
    val baos = ByteArrayOutputStream()
    val inflater = Inflater()
    inflater.setInput(input)
    while (!inflater.finished()) {
        val n: Int = inflater.inflate(buffer)
        baos.write(buffer, 0, n)
    }
    inflater.end()
    return baos.toByteArray()
}

/**
 * Calculate the SHA3-256 hash of the [ByteArray].
 * */
fun ByteArray.sha3(): ByteArray {
    val digest = MessageDigest.getInstance("SHA3-256")
    return digest.digest(this)
}

/**
 * Return an unbounded iterator which spills out random int.
 * Using [VMPCRandomGenerator].
 * */
fun getRandomIntIter(seed: ByteArray) = sequence {
    val vmpc = VMPCRandomGenerator()
    vmpc.addSeedMaterial(seed)
    val intBuffer = ByteArray(Int.SIZE_BYTES)
    while (true) {
        vmpc.nextBytes(intBuffer)
        yield(Pack.bigEndianToInt(intBuffer, 0))
    }
}.iterator()

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

/**
 * Give a randomized index list of [this] list.
 * Use [prng] as a random source and do it [n] times.
 * */
fun <T> List<T>.randomIndexList(prng: Iterator<Int>, n: Int): List<Int> {
    require(n >= 1)
    var availableIndex = this.indices.toMutableList()
    repeat(n) {
        val indexList = buildList {
            while (availableIndex.isNotEmpty())
                add(availableIndex.removeAt(prng.next().absoluteValue % availableIndex.size))
        }
        availableIndex = indexList.toMutableList()
    }
    return availableIndex
}

/**
 * Randomize a list using [prng] as a random source and do it [n] times.
 * */
fun <T> List<T>.randomize(prng: Iterator<Int>, n: Int): List<T> {
    val indexList = this.randomIndexList(prng, n)
    return indexList.map { this[it] }
}

/**
 * The reverse process of [randomize].
 * */
fun <T> List<T>.derandomize(prng: Iterator<Int>, n: Int): List<T> {
    val indexList = this.randomIndexList(prng, n)
    return indexList.mapIndexed { index, it -> it to this[index] }
        .sortedBy { it.first }
        .map { it.second }
}
