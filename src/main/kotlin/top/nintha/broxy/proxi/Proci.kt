package top.nintha.broxy.proxi

import org.slf4j.Logger
import top.nintha.broxy.HttpAsyncSender

interface Proci {
    val logger: Logger
    fun fetch(): Set<String>

    /**
     * 获取并校验
     */
    fun fetchAndCheck(): Set<String> {
        return HttpAsyncSender.instance.checkProxies(fetch())
    }
}