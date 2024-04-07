package info.skyblond.ctc

import java.math.BigInteger


object Encoder {
    private val charToCode = loadCTC().entries.associateBy({ it.value }, { it.key })

    private fun MutableList<Int>.encodeByteArrayToCodes(bytes: ByteArray) {
        var reminder = BigInteger(1, bytes)
        while (reminder > BigInteger.ZERO) {
            val mod = reminder.mod(BigInteger.valueOf(Constants.UTF8_MAX_VALUE))
            reminder = reminder.divide(BigInteger.valueOf(Constants.UTF8_MAX_VALUE))
            add(mod.toInt())
        }
    }

    private fun MutableList<Int>.encodeUTF8(str: String) {
        if (str.isEmpty()) return

        val raw = str.encodeToByteArray()
        val compressed = compress(raw)

        if (raw.size <= compressed.size) {
            add(Constants.UTF8_START)
            encodeByteArrayToCodes(raw)
            add(Constants.UTF8_END)
        } else {
            add(Constants.UTF8_COMPRESSED_START)
            encodeByteArrayToCodes(compressed)
            add(Constants.UTF8_COMPRESSED_END)
        }
    }

    private fun encode(str: String): List<Int> {
        val codes = mutableListOf<Int>()
        var unknownChars = ""
        str.forEach { c ->
            if (charToCode.containsKey(c.toString())) {
                // we can decode this one
                // check if we have unknown chars
                if (unknownChars.isNotEmpty()) {
                    codes.encodeUTF8(unknownChars)
                    unknownChars = ""
                }
                // then process the current char
                codes.add(charToCode[c.toString()]!!)
            } else { // unknown, add to the unknown char list
                unknownChars += c
            }
        }
        // process the last unknown chars
        if (unknownChars.isNotEmpty()) codes.encodeUTF8(unknownChars)

        return codes
    }

    fun String.encodeCTC(): List<Int> {
        var fixedStr = this
            .replace("\r", "")
            .replace("\n", Constants.NEW_LINE_CHAR)
        Constants.fixMappings.forEach { (from, to) ->
            fixedStr = fixedStr.replace(from, to)
        }

        return encode(fixedStr)
    }
}
