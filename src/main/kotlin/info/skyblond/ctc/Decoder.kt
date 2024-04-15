package info.skyblond.ctc

import java.math.BigInteger

object Decoder {
    private val codeToChar = loadCTC()

    private fun List<Int>.decodeToByteArray(): ByteArray {
        var sum = BigInteger.ZERO
        this.reversed().forEach { i ->
            sum = sum.multiply(BigInteger.valueOf(Constants.UTF8_MAX_VALUE))
            sum = sum.add(BigInteger.valueOf(i.toLong()))
        }
        // check the sign, if the first byte is negative, the big integer will prefix with 0
        val result = sum.toByteArray()
        return if (result[0] == 0.toByte() && result[1] < 0) {
            result.copyOfRange(1, result.size)
        } else {
            result
        }
    }

    private fun MutableList<Int>.decodeUTF8(): String {
        if (isEmpty()) return ""

        val startFlag = this.removeFirst()
        if (startFlag == Constants.UTF8_START) {
            // raw UTF8
            val content = mutableListOf<Int>()
            while (true) {
                val c = this.removeFirst()
                if (c == Constants.UTF8_END) break
                content.add(c)
            }
            return content.decodeToByteArray().decodeToString()
        } else if (startFlag == Constants.UTF8_COMPRESSED_START) {
            // compressed UTF8
            val content = mutableListOf<Int>()
            while (true) {
                val c = this.removeFirst()
                if (c == Constants.UTF8_COMPRESSED_END) break
                content.add(c)
            }
            return decompress(content.decodeToByteArray()).decodeToString()
        } else error("The input is not UTF8 encoded: $startFlag")
    }

    private fun decode(codes: List<Int>, ignoreError: Boolean): String {
        val sb = StringBuilder()
        val codesBuffer = codes.toMutableList()

        while (codesBuffer.isNotEmpty()) {
            val c = codesBuffer.removeFirst()
            try {
                if (c == Constants.UTF8_START || c == Constants.UTF8_COMPRESSED_START) {
                    codesBuffer.addFirst(c)
                    sb.append(codesBuffer.decodeUTF8())
                } else if (codeToChar.containsKey(c)) {
                    sb.append(codeToChar[c]!!)
                } else error("Unknown code: $c")
            } catch (t: Throwable) {
                if (!ignoreError) throw t
                else sb.append("\u25a1")
            }
        }

        return sb.toString()
    }

    fun List<Int>.decodeCTC(ignoreError: Boolean): String {
        var str = decode(this, ignoreError).replace(Constants.NEW_LINE_CHAR, "\n")
        Constants.reverseFixMappings.forEach { (from, to) ->
            str = str.replace(from, to)
        }
        return str
    }
}
