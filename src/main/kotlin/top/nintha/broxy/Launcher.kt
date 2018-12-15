package top.nintha.broxy

import io.vertx.core.Vertx
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

private val logger: Logger = LoggerFactory.getLogger("Launcher")

fun main(args: Array<String>) {
    logger.info("==================== start.")
    Vertx.vertx().deployVerticle(MainServer())
    launchNestStorage()

}

fun launchNestStorage(): Timer {
    NestStorage.loadData()
    NestStorage.selfCheck()
    return Timer().also {
        it.schedule(object : TimerTask() {
            override fun run() {
                NestStorage.fetchProcisAsync().join()
                NestStorage.saveData()

                if (NestStorage.getSize() > 2000) {
                    NestStorage.selfCheck()
                }
                val st = System.currentTimeMillis()
                System.gc()
                logger.info("[GC] cost: ${System.currentTimeMillis() - st} ms")
            }
        }, 0, 1000 * 60 * 5)
    }
}