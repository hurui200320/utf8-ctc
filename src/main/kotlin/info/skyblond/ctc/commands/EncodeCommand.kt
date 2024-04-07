package info.skyblond.ctc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import info.skyblond.ctc.Encoder.encodeCTC
import info.skyblond.ctc.getRandomIntIter
import info.skyblond.ctc.randomize
import info.skyblond.ctc.toCTCString

object EncodeCommand : CliktCommand(
    name = "encode",
    help = "Encode text into CTC"
) {
    private val codecOptions by CodecOptions()

    private val codePerLine by option("--code-per-line", "-w")
        .int().default(5)
        .help { "How many code to print in one line (at most), by default is `5`" }
        .check { it > 0 }



    override fun run() {
        val content = codecOptions.text
            ?: codecOptions.file?.readText()
            ?: askContent(codecOptions.eof, codecOptions.charset)
        var codes = content.encodeCTC()
        val width = (terminal.info.width / 6).coerceAtMost(codePerLine)
        val secret = codecOptions.getSecret()
        if (secret != null) {
            codes = codes.randomize(
                getRandomIntIter(secret),
                codecOptions.shuffleIteration
            )
        }

        codes.toCTCString().chunked(width * 6).forEach {
            echo(it)
        }
    }
}
