package info.skyblond.ctc

import info.skyblond.ctc.Decoder.decodeCTC
import info.skyblond.ctc.Encoder.encodeCTC
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CodecTest {

    private val line = """
        我可以吞下玻璃而不伤害身体。
        I can eat glass, it does not hurt me.
        
        This is a super long text: Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque tristique sollicitudin augue, vitae blandit justo egestas sed. Praesent sit amet nunc faucibus, mattis ante sit amet, efficitur justo. Nullam erat est, pretium in tincidunt hendrerit, ornare quis lectus. Cras aliquam nisi ut auctor luctus. Pellentesque ullamcorper magna eu tellus egestas convallis. Nunc iaculis diam a venenatis tempus. Vivamus in lobortis turpis. Cras in pretium tellus. Integer eu ex leo. Maecenas sit amet felis dolor. Sed lobortis eget ligula a venenatis. Maecenas dignissim euismod elit tincidunt commodo. Donec eu auctor lectus. Sed nec est et augue facilisis dignissim nec vel purus.
        斑淀秦沸榴斑匝级弃章产鹰莫螟着氨或黄军五或枝诈引涡，晰砷涂谦态砌狠砍至东赞达嚼宏质安慰萌唉胫嘛怀七堂字皮俱送匈定搂孵时膨览匪穗然圭昨铰念意赏！鹰诉坠浅变履远彝余谣远骑青避蜂毅，唉赚钟噪敞蛴钟炒阁标恼吼穿筐狠非纸翠誓赫兄以彭州蜡醚荀渔必尖厦沸绪浊懒洲妥锈述方腐块雷露育某祝橙溜苯醋掷锻纱每哀林芳秤肢卷口藤脆争羔礼祖。
        
        
        天匠染青红，花腰呈嬝娜。袅娜的娜作女子名的时候念Na。袅娜的娜作女子名的时候念钠。我看到一片广阔的海岸，在我们面前展现的事物超越头脑所能理解的范畴，沙滩上的每一粒沙子，每一滴水和空气分子都是在讲述一个故事。每个都是要被唱响的歌。他们每个人都充满生机，笑声，苦难和仇恨。他们都是一样的，即使他们都是不同的。师者教书育人 虽呕心沥血 亦在所不惜
        孩子不应该悲伤
        童年应该更快乐
        我们原本就是为了孩子而制造的
        孩子是希望
        Anyone who reads Old and Middle English literary texts will be familiar with the mid-brown volumes of the EETS, with the symbol of Alfred's jewel embossed on the front cover. Most of the works attributed to King Alfred or to Aelfric, along with some of those by bishop Wulfstan and much anonymous prose and verse from the pre-Conquest period, are to be found within the Society's three series; all of the surviving medieval drama, most of the Middle English romances, much religious and secular prose and verse including the English works of John Gower, Thomas Hoccleve and most of Caxton's prints all find their place in the publications. Without EETS editions, study of medieval English texts would hardly be possible.
        
        今天是2024年4月6日，现在是20点55分。我正在关注EFF，即电子前哨基金会。
    """.trimIndent()


    @Test
    fun testEncodeAndDecode() {
        val encoded = line.encodeCTC()
        println(encoded.toCTCString())
        println("Raw size: ${line.length} chars")
        println("Encoded: ${encoded.size} codes, total ${encoded.size * 4} digits")
        val decoded = encoded.decodeCTC()
        assertEquals(line, decoded)
    }
}
