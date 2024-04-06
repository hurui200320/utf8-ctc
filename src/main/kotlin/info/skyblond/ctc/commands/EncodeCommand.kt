package info.skyblond.ctc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import info.skyblond.ctc.Encoder.encodeCTC
import info.skyblond.ctc.toCTCString
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*

object EncodeCommand : CliktCommand(
    name = "encode",
    help = "Encode text into CTC"
) {
    private val file by option("-f")
        .file(
            mustExist = true, mustBeReadable = true,
            canBeFile = true, canBeDir = false
        ).help { "Encode the content of the given file" }

    private val text by option("-t")
        .help { "Use the text from cli, will omit `-f`" }

    private val eof by option("--eof")
        .default("EOF")
        .help { "The EOF flag, by default is `EOF`, used in System.in" }

    private val codePerLine by option("--code-per-line", "-w")
        .int().default(5)
        .help { "How many code to print in one line (at most), by default is `5`" }
        .check { it > 0 }

    private val charset by option("-c", "--charset")
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

    override fun run() {
        val content = text ?: file?.readText() ?: askContent(eof, charset)
        val codes = content.encodeCTC()
        val width = (terminal.info.width / 6).coerceAtMost(codePerLine)

        codes.toCTCString().chunked(width * 6).forEach {
            echo(it)
        }
    }
}
