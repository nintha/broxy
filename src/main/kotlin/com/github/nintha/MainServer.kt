package com.github.nintha

import io.vertx.core.AbstractVerticle

class MainServer : AbstractVerticle() {
    override fun start() {
        vertx.createHttpServer()
                .requestHandler { req ->
                    req.response()
                            .putHeader("content-type", "text/plain")
                            .end("Hello from Kotlin Vert.x")
                }.listen(8080)
    }
}