package top.nintha.broxy

import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.Json
import java.util.*

/**
 * object to json
 */
fun <T> T.toJson():String = Json.encode(this)

/**
 * response 返回JSON数据
 */
fun HttpServerResponse.restful(): HttpServerResponse {
    return this.putHeader("content-type", "application/json")
}

fun HttpServerResponse.restEnd(chunk: Any) {
    val json = when(chunk){
        is String -> chunk
        else -> Json.encode(chunk)
    }
    this.putHeader("content-type", "application/json").end(json)
}