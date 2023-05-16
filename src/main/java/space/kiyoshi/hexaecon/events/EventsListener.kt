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

class EventsListener: Listener {
    private val itemdisplayname = GetConfig.main().getString("Economy.Physical.DisplayName")!!
    private val itemtype = GetConfig.main().getString("Economy.Physical.Item")!!
    private val dataeconomyvalue = GetConfig.main().getString("MySQL.DataEconomyName")!!
    private val soundonpickup = GetConfig.main().getString("Sounds.OnPlayerSneakPickupEcon.Sound")!!
    private val volumeonpickup = GetConfig.main().getInt("Sounds.OnPlayerSneakPickupEcon.Volume")
    private val pitchonpickup = GetConfig.main().getInt("Sounds.OnPlayerSneakPickupEcon.Pitch")
    private val soundoninteract = GetConfig.main().getString("Sounds.OnPlayerInteractWithEcon.Sound")!!
    private val volumeoninteract = GetConfig.main().getInt("Sounds.OnPlayerInteractWithEcon.Volume")
    private val pitchoninteract = GetConfig.main().getInt("Sounds.OnPlayerInteractWithEcon.Pitch")
    @EventHandler
    fun onPlaceEvent(event: BlockPlaceEvent) {

        val item : ItemStack = event.player.inventory.itemInMainHand
        if(item.type == Material.valueOf(itemtype) && item.itemMeta?.displayName == Format.hex(Format.color(itemdisplayname))
        ){
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPickupCoinEvent(event: PlayerPickupItemEvent) {
        val player = event.player
        val inventory: PlayerInventory = event.player.inventory
        val item = event.item.itemStack
        if (item.type == Material.valueOf(itemtype) && item.itemMeta?.displayName == Format.hex(Format.color(itemdisplayname))){
            if(!(player.isSneaking)){
                event.isCancelled = true
            } else {
                if(soundonpickup != "NONE"){
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
        if (item?.type == Material.valueOf(itemtype) && item.itemMeta?.displayName == Format.hex(Format.color(itemdisplayname))){
            if(player.isSneaking){
                event.isCancelled = true
                if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK){
                    val data_names = File(
                        HexaEcon.plugin.dataFolder.toString() + "/data/" + player
                            .name + ".txt"
                    )
                    val data_names_config: FileConfiguration = YamlConfiguration.loadConfiguration(data_names)
                    val soma = data_names_config.getInt("data.${dataeconomyvalue}") + amount!!
                    try {
                        TableFunction.dropTable(player)
                    } catch (ignored: SQLException) {
                    }
                    try {
                        TableFunction.createTableAmount(player, soma)
                    } catch (ignored: SQLException) {
                    }
                    data_names_config["data.${dataeconomyvalue}"] = soma
                    try {
                        data_names_config.save(data_names)
                    } catch (ignored: IOException) {
                    }
                    HexaEcon.plugin.reloadConfig()
                    if (player.inventory.itemInMainHand.amount > 1) {
                        player.inventory.itemInMainHand.amount = player.inventory.itemInMainHand.amount - amount
                    } else {
                        player.inventory.setItemInMainHand(null)
                    }
                    player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(genericEarn().replace("%amount", amount.toString()).replace("%valuename", dataeconomyvalue)))))
                    if(soundoninteract != "NONE") {
                        player.playSound(player.location, Sound.valueOf(soundoninteract), volumeoninteract.toFloat(), pitchoninteract.toFloat())
                    }
                }
            }
        }
    }
}