@file:Suppress("FunctionName", "LocalVariableName", "SpellCheckingInspection")

package space.kiyoshi.hexaecon.events

import com.iridium.iridiumcolorapi.IridiumColorAPI
import org.bukkit.Sound
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Animals
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import space.kiyoshi.hexaecon.HexaEcon.Companion.plugin
import space.kiyoshi.hexaecon.functions.TableFunctionMongo
import space.kiyoshi.hexaecon.functions.TableFunctionSQL
import space.kiyoshi.hexaecon.utils.Format
import space.kiyoshi.hexaecon.utils.GetConfig
import space.kiyoshi.hexaecon.utils.Language.genericEarn
import java.io.File
import java.io.IOException
import java.sql.SQLException

class AnimalsDeath : Listener {
    @EventHandler
    @Deprecated("")
    fun MonsterDeath_(e: EntityDeathEvent) {
        val dataeconomyvalue = GetConfig.main().getString("DataBase.DataEconomyName")!!
        val eventsonkillmonstersearnamount = GetConfig.main().getInt("Economy.Events.OnKillAnimals.Earn")
        val hasstrikeeffect = GetConfig.main().getBoolean("Economy.Events.OnKillAnimals.StrikeEffect")
        val sound = GetConfig.main().getString("Sounds.OnKillAnimals.Sound")!!
        val volume = GetConfig.main().getInt("Sounds.OnKillAnimals.Volume")
        val pitch = GetConfig.main().getInt("Sounds.OnKillAnimals.Pitch")
        val player = e.entity.killer
        val databasetype = GetConfig.main().getString("DataBase.Type")!!
        if (GetConfig.main().getBoolean("Modules.OnKillAnimals")) {
            if (e.entity is Animals) {
                if (e.entity.killer != null && e.entity.killer is Player) {
                    if (hasstrikeeffect) {
                        e.entity.location.world!!.strikeLightningEffect(e.entity.location)
                    } else {
                        if (sound != "NONE") {
                            player?.playSound(player.location, Sound.valueOf(sound), volume.toFloat(), pitch.toFloat())
                        }
                    }
                    val data_names_sqlite =
                        File(
                            plugin.dataFolder.toString() + "/data/" + e.entity.killer!!
                                .name + "_SQLite.txt"
                        )
                    val data_names_mysql =
                        File(
                            plugin.dataFolder.toString() + "/data/" + e.entity.killer!!
                                .name + "_MySQL.txt"
                        )
                    val data_names_mongodb =
                        File(
                            plugin.dataFolder.toString() + "/data/" + e.entity.killer!!
                                .name + "_MongoDB.txt"
                        )
                    val data_names_config_sqlite: FileConfiguration =
                        YamlConfiguration.loadConfiguration(data_names_sqlite)
                    val data_names_config_mysql: FileConfiguration =
                        YamlConfiguration.loadConfiguration(data_names_mysql)
                    val data_names_config_mongodb: FileConfiguration =
                        YamlConfiguration.loadConfiguration(data_names_mongodb)
                    val somasqlite =
                        data_names_config_sqlite.getInt("data.${dataeconomyvalue}") + eventsonkillmonstersearnamount
                    val somamysql =
                        data_names_config_mysql.getInt("data.${dataeconomyvalue}") + eventsonkillmonstersearnamount
                    val somamongodb =
                        data_names_config_mongodb.getInt("data.${dataeconomyvalue}") + eventsonkillmonstersearnamount
                    try {
                        if (databasetype == "h2") {
                            TableFunctionSQL.dropTableSQLite(e.entity.killer!!)
                        } else if (databasetype == "MongoDB") {
                            TableFunctionMongo.dropCollection(e.entity.killer!!.name)
                        } else if (databasetype == "MySQL") {
                            TableFunctionSQL.dropTable(e.entity.killer!!)
                        }
                    } catch (_: SQLException) {}
                    try {
                        if (databasetype == "h2") {
                            TableFunctionSQL.createTableAmountSQLite(e.entity.killer!!, somasqlite)
                        } else if (databasetype == "MongoDB") {
                            TableFunctionMongo.createCollectionAmount(e.entity.killer!!.name, somamongodb)
                        } else if (databasetype == "MySQL") {
                            TableFunctionSQL.createTableAmount(e.entity.killer!!, somamysql)
                        }
                    } catch (_: SQLException) {}
                    if (databasetype == "h2") {
                        data_names_config_sqlite["data.${dataeconomyvalue}"] = somasqlite
                    } else if (databasetype == "MongoDB") {
                        data_names_config_mongodb["data.${dataeconomyvalue}"] = somamongodb
                    } else if (databasetype == "MySQL") {
                        data_names_config_mysql["data.${dataeconomyvalue}"] = somamysql
                    }
                    try {
                        if (databasetype == "h2") {
                            data_names_config_sqlite.save(data_names_sqlite)
                        } else if (databasetype == "MongoDB") {
                            data_names_config_mongodb.save(data_names_mongodb)
                        } else if (databasetype == "MySQL") {
                            data_names_config_mysql.save(data_names_mysql)
                        }
                    } catch (ignored: IOException) {}
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