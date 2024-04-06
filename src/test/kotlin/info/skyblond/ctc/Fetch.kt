package info.skyblond.ctc

import org.jsoup.Jsoup
import java.io.File

object Fetch {
    @JvmStatic
    fun main(args: Array<String>) {
        val body = Jsoup
            .connect("https://en.wiktionary.org/wiki/Appendix:Chinese_telegraph_code/Mainland_1983")
            .proxy("127.0.0.1", 1081)
            .get().body()
        val map = body.getElementsByTag("tbody").flatMap { tbody ->
            tbody.getElementsByTag("td").mapNotNull { td ->
                val id = td.ownText()
                val char = td.getElementsByTag("span").firstOrNull()?.text()
                if (!char.isNullOrEmpty() && char.length == 1) id to char else null
            }
        }.associate { it }

        File("./src/main/resources/ctc.txt").printWriter().use {
            map.forEach { (t, u) ->
                println("$t -> $u")
                it.println("$t$u")
            }
        }
    }
}
