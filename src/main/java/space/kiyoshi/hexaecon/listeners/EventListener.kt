/*
 * Copyright (c) 2023. KiyoshiDevelopment
 *   All rights reserved.
 */

@file:Suppress("LocalVariableName", "DEPRECATION", "UNUSED_VARIABLE", "SpellCheckingInspection")

package space.kiyoshi.hexaecon.listeners

import com.iridium.iridiumcolorapi.IridiumColorAPI
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Animals
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import space.kiyoshi.hexaecon.HexaEcon
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

class EventListener : Listener {
    private val itemdisplayname = GetConfig.main().getString("Economy.Physical.DisplayName")!!
    private val itemtype = GetConfig.main().getString("Economy.Physical.Item")!!
    private val dataeconomyvalue = GetConfig.main().getString("DataBase.DataEconomyName")!!
    private val soundonpickup = GetConfig.main().getString("Sounds.OnPlayerSneakPickupEcon.Sound")!!
    private val volumeonpickup = GetConfig.main().getInt("Sounds.OnPlayerSneakPickupEcon.Volume")
    private val pitchonpickup = GetConfig.main().getInt("Sounds.OnPlayerSneakPickupEcon.Pitch")
    private val soundoninteract = GetConfig.main().getString("Sounds.OnPlayerInteractWithEcon.Sound")!!
    private val volumeoninteract = GetConfig.main().getInt("Sounds.OnPlayerInteractWithEcon.Volume")
    private val pitchoninteract = GetConfig.main().getInt("Sounds.OnPlayerInteractWithEcon.Pitch")
    private val databasetype = GetConfig.main().getString("DataBase.Type")!!
    private val texture = GetConfig.main().getString("TextureManager.Texture")!!
    private val hastextureenabled = GetConfig.main().getBoolean("TextureManager.Enabled")

    @EventHandler
    fun onPlaceEvent(event: BlockPlaceEvent) {

        val item: ItemStack = event.player.inventory.itemInMainHand
        if (item.type == Material.valueOf(itemtype) && item.itemMeta?.displayName == Format.hex(
                Format.color(
                    itemdisplayname
                )
            )
        ) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        try {
            when (databasetype) {
                "h2" -> {
                    TableFunctionSQL.createTableSQLite(event.player)
                }
                "MongoDB" -> {
                    TableFunctionMongo.createCollection(event.player.name)
                }
                "MySQL" -> {
                    TableFunctionSQL.createTable(event.player)
                }
                "Redis" -> {
                    TableFunctionRedis.createTable(event.player.name)
                }
            }
        } catch (_: SQLException) {}
        GetConfig.generatePlayerConfig(event.player)
        if (hastextureenabled) {
            event.player.setTexturePack(texture)
        }
    }

    @EventHandler
    fun animalsDeath(e: EntityDeathEvent) {
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
                            HexaEcon.plugin.dataFolder.toString() + "/data/${e.entity.killer!!.name}/" + e.entity.killer!!
                                .name + "_SQLite.txt"
                        )
                    val data_names_mysql =
                        File(
                            HexaEcon.plugin.dataFolder.toString() + "/data/${e.entity.killer!!.name}/" + e.entity.killer!!
                                .name + "_MySQL.txt"
                        )
                    val data_names_mongodb =
                        File(
                            HexaEcon.plugin.dataFolder.toString() + "/data/${e.entity.killer!!.name}/" + e.entity.killer!!
                                .name + "_MongoDB.txt"
                        )
                    val data_names_redis =
                        File(
                            HexaEcon.plugin.dataFolder.toString() + "/data/${e.entity.killer!!.name}/" + e.entity.killer!!
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
                    } catch (ignored: IOException) {}
                    HexaEcon.plugin.reloadConfig()
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

    @EventHandler
    @Deprecated("")
    fun monstersDeath(e: EntityDeathEvent) {
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
                        HexaEcon.plugin.dataFolder.toString() + "/data/${e.entity.killer!!.name}/" + e.entity.killer!!
                            .name + "_SQLite.txt"
                    )
                    val data_names_mysql = File(
                        HexaEcon.plugin.dataFolder.toString() + "/data/${e.entity.killer!!.name}/" + e.entity.killer!!
                            .name + "_MySQL.txt"
                    )
                    val data_names_mongodb = File(
                        HexaEcon.plugin.dataFolder.toString() + "/data/${e.entity.killer!!.name}/" + e.entity.killer!!
                            .name + "_MongoDB.txt"
                    )
                    val data_names_redis = File(
                        HexaEcon.plugin.dataFolder.toString() + "/data/${e.entity.killer!!.name}/" + e.entity.killer!!
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
                    HexaEcon.plugin.reloadConfig()
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

    @EventHandler
    fun onPickupCoinEvent(event: PlayerPickupItemEvent) {
        val player = event.player
        val inventory: PlayerInventory = event.player.inventory
        val item = event.item.itemStack
        if (item.type == Material.valueOf(itemtype) && item.itemMeta?.displayName == Format.hex(
                Format.color(
                    itemdisplayname
                )
            )
        ) {
            if (!(player.isSneaking)) {
                event.isCancelled = true
            } else {
                if (soundonpickup != "NONE") {
                    player.playSound(
                        player.location,
                        Sound.valueOf(soundonpickup),
                        volumeonpickup.toFloat(),
                        pitchonpickup.toFloat()
                    )
                }
            }
        }
    }

    @EventHandler
    fun onItemClickEvent(event: PlayerInteractEvent) {
        val player = event.player
        val item = event.item
        val amount = item?.amount
        val action = event.action
        if (item?.type == Material.valueOf(itemtype) && item.itemMeta?.displayName == Format.hex(
                Format.color(
                    itemdisplayname
                )
            )
        ) {
            if (player.isSneaking) {
                event.isCancelled = true
                if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                    val data_names_sqlite = File(
                        HexaEcon.plugin.dataFolder.toString() + "/data/${player.name}/" + player
                            .name + "_SQLite.txt"
                    )
                    val data_names_mysql = File(
                        HexaEcon.plugin.dataFolder.toString() + "/data/${player.name}/" + player
                            .name + "_MySQL.txt"
                    )
                    val data_names_mongodb = File(
                        HexaEcon.plugin.dataFolder.toString() + "/data/${player.name}/" + player
                            .name + "_MongoDB.txt"
                    )
                    val data_names_redis = File(
                        HexaEcon.plugin.dataFolder.toString() + "/data/${player.name}/" + player
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

                    val somasqlite = data_names_config_sqlite.getLong("data.${dataeconomyvalue}") + amount!!
                    val somamysql = data_names_config_mysql.getLong("data.${dataeconomyvalue}") + amount
                    val somamongodb = data_names_config_mongodb.getLong("data.${dataeconomyvalue}") + amount
                    val somaredis = data_names_config_redis.getLong("data.${dataeconomyvalue}") + amount

                    try {
                        when (databasetype) {
                            "h2" -> {
                                TableFunctionSQL.dropTableSQLite(player)
                            }
                            "MongoDB" -> {
                                TableFunctionMongo.dropCollection(player.name)
                            }
                            "MySQL" -> {
                                TableFunctionSQL.dropTable(player)
                            }
                            "Redis" -> {
                                TableFunctionRedis.dropTable(player.name)
                            }
                        }
                    } catch (_: SQLException) {}
                    try {
                        when (databasetype) {
                            "h2" -> {
                                TableFunctionSQL.createTableAmountSQLite(player, somasqlite)
                            }
                            "MongoDB" -> {
                                TableFunctionMongo.createCollectionAmount(player.name, somamongodb)
                            }
                            "MySQL" -> {
                                TableFunctionSQL.createTableAmount(player, somamysql)
                            }
                            "Redis" -> {
                                TableFunctionRedis.createTableAmount(player.name, somaredis)
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
                    HexaEcon.plugin.reloadConfig()
                    Economy.removeEconomyFromHand(player, amount)
                    player.sendMessage(
                        Format.hex(
                            Format.color(
                                IridiumColorAPI.process(
                                    genericEarn().replace(
                                        "%amount%",
                                        amount.toString()
                                    ).replace("%valuename%", dataeconomyvalue)
                                        .replace("%amountformatted%", Economy.formatBalance(amount.toString()))
                                )
                            )
                        )
                    )
                    if (soundoninteract != "NONE") {
                        player.playSound(
                            player.location,
                            Sound.valueOf(soundoninteract),
                            volumeoninteract.toFloat(),
                            pitchoninteract.toFloat()
                        )
                    }
                }
            }
        }
    }
}