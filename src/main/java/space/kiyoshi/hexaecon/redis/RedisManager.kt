/*
 * Copyright (c) 2023. KiyoshiDevelopment
 *   All rights reserved.
 */

@file:Suppress("unused")

package space.kiyoshi.hexaecon.redis

import redis.clients.jedis.Jedis

class RedisManager(private val host: String, private val port: Int) {
    private lateinit var jedis: Jedis

    init {
        connect()
    }

    private fun connect() {
        jedis = Jedis(host, port)
        jedis.connect()
    }

    fun disconnect() {
        jedis.disconnect()
    }

    fun setValue(key: String, value: String) {
        jedis.set(key, value)
    }

    fun getValue(key: String): String? {
        return jedis.get(key)
    }

    fun deleteKey(key: String) {
        jedis.del(key)
    }
}