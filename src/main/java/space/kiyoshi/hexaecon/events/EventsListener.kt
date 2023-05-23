/*
 * Copyright (c) 2023. KiyoshiDevelopment
 *   All rights reserved.
 */

@file:Suppress("LocalVariableName", "DEPRECATION", "UNUSED_VARIABLE", "SpellCheckingInspection")

package space.kiyoshi.hexaecon.events

import com.iridium.iridiumcolorapi.IridiumColorAPI
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
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

class EventsListener : Listener {
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