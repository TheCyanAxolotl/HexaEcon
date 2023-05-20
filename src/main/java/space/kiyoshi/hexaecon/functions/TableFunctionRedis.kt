@file:Suppress("UnnecessaryVariable")

package space.kiyoshi.hexaecon.functions

import redis.clients.jedis.Jedis
import space.kiyoshi.hexaecon.redis.RedisManager
import space.kiyoshi.hexaecon.utils.GetConfig

object TableFunctionRedis {

    private val redishost = GetConfig.main().getString("Redis.Host")!!
    private val redisport = GetConfig.main().getInt("Redis.Port")
    private val jedis = RedisManager(redishost, redisport)

    @JvmStatic
    fun createTable(name: String) {
        val tableName = name
        val defaultValue = "0"
        if (!Jedis(redishost, redisport).exists(tableName)) {
            jedis.setValue(tableName, defaultValue)
        }
    }

    @JvmStatic
    fun createTableAmount(name: String, value: Int) {
        val tableName = name

        if (!Jedis(redishost, redisport).exists(tableName)) {
            jedis.setValue(tableName, value.toString())
        }
    }

    @JvmStatic
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