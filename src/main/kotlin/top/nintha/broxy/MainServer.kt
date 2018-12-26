package top.nintha.broxy

import io.vertx.core.AbstractVerticle
import io.vertx.ext.web.Router
import io.vertx.core.http.HttpServerOptions

class MainServer : AbstractVerticle() {
    override fun start() {
        val serverOptions = HttpServerOptions().setCompressionSupported(true)
        val server = vertx.createHttpServer(serverOptions)
        val mainRouter = Router.router(vertx)
        mainRouter.get("/proxy").handler { routingContext ->
            val address = NestStorage.getAllAddress()
            routingContext.response().restEnd( mapOf("code" to 100, "data" to mapOf("total" to address.size, "list" to address)))
        }

        mainRouter.get("/binaryProxy").handler { routingContext ->
            val address = NestStorage.getAllAddress()
            routingContext.response().restEnd( mapOf("code" to 100, "data" to mapOf("total" to address.size, "list" to address.map { NestStorage.iaToLong(it).toString() })))
        }

        server.requestHandler(mainRouter).listen(8080);
    }
}
