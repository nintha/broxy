package com.github.nintha

data class ProxyItem(var iaNum: Long) {
    companion object {
        const val MAX_FAILED_TIMES: Int = 2
    }

    var filedTimes: Int = 0
    var nextIdleTime: Long = System.currentTimeMillis() // 空闲时间戳

    fun getIaString(): String = NestStorage.longToIa(iaNum)
    fun getIpPortPair(): Pair<String, Int> = Pair(NestStorage.longToIp(iaNum.shr(16)) ,iaNum.and(65535L).toInt())
    fun isIdle(): Boolean = System.currentTimeMillis() >= nextIdleTime
    fun isInvalid(): Boolean = filedTimes >= MAX_FAILED_TIMES
    fun isValid(): Boolean = filedTimes < MAX_FAILED_TIMES
}