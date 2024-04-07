package info.skyblond.ctc.commands

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import info.skyblond.ctc.decodeHex
import info.skyblond.ctc.sha3
import info.skyblond.ctc.x25519
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*

class CodecOptions: OptionGroup(
    name = "Codec options",
    help = "Options related to encode and decode"
) {
    val file by option("-f")
        .file(
            mustExist = true, mustBeReadable = true,
            canBeFile = true, canBeDir = false
        ).help { "Encode the content of the given file" }

    val text by option("-t")
        .help { "Use the text from cli, will omit `-f`" }

    val eof by option("--eof")
        .default("EOF")
        .help { "The EOF flag, by default is `EOF`, used in System.in" }

    val charset: Charset by option("-c", "--charset")
        .convert { Charset.forName(it) }
        .default(
            when (Locale.getDefault().toLanguageTag()) {
                "zh-CN" -> Charset.forName("GBK")
                "zh-TW" -> Charset.forName("BIG5")
                else -> StandardCharsets.UTF_8
            }
        )
        .help {
            "Use specific charset for input and output. For windows, " +
                    "if the locale is zh-CN, then GBK will be used by default; " +
                    "if the locale is zh-TW, then BIG5 will be used by default; " +
                    "otherwise UTF-8 will be used by default. " +
                    "For non-windows users, the UTF-8 will be always used by default."
        }

    private val key by option("-k", "--key", metavar = "string")
        .help { "Use key to shuffle the output. Combined with --dh to use x25519" }

    private val dh by option("--dh", metavar = "hex")
        .help { "Your partner's dh public key" }

    val shuffleIteration by option("-n", "--iteration").int()
        .help { "The iteration of shuffle, default is 1200000" }
        .default(1200000)
        .check { it >= 1 }

    /**
     * When the user only gives a key, use it's sha3-256 as the encryption key.
     * If the user gives key and dh key, then use x25519 result as the encryption key.
     * Return null if user didn't give any key.
     * */
    fun getSecret(): ByteArray? {
        val hashedKey = key?.encodeToByteArray()?.sha3() ?: return null
        return if (dh == null) {
            hashedKey
        } else {
            val dhKey = dh!!.decodeHex()
            require(dhKey.size == 32) { "dh key must be 32 bytes (256 bits)" }
            x25519(hashedKey, dhKey).sha3()
        }
    }
}
