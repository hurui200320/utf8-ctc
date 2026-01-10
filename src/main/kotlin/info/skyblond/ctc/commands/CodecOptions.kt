package info.skyblond.ctc.commands

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import info.skyblond.ctc.decodeHex
import info.skyblond.ctc.sha3
import info.skyblond.ctc.x25519
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*

class CodecOptions : OptionGroup(
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
            if (System.getProperty("os.name").lowercase().startsWith("windows")) {
                when (Locale.getDefault().toLanguageTag()) {
                    "zh-CN" -> Charset.forName("GBK")
                    "zh-TW" -> Charset.forName("BIG5")
                    else -> StandardCharsets.UTF_8
                }
            } else StandardCharsets.UTF_8
        )
        .help {
            "Use specific charset for input and output. For windows, " +
                    "if the locale is zh-CN, then GBK will be used by default; " +
                    "if the locale is zh-TW, then BIG5 will be used by default; " +
                    "otherwise UTF-8 will be used by default. " +
                    "For non-windows users, the UTF-8 will be always used by default."
        }

    private val key by option("-k", "--key", metavar = "string")
        .help {
            "Use key to encrypt the output. If no dh, use as utf-8 string then apply sha3 256 as key. " +
                    "Combined with --dh to use x25519, in this case, the key will be treated as hex string."
        }

    private val dh by option("--dh", metavar = "hex")
        .help { "Your partner's dh public key, in hex format." }

    private val nonce by option("-n", "--nonce", metavar = "hex")
        .help {
            "The nonce for encryption, in hex format. Must be 12 bytes (96 bits). " +
                    "This will be generated when encrypting, and is required when decrypting."
        }

    private val mac by option("-m", "--mac", metavar = "hex")
        .help {
            "The mac for encrypted codes, in hex format. Must be 8 bytes (64 bits). " +
                    "This will be generated when encrypting, and is required when decrypting."
        }

    /**
     * If dh is not enabled, treat key as utf8 string and apply sha3 256 as the encryption key.
     *
     * If dh is enabled, use x25519 to get the encryption key, key will be our private key in hex format,
     * and [dh] will provide the partner's public key in hex format. The x25519 result will be applied sha3 256,
     * the result is the encryption key.
     *
     * Return null if the user didn't give any key and dh is not enabled.
     * */
    fun getSecret(): ByteArray? {
        return if (dh == null) {
            key?.encodeToByteArray()?.sha3() ?: return null
        } else {
            require(key != null) { "dh public key must be given via `-k` or `--key`" }
            val dhKey = dh!!.decodeHex()
            require(dhKey.size == 32) { "dh key must be 32 bytes (256 bits)" }
            x25519(key!!.decodeHex(), dhKey).sha3()
        }
    }

    fun getNonce(): ByteArray? = nonce
        ?.replace(" ", "")
        ?.decodeHex()
        ?.apply { require(this.size == 12) { "nonce must be 12 bytes (96 bits)" } }

    fun getMac(): ByteArray? = mac
        ?.replace(" ", "")
        ?.decodeHex()
        ?.apply { require(this.size == 8) { "mac must be 8 bytes (64 bits)" } }
}
