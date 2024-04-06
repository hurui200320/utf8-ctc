package info.skyblond.ctc

import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater

fun loadCTC(): Map<Int, String> =
    object {}.javaClass.getResourceAsStream("/ctc.txt")!!.use {
        it.reader().readLines()
    }.associate {
        it.take(4).toInt() to it.drop(4)
    }

fun List<Int>.toCTCString(): String =
    this.joinToString { "%04d".format(it) }

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
    return baos.toByteArray()
}

fun decompress(input: ByteArray): ByteArray {
    val buffer = ByteArray(1024)
    val baos = ByteArrayOutputStream()
    val inflater = Inflater()
    inflater.setInput(input)
    while (!inflater.finished()) {
        val n: Int = inflater.inflate(buffer)
        baos.write(buffer, 0, n)
    }
    return baos.toByteArray()
}
