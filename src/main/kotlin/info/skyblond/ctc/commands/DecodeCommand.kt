package info.skyblond.ctc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import info.skyblond.ctc.Decoder.decodeCTC

object DecodeCommand : CliktCommand(
    name = "decode",
    help = "Decode text from CTC"
) {
    private val file by option("-f")
        .file(
            mustExist = true, mustBeReadable = true,
            canBeFile = true, canBeDir = false
        ).help { "Decode the content of the given file" }

    private val text by option("-t").help { "Use the text from cli, will omit `-f`" }

    private val eof by option("--eof")
        .default("EOF")
        .help { "The EOF flag, by default is `EOF`, used in System.in" }

    override fun run() {
        val content = (text ?: file?.readText() ?: askContent(eof)).filter { it in '0'..'9' }
        echo(content.chunked(4).map { it.toInt() }.decodeCTC())
    }
}
