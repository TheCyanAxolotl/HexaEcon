/*
 * Copyright (c) 2023. KiyoshiDevelopment
 *   All rights reserved.
 */

@file:Suppress("UnnecessaryVariable")

package space.kiyoshi.hexaecon.functions

import redis.clients.jedis.Jedis
import space.kiyoshi.hexaecon.utils.DataManager

object TableFunctionRedis {

    private val redishost = DataManager.main().getString("Redis.Host")!!
    private val redisport = DataManager.main().getInt("Redis.Port")

    fun createTable(name: String) {
        val tableName = name
        val defaultValue = 0L
        val jedis = Jedis(redishost, redisport)
        if (!jedis.exists(tableName)) {
            jedis.set(tableName, defaultValue.toString())
        }
    }

    fun createTableAmount(name: String, value: Long) {
        val tableName = name
        val jedis = Jedis(redishost, redisport)
        if (!jedis.exists(tableName)) {
            jedis.set(tableName, value.toString())
        }
    }

    fun dropTable(name: String) {
        Jedis(redishost, redisport).del(name)
    }

    @JvmStatic
    fun selectAllFromCollectionAsStringRedis(tableName: String): List<String> {
        val results = mutableListOf<String>()
        try {
            val hashValues = Jedis(redishost, redisport).mget(tableName)
            for (value in hashValues) {
                results.add(value)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return results
    }

}