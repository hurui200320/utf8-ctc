package info.skyblond.ctc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import info.skyblond.ctc.sha3
import info.skyblond.ctc.toHexString
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters

object PrintPubKeyCommand: CliktCommand(
    name = "pubkey",
    help = "Print the public key of the given private key"
) {
    private val key by argument("key")
        .help { "Your private key" }

    override fun run() {
        val privateKey = key.encodeToByteArray().sha3()
        val publicKey = X25519PrivateKeyParameters(privateKey).generatePublicKey().encoded
        echo(publicKey.toHexString().uppercase())
    }
}
