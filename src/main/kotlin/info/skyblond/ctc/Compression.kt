package info.skyblond.ctc

import org.tukaani.xz.FinishableWrapperOutputStream
import org.tukaani.xz.LZMA2Options
import java.io.ByteArrayOutputStream

private val lzma2options = LZMA2Options().apply {
    // best compression
    setPreset(LZMA2Options.PRESET_MAX)
    // override preset to use 256KB dict
    dictSize = 256 * 1024
}

/**
 * Compress the [input].
 * */
fun compress(input: ByteArray): ByteArray {
    val baos = ByteArrayOutputStream()
    val wrapper = FinishableWrapperOutputStream(baos)

    val lzma = lzma2options.getOutputStream(wrapper)

    lzma.write(input)
    lzma.finish()
    lzma.close()

    return baos.toByteArray()
}

/**
 * Decompress the [input].
 * */
fun decompress(input: ByteArray): ByteArray {
    val lzma = lzma2options.getInputStream(input.inputStream())
    return lzma.readAllBytes()
}