package info.skyblond.ctc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import info.skyblond.ctc.*

object DecodeCommand : CliktCommand(
    name = "decode"
) {
    override fun help(context: Context): String {
        return "Decode text from CTC"
    }

    private val codecOptions by CodecOptions()
    private val ignoreError by option("-i", "--ignore-error", help = "Ignore error")
        .flag(default = false)
        .help { "Ignore error and keep decoding. Default: `false`" }

    override fun run() {
        val input = (codecOptions.text
            ?: codecOptions.file?.readText()
            ?: askContent(codecOptions.eof, codecOptions.charset)).lines()

        val errorLines = input.filterNot {
            verifyISO7064(it)
        }
        errorLines.forEach {
            echo("Detect error line: $it", err = true)
        }
        if (errorLines.isNotEmpty() && !ignoreError) {
            error("There are errors in the input")
        }

        val content = input.joinToString("") { line ->
            line.trim().filter { it in '0'..'9' }.dropLast(2)
        }
        var codes = content.chunked(4).map { it.toInt() }

        val secret = codecOptions.getSecret()
        if (secret != null) {
            val nonce = codecOptions.getNonce() ?: error("Nonce is required for decryption")
            val sipHashKey = secret.hkdfSipHashKey()
            val mac = codecOptions.getMac() ?: error("MAC is required for decryption")
            val calculatedSipHash = codes.sipHash(sipHashKey)
            require(calculatedSipHash.contentEquals(mac)) { "MAC mismatch" }
            codes = codes.asSequence().decrypt(
                createChacha20CodeSeq(secret, nonce)
            ).toList()
        }

        echo(codes.decodeCTC(ignoreError))
    }
}
