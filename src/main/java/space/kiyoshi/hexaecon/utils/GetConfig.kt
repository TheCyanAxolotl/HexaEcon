@file:Suppress("LocalVariableName", "SpellCheckingInspection")

package space.kiyoshi.hexaecon.utils

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import space.kiyoshi.hexaecon.HexaEcon.Companion.plugin
import java.io.File
import java.io.IOException

object GetConfig {

    fun main(): FileConfiguration {
        return plugin.config
    }

    fun generatePlayerConfig(player: Player) {
        val databasetype = main().getString("DataBase.Type")!!
        val dataeconomyvalue = main().getString("DataBase.DataEconomyName")!!
        val data = File(plugin.dataFolder, "data")
        val playerfolder = File(plugin.dataFolder, "data/${player.name}/")
        val data_names_sqlite =
            File(plugin.dataFolder.toString() + "/data/${player.name}/" + player.name + "_SQLite.txt")
        val data_names_mysql =
            File(plugin.dataFolder.toString() + "/data/${player.name}/" + player.name + "_MySQL.txt")
        val data_names_mongodb =
            File(plugin.dataFolder.toString() + "/data/${player.name}/" + player.name + "_MongoDB.txt")
        val data_names_redis =
            File(plugin.dataFolder.toString() + "/data/${player.name}/" + player.name + "_Redis.txt")
        if (!data.exists()) {
            data.mkdir()
        }
        if(!playerfolder.exists()) {
            playerfolder.mkdir()
        }
        val data_names_config_sqlite: FileConfiguration = YamlConfiguration.loadConfiguration(data_names_sqlite)
        val data_names_config_mysql: FileConfiguration = YamlConfiguration.loadConfiguration(data_names_mysql)
        val data_names_config_mongodb: FileConfiguration = YamlConfiguration.loadConfiguration(data_names_mongodb)
        val data_names_config_redis: FileConfiguration = YamlConfiguration.loadConfiguration(data_names_redis)
        if (databasetype == "h2") {
            if (!data_names_sqlite.exists()) {
                try {
                    data_names_sqlite.createNewFile()
                    data_names_config_sqlite.createSection("data")
                    if (!data_names_config_sqlite.isSet("data.${dataeconomyvalue}")) {
                        data_names_config_sqlite["data.${dataeconomyvalue}"] = 0
                        data_names_config_sqlite.save(data_names_sqlite)
                    }
                } catch (_: IOException) {}
            }
        } else if (databasetype == "MongoDB") {
            if (!data_names_mongodb.exists()) {
                try {
                    data_names_mongodb.createNewFile()
                    data_names_config_mongodb.createSection("data")
                    if (!data_names_config_mongodb.isSet("data.${dataeconomyvalue}")) {
                        data_names_config_mongodb["data.${dataeconomyvalue}"] = 0
                        data_names_config_mongodb.save(data_names_mongodb)
                    }
                } catch (_: IOException) {}
            }
        } else if (databasetype == "MySQL") {
            if (!data_names_mysql.exists()) {
                try {
                    data_names_mysql.createNewFile()
                    data_names_config_mysql.createSection("data")
                    if (!data_names_config_mysql.isSet("data.${dataeconomyvalue}")) {
                        data_names_config_mysql["data.${dataeconomyvalue}"] = 0
                        data_names_config_mysql.save(data_names_mysql)
                    }
                } catch (_: IOException) {}
            }
        } else if (databasetype == "Redis") {
            if (!data_names_redis.exists()) {
                try {
                    data_names_redis.createNewFile()
                    data_names_config_redis.createSection("data")
                    if (!data_names_config_redis.isSet("data.${dataeconomyvalue}")) {
                        data_names_config_redis["data.${dataeconomyvalue}"] = 0
                        data_names_config_redis.save(data_names_redis)
                    }
                } catch (_: IOException) {}
            }
        }
    }

    fun deletePlayerConfig(player: Player) {
        val dataFolder = File(plugin.dataFolder, "data")
        val playerFolder = File(plugin.dataFolder, "data/${player.name}")

        if (playerFolder.exists()) {
            playerFolder.deleteRecursively()
        }

        if (dataFolder.exists() && dataFolder.listFiles()!!.isEmpty()) {
            dataFolder.delete()
        }
    }
}