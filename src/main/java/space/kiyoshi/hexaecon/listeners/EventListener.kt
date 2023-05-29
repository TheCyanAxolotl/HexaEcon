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
import space.kiyoshi.hexaecon.HexaEcon.Companion.plugin
import space.kiyoshi.hexaecon.functions.TableFunctionMongo
import space.kiyoshi.hexaecon.functions.TableFunctionRedis
import space.kiyoshi.hexaecon.functions.TableFunctionSQL
import space.kiyoshi.hexaecon.utils.Economy
import space.kiyoshi.hexaecon.utils.Format
import space.kiyoshi.hexaecon.utils.DataManager
import space.kiyoshi.hexaecon.utils.Language.genericEarn
import java.io.File
import java.sql.SQLException

class EventListener : Listener {
    private val itemdisplayname = DataManager.main().getString("Economy.Physical.DisplayName")!!
    private val itemtype = DataManager.main().getString("Economy.Physical.Item")!!
    private val dataeconomyvalue = DataManager.main().getString("DataBase.DataEconomyName")!!
    private val soundonpickup = DataManager.main().getString("Sounds.OnPlayerSneakPickupEcon.Sound")!!
    private val volumeonpickup = DataManager.main().getInt("Sounds.OnPlayerSneakPickupEcon.Volume")
    private val pitchonpickup = DataManager.main().getInt("Sounds.OnPlayerSneakPickupEcon.Pitch")
    private val soundoninteract = DataManager.main().getString("Sounds.OnPlayerInteractWithEcon.Sound")!!
    private val volumeoninteract = DataManager.main().getInt("Sounds.OnPlayerInteractWithEcon.Volume")
    private val pitchoninteract = DataManager.main().getInt("Sounds.OnPlayerInteractWithEcon.Pitch")
    private val databasetype = DataManager.main().getString("DataBase.Type")!!
    private val texture = DataManager.main().getString("TextureManager.Texture")!!
    private val hastextureenabled = DataManager.main().getBoolean("TextureManager.Enabled")

    @EventHandler
    fun onPlaceEvent(event: BlockPlaceEvent) {

        val item: ItemStack = event.player.inventory.itemInMainHand
        if (item.type == Material.valueOf(itemtype) && item.itemMeta?.displayName == Format.hex(Format.color(itemdisplayname))) {
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
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        DataManager.generatePlayerConfig(event.player)
        if (hastextureenabled) {
            event.player.setTexturePack(texture)
        }
    }

    @EventHandler
    fun onMobsDeath(event: EntityDeathEvent) {
        val config = DataManager.main()
        val economyName = config.getString("DataBase.DataEconomyName")!!
        val animalsEarnAmount = config.getInt("Economy.Events.OnKillAnimals.Earn")
        val monstersEarnAmount = config.getInt("Economy.Events.OnKillMonsters.Earn")
        val hasStrikeEffectAnimals = config.getBoolean("Economy.Events.OnKillAnimals.StrikeEffect")
        val hasStrikeEffectMonsters = config.getBoolean("Economy.Events.OnKillMonsters.StrikeEffect")
        val soundAnimals = config.getString("Sounds.OnKillAnimals.Sound")!!
        val soundMonsters = config.getString("Sounds.OnKillMonsters.Sound")!!
        val soundGenericMobs = config.getString("Sounds.OnKillGenericMobs.Sound")!!
        val volumeAnimals = config.getInt("Sounds.OnKillAnimals.Volume")
        val volumeMonsters = config.getInt("Sounds.OnKillMonsters.Volume")
        val volumeGenericMobs = config.getInt("Sounds.OnKillGenericMobs.Volume")
        val pitchAnimals = config.getInt("Sounds.OnKillAnimals.Pitch")
        val pitchMonsters = config.getInt("Sounds.OnKillMonsters.Pitch")
        val pitchGenericMobs = config.getInt("Sounds.OnKillGenericMobs.Pitch")
        val customMobModule = config.getBoolean("CustomModules.OnKillGenericMobs")
        val moduleanimals = config.getBoolean("GenericModules.OnKillAnimals")
        val modulemonsters = config.getBoolean("GenericModules.OnKillMonsters")
        val genericmodule = config.getBoolean("CustomModules.OnKillGenericMobs")

        val entity = event.entity
        val killer = entity.killer

        if (killer != null) {
            val dataFile = File(plugin.dataFolder, "data/${killer.name}/${killer.name}_SQLite.txt")
            val dataConfig: FileConfiguration = YamlConfiguration.loadConfiguration(dataFile)

            if (customMobModule) {
                val entityType = entity.type.toString()
                if (getMonsterData(entityType).first) {
                    if (soundGenericMobs != "NONE") {
                        killer.playSound(killer.location, Sound.valueOf(soundGenericMobs), volumeGenericMobs.toFloat(), pitchGenericMobs.toFloat())
                    }
                    val earnedAmount = getMonsterData(entityType).second
                    val currentAmount = dataConfig.getLong("data.$dataeconomyvalue") + earnedAmount
                    dataConfig["data.$dataeconomyvalue"] = currentAmount
                    dataConfig.save(dataFile)
                    when (databasetype) {
                        "h2" -> {
                            TableFunctionSQL.dropTableSQLite(event.entity.killer!!)
                            TableFunctionSQL.createTableAmountSQLite(event.entity.killer!!, currentAmount)
                        }
                        "MongoDB" -> {
                            TableFunctionMongo.dropCollection(event.entity.killer!!.name)
                            TableFunctionMongo.createCollectionAmount(event.entity.killer!!.name, currentAmount)
                        }
                        "MySQL" -> {
                            TableFunctionSQL.dropTable(event.entity.killer!!)
                            TableFunctionSQL.createTableAmount(event.entity.killer!!, currentAmount)
                        }
                        "Redis" -> {
                            TableFunctionRedis.dropTable(event.entity.killer!!.name)
                            TableFunctionRedis.createTableAmount(event.entity.killer!!.name, currentAmount)
                        }
                    }
                    plugin.reloadConfig()

                    val formattedEarnedAmount = Economy.formatBalance(earnedAmount.toString())
                    val message = Format.hex(
                        Format.color(
                            IridiumColorAPI.process(
                                genericEarn().replace("%amount%", earnedAmount.toString())
                                    .replace("%valuename%", dataeconomyvalue)
                                    .replace("%amountformatted%", formattedEarnedAmount)
                            )
                        )
                    )
                    killer.sendMessage(message)
                }
            } else {
                if (moduleanimals) {
                    config.set("CustomModules.OnKillGenericMobs", false)
                    plugin.saveConfig()
                    if (entity is Animals) {
                        if (hasStrikeEffectAnimals) {
                            entity.location.world!!.strikeLightningEffect(entity.location)
                        } else {
                            if (soundAnimals != "NONE") {
                                killer.playSound(killer.location, Sound.valueOf(soundAnimals), volumeAnimals.toFloat(), pitchAnimals.toFloat())
                            }
                        }

                        val currentAmount = dataConfig.getLong("data.$economyName") + animalsEarnAmount
                        dataConfig["data.$economyName"] = currentAmount
                        dataConfig.save(dataFile)
                        when (databasetype) {
                            "h2" -> {
                                TableFunctionSQL.dropTableSQLite(event.entity.killer!!)
                                TableFunctionSQL.createTableAmountSQLite(event.entity.killer!!, currentAmount)
                            }

                            "MongoDB" -> {
                                TableFunctionMongo.dropCollection(event.entity.killer!!.name)
                                TableFunctionMongo.createCollectionAmount(event.entity.killer!!.name, currentAmount)
                            }

                            "MySQL" -> {
                                TableFunctionSQL.dropTable(event.entity.killer!!)
                                TableFunctionSQL.createTableAmount(event.entity.killer!!, currentAmount)
                            }

                            "Redis" -> {
                                TableFunctionRedis.dropTable(event.entity.killer!!.name)
                                TableFunctionRedis.createTableAmount(event.entity.killer!!.name, currentAmount)
                            }
                        }

                        plugin.reloadConfig()

                        val formattedEarnedAmount = Economy.formatBalance(animalsEarnAmount.toString())
                        val message = Format.hex(
                            Format.color(
                                IridiumColorAPI.process(
                                    genericEarn().replace("%amount%", animalsEarnAmount.toString())
                                        .replace("%valuename%", economyName)
                                        .replace("%amountformatted%", formattedEarnedAmount)
                                )
                            )
                        )
                        killer.sendMessage(message)
                    }
                }
                if (entity is Monster) {
                    if (modulemonsters) {
                        config.set("CustomModules.OnKillGenericMobs", false)
                        plugin.saveConfig()
                        if (hasStrikeEffectMonsters) {
                            entity.location.world!!.strikeLightningEffect(entity.location)
                        } else {
                            if (soundMonsters != "NONE") {
                                killer.playSound(killer.location, Sound.valueOf(soundMonsters), volumeMonsters.toFloat(), pitchMonsters.toFloat())
                            }
                        }

                        val currentAmount = dataConfig.getLong("data.$economyName") + monstersEarnAmount
                        dataConfig["data.$economyName"] = currentAmount
                        dataConfig.save(dataFile)
                        when (databasetype) {
                            "h2" -> {
                                TableFunctionSQL.dropTableSQLite(event.entity.killer!!)
                                TableFunctionSQL.createTableAmountSQLite(event.entity.killer!!, currentAmount)
                            }
                            "MongoDB" -> {
                                TableFunctionMongo.dropCollection(event.entity.killer!!.name)
                                TableFunctionMongo.createCollectionAmount(event.entity.killer!!.name, currentAmount)
                            }
                            "MySQL" -> {
                                TableFunctionSQL.dropTable(event.entity.killer!!)
                                TableFunctionSQL.createTableAmount(event.entity.killer!!, currentAmount)
                            }
                            "Redis" -> {
                                TableFunctionRedis.dropTable(event.entity.killer!!.name)
                                TableFunctionRedis.createTableAmount(event.entity.killer!!.name, currentAmount)
                            }
                        }

                        plugin.reloadConfig()

                        val formattedEarnedAmount = Economy.formatBalance(monstersEarnAmount.toString())
                        val message = Format.hex(
                            Format.color(
                                IridiumColorAPI.process(
                                    genericEarn().replace("%amount%", monstersEarnAmount.toString())
                                        .replace("%valuename%", economyName)
                                        .replace("%amountformatted%", formattedEarnedAmount)
                                )
                            )
                        )
                        killer.sendMessage(message)
                    }
                }
            }
        }
    }



    @EventHandler
    fun onPickupCoinEvent(event: PlayerPickupItemEvent) {
        val player = event.player
        val inventory: PlayerInventory = event.player.inventory
        val item = event.item.itemStack
        if (item.type == Material.valueOf(itemtype) && item.itemMeta?.displayName == Format.hex(Format.color(itemdisplayname))) {
            if (!(player.isSneaking)) {
                event.isCancelled = true
            } else {
                if (soundonpickup != "NONE") {
                    player.playSound(player.location, Sound.valueOf(soundonpickup), volumeonpickup.toFloat(), pitchonpickup.toFloat())
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
        if (item?.type == Material.valueOf(itemtype) && item.itemMeta?.displayName == Format.hex(Format.color(itemdisplayname))) {
            if (player.isSneaking) {
                event.isCancelled = true
                if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                    val data_names_sqlite = File(plugin.dataFolder.toString() + "/data/${player.name}/" + player.name + "_SQLite.txt")
                    val data_names_mysql = File(plugin.dataFolder.toString() + "/data/${player.name}/" + player.name + "_MySQL.txt")
                    val data_names_mongodb = File(plugin.dataFolder.toString() + "/data/${player.name}/" + player.name + "_MongoDB.txt")
                    val data_names_redis = File(plugin.dataFolder.toString() + "/data/${player.name}/" + player.name + "_Redis.txt")
                    val data_names_config_sqlite: FileConfiguration = YamlConfiguration.loadConfiguration(data_names_sqlite)
                    val data_names_config_mysql: FileConfiguration = YamlConfiguration.loadConfiguration(data_names_mysql)
                    val data_names_config_mongodb: FileConfiguration = YamlConfiguration.loadConfiguration(data_names_mongodb)
                    val data_names_config_redis: FileConfiguration = YamlConfiguration.loadConfiguration(data_names_redis)
                    val somasqlite = data_names_config_sqlite.getLong("data.${dataeconomyvalue}") + amount!!
                    val somamysql = data_names_config_mysql.getLong("data.${dataeconomyvalue}") + amount
                    val somamongodb = data_names_config_mongodb.getLong("data.${dataeconomyvalue}") + amount
                    val somaredis = data_names_config_redis.getLong("data.${dataeconomyvalue}") + amount
                    try {
                        when (databasetype) {
                            "h2" -> {
                                TableFunctionSQL.dropTableSQLite(player)
                                TableFunctionSQL.createTableAmountSQLite(player, somasqlite)
                                data_names_config_sqlite["data.${dataeconomyvalue}"] = somasqlite
                                data_names_config_sqlite.save(data_names_sqlite)
                            }
                            "MongoDB" -> {
                                TableFunctionMongo.dropCollection(player.name)
                                TableFunctionMongo.createCollectionAmount(player.name, somamongodb)
                                data_names_config_mongodb["data.${dataeconomyvalue}"] = somamongodb
                                data_names_config_mongodb.save(data_names_mongodb)
                            }
                            "MySQL" -> {
                                TableFunctionSQL.dropTable(player)
                                TableFunctionSQL.createTableAmount(player, somamysql)
                                data_names_config_mysql["data.${dataeconomyvalue}"] = somamysql
                                data_names_config_mysql.save(data_names_mysql)
                            }
                            "Redis" -> {
                                TableFunctionRedis.dropTable(player.name)
                                TableFunctionRedis.createTableAmount(player.name, somaredis)
                                data_names_config_redis["data.${dataeconomyvalue}"] = somaredis
                                data_names_config_redis.save(data_names_redis)
                            }
                        }
                    } catch (e: SQLException) {
                        e.printStackTrace()
                    }
                    plugin.reloadConfig()
                    Economy.removeEconomyFromHand(player, amount)
                    player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(genericEarn().replace("%amount%", amount.toString()).replace("%valuename%", dataeconomyvalue).replace("%amountformatted%", Economy.formatBalance(amount.toString()))))))
                    if (soundoninteract != "NONE") {
                        player.playSound(player.location, Sound.valueOf(soundoninteract), volumeoninteract.toFloat(), pitchoninteract.toFloat())
                    }
                }
            }
        }
    }

    private fun getMonsterData(mob: String): Pair<Boolean, Int> {
        val customMobsFile = File("${plugin.dataFolder}/modules/custommobs.yml")
        val customMobsConfig = YamlConfiguration.loadConfiguration(customMobsFile)

        val enabled = customMobsConfig.getBoolean("Mobs.${mob}.enabled", false)
        val earn = customMobsConfig.getInt("Mobs.${mob}.earn", 0)

        return enabled to earn
    }
}