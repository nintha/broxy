package top.nintha.broxy.proxi

import top.nintha.broxy.HttpSender
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Ip98daili : Proci {
    override val logger: Logger = LoggerFactory.getLogger(this::class.java)
    private val urls = listOf("http://www.89ip.cn/tqdl.html?api=1&num=9999&port=&address=&isp=")
    private val regex = Regex("""<\/script>\n(.*)<br>""")
    override fun fetch(): Set<String> {
        val list = urls.flatMap {
            val html = HttpSender.get(it)
            regex.findAll(html).flatMap { mr ->
                (mr.groups[1]?.value ?: "").split("<br>").asSequence().filter(StringUtils::isNotBlank)
            }.toList()
        }
        return list.toSet()
    }
}

//fun main(args: Array<String>) {
//    val proxi = Ip98daili()
//    val proxys = proxi.fetch()
//    println("proxies size=${proxys.size}")
//    LoggerFactory.getLogger("Ip98daili").info(proxys.toString())
//}