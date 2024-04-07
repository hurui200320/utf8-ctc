package info.skyblond.ctc.commands

import com.github.ajalt.clikt.core.CliktCommand
import java.nio.charset.Charset
import java.util.*

fun CliktCommand.askContent(eof: String, charset: Charset): String {
    echo(">> ", trailingNewline = false)
    val scanner = Scanner(System.`in`, charset)
    val sb = StringBuilder()
    while (scanner.hasNextLine()) {
        val l = scanner.nextLine()
        if (l == eof) break
        else sb.append(l).append("\n")
        echo(">> ", trailingNewline = false)
    }
    return sb.toString().trim()
}
