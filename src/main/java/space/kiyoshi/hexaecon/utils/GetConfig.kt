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
        val data_names_sqlite =
            File(plugin.dataFolder.toString() + "/data/" + player.name + "_SQLite.txt")
        val data_names_mysql =
            File(plugin.dataFolder.toString() + "/data/" + player.name + "_MySQL.txt")
        if (!data.exists()) {
            data.mkdir()
        }
        val data_names_config_sqlite: FileConfiguration = YamlConfiguration.loadConfiguration(data_names_sqlite)
        val data_names_config_mysql: FileConfiguration = YamlConfiguration.loadConfiguration(data_names_mysql)
        if(databasetype == "h2") {
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
        } else {
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
        }
    }
}