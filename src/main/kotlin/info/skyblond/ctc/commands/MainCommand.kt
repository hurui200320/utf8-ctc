package info.skyblond.ctc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

object MainCommand : CliktCommand(
    name = "ctc",
    printHelpOnEmptyArgs = true
) {
    init {
        subcommands(
            DecodeCommand,
            EncodeCommand,
            PrintPubKeyCommand,
        )
    }

    override fun run() {
    }
}
