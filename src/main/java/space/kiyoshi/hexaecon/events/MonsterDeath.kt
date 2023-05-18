@file:Suppress("FunctionName", "LocalVariableName", "SpellCheckingInspection")

package space.kiyoshi.hexaecon.events

import com.iridium.iridiumcolorapi.IridiumColorAPI
import org.bukkit.Sound
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import space.kiyoshi.hexaecon.HexaEcon.Companion.plugin
import space.kiyoshi.hexaecon.functions.TableFunction
import space.kiyoshi.hexaecon.utils.Format
import space.kiyoshi.hexaecon.utils.GetConfig
import space.kiyoshi.hexaecon.utils.Language.genericEarn
import java.io.File
import java.io.IOException
import java.sql.SQLException

class MonsterDeath : Listener {
    @EventHandler
    @Deprecated("")
    fun MonsterDeath_(e: EntityDeathEvent) {
        val dataeconomyvalue = GetConfig.main().getString("DataBase.DataEconomyName")!!
        val eventsonkillmonstersearnamount = GetConfig.main().getInt("Economy.Events.OnKillMonsters.Earn")
        val hasstrikeeffect = GetConfig.main().getBoolean("Economy.Events.OnKillMonsters.StrikeEffect")
        val sound = GetConfig.main().getString("Sounds.OnKillMonsters.Sound")!!
        val volume = GetConfig.main().getInt("Sounds.OnKillMonsters.Volume")
        val pitch = GetConfig.main().getInt("Sounds.OnKillMonsters.Pitch")
        val player = e.entity.killer
        val databasetype = GetConfig.main().getString("DataBase.Type")!!
        if (GetConfig.main().getBoolean("Modules.OnKillMonsters")) {
            if (e.entity is Monster) {
                if (e.entity.killer != null && e.entity.killer is Player) {
                    if (hasstrikeeffect) {
                        e.entity.location.world!!.strikeLightningEffect(e.entity.location)
                    } else {
                        player?.playSound(player.location, Sound.valueOf(sound), volume.toFloat(), pitch.toFloat())
                    }

                    val data_names_sqlite = File(
                        plugin.dataFolder.toString() + "/data/" + e.entity.killer!!
                            .name + "_SQLite.txt"
                    )
                    val data_names_mysql = File(
                        plugin.dataFolder.toString() + "/data/" + e.entity.killer!!
                            .name + "_MySQL.txt"
                    )

                    val data_names_config_sqlite: FileConfiguration =
                        YamlConfiguration.loadConfiguration(data_names_sqlite)
                    val data_names_config_mysql: FileConfiguration =
                        YamlConfiguration.loadConfiguration(data_names_mysql)
                    val somasqlite =
                        data_names_config_sqlite.getInt("data.${dataeconomyvalue}") + eventsonkillmonstersearnamount
                    val somamysql =
                        data_names_config_mysql.getInt("data.${dataeconomyvalue}") + eventsonkillmonstersearnamount
                    try {
                        if (databasetype == "h2") {
                            TableFunction.dropTableSQLite(e.entity.killer!!)
                        } else {
                            TableFunction.dropTable(e.entity.killer!!)
                        }
                    } catch (_: SQLException) {
                    }
                    try {
                        if (databasetype == "h2") {
                            TableFunction.createTableAmountSQLite(e.entity.killer!!, somasqlite)
                        } else {
                            TableFunction.createTableAmount(e.entity.killer!!, somamysql)
                        }
                    } catch (_: SQLException) {
                    }
                    if (databasetype == "h2") {
                        data_names_config_sqlite["data.${dataeconomyvalue}"] = somasqlite
                    } else {
                        data_names_config_mysql["data.${dataeconomyvalue}"] = somamysql
                    }
                    try {
                        if (databasetype == "h2") {
                            data_names_config_sqlite.save(data_names_sqlite)
                        } else {
                            data_names_config_mysql.save(data_names_mysql)
                        }
                    } catch (_: IOException) {
                    }
                    plugin.reloadConfig()
                    e.entity.killer!!.sendMessage(
                        Format.hex(
                            Format.color(
                                IridiumColorAPI.process(
                                    genericEarn().replace(
                                        "%amount",
                                        eventsonkillmonstersearnamount.toString()
                                    ).replace("%valuename", dataeconomyvalue)
                                )
                            )
                        )
                    )
                }
            }
        }
    }
}