package com.github.nintha

import com.github.nintha.proxi.*
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier

class NestStorage {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
        // address => proxyItem
        private val storage: ConcurrentHashMap<Long, ProxyItem> = ConcurrentHashMap()

        private const val STORAGE_PATH = "storage/proxy.data"

        private const val LINE_SEPARATOR = "\n"

        private val PROCIS: Set<Proci> = setOf(Ip98daili(), Kuaidaili(), Xicidaili(), Cnproxy())

        fun loadData() {
            val exist: Boolean = Paths.get(STORAGE_PATH).let { Files.exists(it) }
            if (!exist) {
                logger.warn("'$STORAGE_PATH' is not existed.")
                return
            }

            Paths.get(STORAGE_PATH).toFile().readLines().map { it.toLong() }.forEach { storage[it] = ProxyItem(it) }
            logger.info("[loadData] storage size=${storage.size}")
        }

        fun saveData() {
            val lines = storage.keys().toList().joinToString(separator = LINE_SEPARATOR)
            val path = Paths.get(STORAGE_PATH)
            Files.createDirectories(path.parent)
            path.toFile().writeText(lines)
            logger.info("[saveData] storage size=${storage.size}")
        }

        fun saveDataAsync(): CompletableFuture<Void> {
            return CompletableFuture.runAsync(this::saveData)
        }

        fun add(proxyAddress: Collection<String>) {
            storage.putAll(proxyAddress.filter { StringUtils.isNotBlank(it) }.map { iaToLong(it) }.associate { Pair(it, ProxyItem(it)) })
        }

        fun remove(address: String) {
            storage.remove(iaToLong(address))
        }

        fun getAllAddress(): Set<String> {
            return storage.keys.map { longToIa(it) }.toSet()
        }

        fun getSize(): Int = storage.size

        fun notInStorage(address: String): Boolean {
            if(StringUtils.isBlank(address)) return false
            return !storage.containsKey(iaToLong(address))
        }

        fun fetchProcisAsync(): CompletableFuture<Void> {
            return CompletableFuture.runAsync {
                PROCIS.forEach { proci ->
                    logger.info("[fetchProcisAsync] try ${proci.javaClass.simpleName}")
                    val items = proci.fetchAndCheck().apply { add(this) }
                    logger.info("[fetchProcisAsync] ${proci.javaClass.simpleName} > ${items.size}")
                }
            }
        }

        // 对所有代理进行校验并移除无效代理
        fun selfCheck() {
            logger.info("[selfCheck] start, storage size=${getSize()}")
            fun check(item: String): Boolean {
                return try {
                    val strs = item.split(":")
                    val html = HttpSender.get(HttpSender.TEST_URL, strs[0], strs[1].toInt())
                    StringUtils.isNotBlank(html)
                } catch (e: Exception) {
                    false
                }
            }

            val invalidItems = getAllAddress().map {
                CompletableFuture.supplyAsync(Supplier { Pair(it, check(it)) }, HttpSender.threadPool)
            }.asSequence().map { it.get() }.filter { !it.second }.map { it.first }.toSet()
            invalidItems.forEach { remove(it) }
            saveData()
            logger.info("[selfCheck] remove invalid items=${invalidItems.size}, storage size=${getSize()}")
        }

        // ia 48bit = ip 32bit + port 16bit
        fun iaToLong(ia: String): Long {
            try {
                val array = ia.split(":")
                return array[1].toLong() + ipToLong(array[0]).shl(16)
            } catch (e: Exception) {
                logger.error("ia=$ia", e)
            }
            return 0
        }

        fun longToIa(num: Long): String = "${longToIp(num.shr(16))}:${num.and(65535L)}"

        fun ipToLong(ip: String): Long {
            try {
                val ipParts = ip.split(".")
                return ipParts[0].toLong().shl(24) + ipParts[1].toLong().shl(16) + ipParts[2].toLong().shl(8) + ipParts[3].toLong()
            }catch (e: Exception){
                logger.error("ip=$ip", e)
            }
            return 0
        }

        fun longToIp(num: Long): String {
            val a = num.shr(24).and(255L)
            val b = num.shr(16).and(255L)
            val c = num.shr(8).and(255L)
            val d = num.and(255L)
            return "$a.$b.$c.$d"
        }
    }
}


//fun main(args: Array<String>) {
//    val proxy = "192.168.11.250:8923"
//    val num = iaToLong(proxy)
//    val ia = longToIa(num)
//    println(num)
//    println(ia)
//
//}