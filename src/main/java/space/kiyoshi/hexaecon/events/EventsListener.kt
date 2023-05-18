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
import space.kiyoshi.hexaecon.functions.TableFunction
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

        val item: ItemStack = event.player.inventory.itemInHand
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
                        HexaEcon.plugin.dataFolder.toString() + "/data/" + player
                            .name + "_SQLite.txt"
                    )
                    val data_names_mysql = File(
                        HexaEcon.plugin.dataFolder.toString() + "/data/" + player
                            .name + "_MySQL.txt"
                    )
                    val data_names_config_sqlite: FileConfiguration =
                        YamlConfiguration.loadConfiguration(data_names_sqlite)
                    val data_names_config_mysql: FileConfiguration =
                        YamlConfiguration.loadConfiguration(data_names_mysql)
                    val somasqlite = data_names_config_sqlite.getInt("data.${dataeconomyvalue}") + amount!!
                    val somamysql = data_names_config_mysql.getInt("data.${dataeconomyvalue}") + amount
                    try {
                        if (databasetype == "h2") {
                            TableFunction.dropTableSQLite(player)
                        } else {
                            TableFunction.dropTable(player)
                        }
                    } catch (_: SQLException) {
                    }
                    try {
                        if (databasetype == "h2") {
                            TableFunction.createTableAmountSQLite(player, somasqlite)
                        } else {
                            TableFunction.createTableAmount(player, somamysql)
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
                    HexaEcon.plugin.reloadConfig()
                    if (player.inventory.itemInHand.amount > 1) {
                        player.inventory.itemInHand.amount = player.inventory.itemInHand.amount - amount
                    } else {
                        player.inventory.setItemInHand(null)
                    }
                    player.sendMessage(
                        Format.hex(
                            Format.color(
                                IridiumColorAPI.process(
                                    genericEarn().replace(
                                        "%amount",
                                        amount.toString()
                                    ).replace("%valuename", dataeconomyvalue)
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