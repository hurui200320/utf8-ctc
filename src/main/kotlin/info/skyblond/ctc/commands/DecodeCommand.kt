package info.skyblond.ctc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import info.skyblond.ctc.Decoder.decodeCTC
import info.skyblond.ctc.derandomize
import info.skyblond.ctc.getRandomIntIter

object DecodeCommand : CliktCommand(
    name = "decode",
    help = "Decode text from CTC"
) {
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
            codes = codes.derandomize(
                getRandomIntIter(secret),
                codecOptions.shuffleIteration
            )
        }

        echo(codes.decodeCTC(ignoreError))
    }
}
