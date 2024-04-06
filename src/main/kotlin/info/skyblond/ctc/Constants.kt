package info.skyblond.ctc

object Constants {
    /**
     * Use this char to represent `\n`.
     * */
    const val NEW_LINE_CHAR: String = "〷"

    /**
     * The char code to represent UTF-8 data will be ranged from 0 to 9991.
     * */
    const val UTF8_MAX_VALUE = 9992L

    /**
     * Char code to mark the start of UTF-8 byte array.
     * */
    const val UTF8_START = 9992

    /**
     * Char code to mark the end of UTF-8 byte array.
     * */
    const val UTF8_END = 9993

    /**
     * Char code to mark the start of compressed UTF-8 byte array.
     * */
    const val UTF8_COMPRESSED_START = 9994

    /**
     * Char code to mark the end of compressed UTF-8 byte array.
     * */
    const val UTF8_COMPRESSED_END = 9995

    /**
     * Mappings for fixing string. Like `1日` to `㏠`
     * */
    val fixMappings = mapOf(
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
    val reverseFixMappings = fixMappings.entries.associateBy({ it.value }, { it.key })
}
