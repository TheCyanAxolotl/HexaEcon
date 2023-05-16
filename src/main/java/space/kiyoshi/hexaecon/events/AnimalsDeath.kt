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
import space.kiyoshi.hexaecon.functions.TableFunction
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
        val dataeconomyvalue = GetConfig.main().getString("MySQL.DataEconomyName")!!
        val eventsonkillmonstersearnamount = GetConfig.main().getInt("Economy.Events.OnKillAnimals.Earn")
        val hasstrikeeffect = GetConfig.main().getBoolean("Economy.Events.OnKillAnimals.StrikeEffect")
        val sound = GetConfig.main().getString("Sounds.OnKillAnimals.Sound")!!
        val volume = GetConfig.main().getInt("Sounds.OnKillAnimals.Volume")
        val pitch = GetConfig.main().getInt("Sounds.OnKillAnimals.Pitch")
        val player = e.entity.killer
        if(GetConfig.main().getBoolean("Modules.OnKillAnimals")){
            if (e.entity is Animals) {
                if (e.entity.killer != null && e.entity.killer is Player) {
                    if(hasstrikeeffect) {
                        e.entity.location.world!!.strikeLightningEffect(e.entity.location)
                    } else {
                        if(sound != "NONE") {
                            player?.playSound(player.location, Sound.valueOf(sound), volume.toFloat(), pitch.toFloat())
                        }
                    }
                    val data_names = File(
                            plugin.dataFolder.toString() + "/data/" + e.entity.killer!!
                                    .name + ".txt"
                    )
                    val data_names_config: FileConfiguration = YamlConfiguration.loadConfiguration(data_names)
                    val soma = data_names_config.getInt("data.${dataeconomyvalue}") + eventsonkillmonstersearnamount
                    try {
                        TableFunction.dropTable(e.entity.killer!!)
                    } catch (ignored: SQLException) {
                    }
                    try {
                        TableFunction.createTableAmount(e.entity.killer!!, soma)
                    } catch (ignored: SQLException) {
                    }
                    data_names_config["data.${dataeconomyvalue}"] = soma
                    try {
                        data_names_config.save(data_names)
                    } catch (ignored: IOException) {
                    }
                    plugin.reloadConfig()
                    e.entity.killer!!.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(genericEarn().replace("%amount", eventsonkillmonstersearnamount.toString()).replace("%valuename", dataeconomyvalue)))))
                }
            }
        }
    }
}