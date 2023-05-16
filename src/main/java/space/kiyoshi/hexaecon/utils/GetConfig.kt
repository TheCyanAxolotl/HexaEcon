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
        val dataeconomyvalue = main().getString("MySQL.DataEconomyName")!!
        val data = File(plugin.dataFolder, "data")
        val data_names =
            File(plugin.dataFolder.toString() + "/data/" + player.name + ".txt")
        if (!data.exists()) {
            data.mkdir()
        }
        val data_names_config: FileConfiguration = YamlConfiguration.loadConfiguration(data_names)
        if (!data_names.exists()) {
            try {
                data_names.createNewFile()
                data_names_config.createSection("data")
                if (!data_names_config.isSet("data.${dataeconomyvalue}")) {
                    data_names_config["data.${dataeconomyvalue}"] = 0
                    data_names_config.save(data_names)
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
    }
}