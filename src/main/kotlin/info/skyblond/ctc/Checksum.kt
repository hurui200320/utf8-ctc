package info.skyblond.ctc

import java.math.BigInteger
import kotlin.text.filter

/**
 * Generate an ISO 7064 Mod 97,10 checksum.
 *
 * The returned checksum should append at the end of the input digits.
 * */
fun generateISO7064(rawInputDigits: String): String {
    val digits = rawInputDigits.filter { it in '0'..'9' }
    // mod(data * 100, 97)
    val data100 = BigInteger(digits + "00")
    val part = data100.mod(BigInteger.valueOf(97))
    // checksum = mod(98 - part, 97)
    val checksum = BigInteger.valueOf(98).subtract(part).mod(BigInteger.valueOf(97))

    return checksum.toString().padStart(2, '0')
}

fun verifyISO7064(rawInputDigitsWithChecksum: String): Boolean {
    val digits = rawInputDigitsWithChecksum.filter { it in '0'..'9' }
    val data = BigInteger(digits)
    return data.mod(BigInteger.valueOf(97)) == BigInteger.ONE
}