package info.skyblond.ctc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import info.skyblond.ctc.createChacha20CodeSeq
import info.skyblond.ctc.decodeCTC
import info.skyblond.ctc.decrypt

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
        val content = (
                codecOptions.text
                    ?: codecOptions.file?.readText()
                    ?: askContent(codecOptions.eof, codecOptions.charset)
                ).filter { it in '0'..'9' }
        var codes = content.chunked(4).map { it.toInt() }

        val secret = codecOptions.getSecret()
        if (secret != null) {
            val nonce = codecOptions.getNonce() ?: error("Nonce is required for decryption")
            codes = codes.asSequence().decrypt(
                createChacha20CodeSeq(secret, nonce)
            ).toList()
        }

        echo(codes.decodeCTC(ignoreError))
    }
}
