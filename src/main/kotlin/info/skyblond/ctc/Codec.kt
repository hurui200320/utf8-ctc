package info.skyblond.ctc

import java.math.BigInteger

/**
 * Code to char mapping, used for decoding.
 * */
private val codeToChar = loadCTC()

/**
 * Char to code mapping, used for encoding.
 * */
private val charToCode = codeToChar.entries.associateBy({ it.value }, { it.key })

/**
 * Use this char to represent `\n`.
 * */
private const val NEW_LINE_CHAR: String = "〷"

/**
 * The char code to represent UTF-8 data will be ranged from 0 to 9991.
 * */
private const val UTF8_MAX_VALUE = 9992L

/**
 * Char code to mark the start of an UTF-8 byte array.
 * */
private const val UTF8_START = 9992

/**
 * Char code to mark the end of an UTF-8 byte array.
 * */
private const val UTF8_END = 9993

/**
 * Char code to mark the start of a compressed UTF-8 byte array.
 * */
private const val UTF8_COMPRESSED_START = 9994

/**
 * Char code to mark the end of a compressed UTF-8 byte array.
 * */
private const val UTF8_COMPRESSED_END = 9995

/**
 * Mappings for fixing string. Like `1日` to `㏠`
 * */
private val fixMappings = mapOf(
    "10日" to "㏩",
    "11日" to "㏪",
    "12日" to "㏫",
    "13日" to "㏬",
    "14日" to "㏭",
    "15日" to "㏮",
    "16日" to "㏯",
    "17日" to "㏰",
    "18日" to "㏱",
    "19日" to "㏲",
    "20日" to "㏳",
    "21日" to "㏴",
    "22日" to "㏵",
    "23日" to "㏶",
    "24日" to "㏷",
    "25日" to "㏸",
    "26日" to "㏹",
    "27日" to "㏺",
    "28日" to "㏻",
    "29日" to "㏼",
    "30日" to "㏽",
    "31日" to "㏾",
    "1日" to "㏠",
    "2日" to "㏡",
    "3日" to "㏢",
    "4日" to "㏣",
    "5日" to "㏤",
    "6日" to "㏥",
    "7日" to "㏦",
    "8日" to "㏧",
    "9日" to "㏨",
    "。。。" to "…",
    "10点" to "㍢",
    "11点" to "㍣",
    "12点" to "㍤",
    "13点" to "㍥",
    "14点" to "㍦",
    "15点" to "㍧",
    "16点" to "㍨",
    "17点" to "㍩",
    "18点" to "㍪",
    "19点" to "㍫",
    "20点" to "㍬",
    "21点" to "㍭",
    "22点" to "㍮",
    "23点" to "㍯",
    "24点" to "㍰",
    "0点" to "㍘",
    "1点" to "㍙",
    "2点" to "㍚",
    "3点" to "㍛",
    "4点" to "㍜",
    "5点" to "㍝",
    "6点" to "㍞",
    "7点" to "㍟",
    "8点" to "㍠",
    "9点" to "㍡",
    "10月" to "㋉",
    "11月" to "㋊",
    "12月" to "㋋",
    "1月" to "㋀",
    "2月" to "㋁",
    "3月" to "㋂",
    "4月" to "㋃",
    "5月" to "㋄",
    "6月" to "㋅",
    "7月" to "㋆",
    "8月" to "㋇",
    "9月" to "㋈",
)

/**
 * Reverse of the [fixMappings].
 * */
private val reverseFixMappings = fixMappings.entries.associateBy({ it.value }, { it.key })


/**
 * Encode a [ByteArray] into a list of CTC codes using base9992,
 * and append to the current list.
 * */
private fun MutableList<Int>.encodeByteArrayToCodes(bytes: ByteArray) {
    var reminder = BigInteger(1, bytes)
    while (reminder > BigInteger.ZERO) {
        val mod = reminder.mod(BigInteger.valueOf(UTF8_MAX_VALUE))
        reminder = reminder.divide(BigInteger.valueOf(UTF8_MAX_VALUE))
        add(mod.toInt())
    }
}

/**
 * Decode a list of CTC codes as base9992 as [ByteArray].
 * */
private fun List<Int>.decodeToByteArray(): ByteArray {
    var sum = BigInteger.ZERO
    this.reversed().forEach { i ->
        sum = sum.multiply(BigInteger.valueOf(UTF8_MAX_VALUE))
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

/**
 * Encode a UTF-8 string into a list of CTC codes.
 * Will try compress and use the shorter one.
 *
 * For uncompressed data, wrapped with [UTF8_START] and [UTF8_END].
 * For compressed data, wrapped with [UTF8_COMPRESSED_START] and [UTF8_COMPRESSED_END].
 *
 * The result will be appended to the current list.
 * */
private fun MutableList<Int>.encodeUTF8(str: String) {
    if (str.isEmpty()) return

    val raw = str.encodeToByteArray()
    val compressed = compress(raw)

    if (raw.size <= compressed.size) {
        add(UTF8_START)
        encodeByteArrayToCodes(raw)
        add(UTF8_END)
    } else {
        add(UTF8_COMPRESSED_START)
        encodeByteArrayToCodes(compressed)
        add(UTF8_COMPRESSED_END)
    }
}

/**
 * Decode a list of CTC codes as UTF-8 string.
 *
 * Depends on the first byte to determine the encoding type:
 * + [UTF8_START]: raw UTF8, will stop when met an [UTF8_END] mark
 * + [UTF8_COMPRESSED_START]: compressed UTF8, will stop when met an [UTF8_COMPRESSED_END] mark
 *
 * The decoded code will be removed from the current list.
 * */
private fun MutableList<Int>.decodeUTF8(): String {
    if (isEmpty()) return ""

    val startFlag = this.removeFirst()
    if (startFlag == UTF8_START) {
        // raw UTF8
        val content = mutableListOf<Int>()
        while (true) {
            val c = this.removeFirst()
            if (c == UTF8_END) break
            content.add(c)
        }
        return content.decodeToByteArray().decodeToString()
    } else if (startFlag == UTF8_COMPRESSED_START) {
        // compressed UTF8
        val content = mutableListOf<Int>()
        while (true) {
            val c = this.removeFirst()
            if (c == UTF8_COMPRESSED_END) break
            content.add(c)
        }
        return decompress(content.decodeToByteArray()).decodeToString()
    } else error("The input is not UTF8 encoded: $startFlag")
}

/**
 * Encoding raw string into a list of CTC codes.
 * Will automatically handle non-Chinese characters.
 *
 * This will not apply any pre-processing to the input string.
 * */
private fun encodeRawString(str: String): List<Int> {
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

/**
 * Decode a list of CTC codes into a string.
 * Will automatically handle non-Chinese characters.
 *
 * This will not apply any post-processing to the result string.
 *
 * @param ignoreError Whether to ignore errors during decoding.
 *                    If true, unknown characters will be replaced with a question mark.
 * */
private fun decodeRawString(codes: List<Int>, ignoreError: Boolean): String {
    val sb = StringBuilder()
    val codesBuffer = codes.toMutableList()

    while (codesBuffer.isNotEmpty()) {
        val c = codesBuffer.removeFirst()
        try {
            if (c == UTF8_START || c == UTF8_COMPRESSED_START) {
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

/**
 * Encode a string into a list of CTC codes.
 * Will automatically handle non-Chinese characters.
 *
 * This will apply some pre-processing to shorten the input string.
 *
 * @return The encoded CTC codes.
 * */
fun String.encodeCTC(): List<Int> {
    var fixedStr = this
        .replace("\r", "")
        .replace("\n", NEW_LINE_CHAR)
    fixMappings.forEach { (from, to) ->
        fixedStr = fixedStr.replace(from, to)
    }

    return encodeRawString(fixedStr)
}

/**
 * Decode a list of CTC codes into a string.
 * Will automatically handle non-Chinese characters.
 *
 * This will apply some post-processing to the result string
 * (aka the reverse of [encodeCTC]).
 *
 * @param ignoreError Whether to ignore errors during decoding.
 *                    If true, unknown characters will be replaced with a question mark.
 * */
fun List<Int>.decodeCTC(ignoreError: Boolean): String {
    var str = decodeRawString(this, ignoreError)
        .replace(NEW_LINE_CHAR, "\n")
    reverseFixMappings.forEach { (from, to) ->
        str = str.replace(from, to)
    }
    return str
}