package info.skyblond.ctc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.defaultLazy
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
        .int().defaultLazy { terminal.info.width / 6 }
        .help { "How many code to print in one line, by default fit your terminal. Use `-1` to disable formatting." }


    override fun run() {
        val content = codecOptions.text
            ?: codecOptions.file?.readText()
            ?: askContent(codecOptions.eof, codecOptions.charset)
        var codes = content.encodeCTC()
        val secret = codecOptions.getSecret()
        if (secret != null) {
            codes = codes.randomize(
                getRandomIntIter(secret),
                codecOptions.shuffleIteration
            )
        }

        codes.toCTCString().let { s ->
            if (codePerLine > 0) {
                s.chunked(codePerLine * 6).forEach {
                    echo(it)
                }
            } else {
                echo(s)
            }
        }
    }
}
