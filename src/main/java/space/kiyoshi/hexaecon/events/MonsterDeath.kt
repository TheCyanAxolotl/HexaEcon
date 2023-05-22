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
import space.kiyoshi.hexaecon.functions.TableFunctionMongo
import space.kiyoshi.hexaecon.functions.TableFunctionRedis
import space.kiyoshi.hexaecon.functions.TableFunctionSQL
import space.kiyoshi.hexaecon.utils.Economy
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
                        if(sound != "NONE") {
                            player?.playSound(player.location, Sound.valueOf(sound), volume.toFloat(), pitch.toFloat())
                        }
                    }

                    val data_names_sqlite = File(
                        plugin.dataFolder.toString() + "/data/${e.entity.killer!!.name}/" + e.entity.killer!!
                            .name + "_SQLite.txt"
                    )
                    val data_names_mysql = File(
                        plugin.dataFolder.toString() + "/data/${e.entity.killer!!.name}/" + e.entity.killer!!
                            .name + "_MySQL.txt"
                    )
                    val data_names_mongodb = File(
                        plugin.dataFolder.toString() + "/data/${e.entity.killer!!.name}/" + e.entity.killer!!
                            .name + "_MongoDB.txt"
                    )
                    val data_names_redis = File(
                        plugin.dataFolder.toString() + "/data/${e.entity.killer!!.name}/" + e.entity.killer!!
                            .name + "_Redis.txt"
                    )

                    val data_names_config_sqlite: FileConfiguration =
                        YamlConfiguration.loadConfiguration(data_names_sqlite)
                    val data_names_config_mysql: FileConfiguration =
                        YamlConfiguration.loadConfiguration(data_names_mysql)
                    val data_names_config_mongodb: FileConfiguration =
                        YamlConfiguration.loadConfiguration(data_names_mongodb)
                    val data_names_config_redis: FileConfiguration =
                        YamlConfiguration.loadConfiguration(data_names_redis)

                    val somasqlite =
                        data_names_config_sqlite.getLong("data.${dataeconomyvalue}") + eventsonkillmonstersearnamount
                    val somamysql =
                        data_names_config_mysql.getLong("data.${dataeconomyvalue}") + eventsonkillmonstersearnamount
                    val somamongodb =
                        data_names_config_mongodb.getLong("data.${dataeconomyvalue}") + eventsonkillmonstersearnamount
                    val somaredis =
                        data_names_config_redis.getLong("data.${dataeconomyvalue}") + eventsonkillmonstersearnamount

                    try {
                        when (databasetype) {
                            "h2" -> {
                                TableFunctionSQL.dropTableSQLite(e.entity.killer!!)
                            }
                            "MongoDB" -> {
                                TableFunctionMongo.dropCollection(e.entity.killer!!.name)
                            }
                            "MySQL" -> {
                                TableFunctionSQL.dropTable(e.entity.killer!!)
                            }
                            "Redis" -> {
                                TableFunctionRedis.dropTable(e.entity.killer!!.name)
                            }
                        }
                    } catch (_: SQLException) {}
                    try {
                        when (databasetype) {
                            "h2" -> {
                                TableFunctionSQL.createTableAmountSQLite(e.entity.killer!!, somasqlite)
                            }
                            "MongoDB" -> {
                                TableFunctionMongo.createCollectionAmount(e.entity.killer!!.name, somamongodb)
                            }
                            "MySQL" -> {
                                TableFunctionSQL.createTableAmount(e.entity.killer!!, somamysql)
                            }
                            "Redis" -> {
                                TableFunctionRedis.createTableAmount(e.entity.killer!!.name, somaredis)
                            }
                        }
                    } catch (_: SQLException) {}
                    when (databasetype) {
                        "h2" -> {
                            data_names_config_sqlite["data.${dataeconomyvalue}"] = somasqlite
                        }
                        "MongoDB" -> {
                            data_names_config_mongodb["data.${dataeconomyvalue}"] = somamongodb
                        }
                        "MySQL" -> {
                            data_names_config_mysql["data.${dataeconomyvalue}"] = somamysql
                        }
                        "Redis" -> {
                            data_names_config_redis["data.${dataeconomyvalue}"] = somaredis
                        }
                    }
                    try {
                        when (databasetype) {
                            "h2" -> {
                                data_names_config_sqlite.save(data_names_sqlite)
                            }
                            "MongoDB" -> {
                                data_names_config_mongodb.save(data_names_mongodb)
                            }
                            "MySQL" -> {
                                data_names_config_mysql.save(data_names_mysql)
                            }
                            "Redis" -> {
                                data_names_config_redis.save(data_names_redis)
                            }
                        }
                    } catch (_: IOException) {}
                    plugin.reloadConfig()
                    e.entity.killer!!.sendMessage(
                        Format.hex(
                            Format.color(
                                IridiumColorAPI.process(
                                    genericEarn().replace(
                                        "%amount%",
                                        eventsonkillmonstersearnamount.toString()
                                    ).replace("%valuename%", dataeconomyvalue)
                                        .replace("%amountformatted%", Economy.formatBalance(eventsonkillmonstersearnamount.toString()))
                                )
                            )
                        )
                    )
                }
            }
        }
    }
}