package top.nintha.broxy

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.net.ProxyOptions
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class HttpAsyncSender(val vertx: Vertx) {
    private fun spiltUrl(url: String): Pair<String, String> {
        return url.split("://")[1].split("/", limit = 2).let { Pair(it[0], "/${it[1]}") }
    }

    suspend fun getAsync(url: String, proxy: String = ""): String {
        val (host, route) = spiltUrl(url)
        val responseFuture = Future.future<HttpResponse<Buffer>>()
        val webClientOptions = WebClientOptions().apply {
            if (proxy.isNotBlank()) {
                val part = proxy.split(":")
                proxyOptions = ProxyOptions()
                proxyOptions.host = part[0]
                proxyOptions.port = part[1].toInt()
            }
            connectTimeout = 2000
            idleTimeout = 1
            isKeepAlive = false
            userAgent = HttpSender.randomUserAgent()
        }

        val client = WebClient.create(vertx, webClientOptions)
        return try {
            client.get(host, route).send(responseFuture)
            val response = responseFuture.await()
            response.bodyAsString()
        } catch (e: Exception) {
            ""
        } finally {
            client.close()
        }
    }

    fun checkProxies(proxies: Collection<String>): Set<String> = runBlocking(vertx.dispatcher()) {
        proxies.map { proxy ->
            async {
                val html = getAsync(HttpSender.TEST_URL, proxy)
                if (html.isNotBlank()) proxy else ""
            }
        }.map { it.await() }.filter { it.isNotBlank() }.toSet()
    }

}
