@file:Suppress("ReplaceSizeZeroCheckWithIsEmpty", "UnnecessaryVariable", "LocalVariableName", "SpellCheckingInspection")

package space.kiyoshi.hexaecon.commands

import com.iridium.iridiumcolorapi.IridiumColorAPI
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import space.kiyoshi.hexaecon.HexaEcon
import space.kiyoshi.hexaecon.utils.Format
import space.kiyoshi.hexaecon.utils.GetConfig
import space.kiyoshi.hexaecon.utils.Language.bankAmount
import space.kiyoshi.hexaecon.utils.Language.isConsolePlayer
import java.util.concurrent.CompletableFuture

class Eco : CommandExecutor {
    private val dataeconomyvalue = GetConfig.main().getString("MySQL.DataEconomyName")!!
    private val sound = GetConfig.main().getString("Sounds.EcoCommand.Sound")!!
    private val volume = GetConfig.main().getInt("Sounds.EcoCommand.Volume")
    private val pitch = GetConfig.main().getInt("Sounds.EcoCommand.Pitch")
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(Format.hex(Format.color(isConsolePlayer())))
            return true
        }
        val player = sender
        if (command.name == "eco") {
            if (args.size == 0) {
                val future = CompletableFuture.supplyAsync {
                    val stmt = HexaEcon.SQL!!.getconnection()!!.createStatement()
                    val SQL = "SELECT * FROM " + player.name
                    val rs = stmt.executeQuery(SQL)
                    rs.next()
                    val value = rs.getInt(dataeconomyvalue)
                    rs.close()
                    stmt.close()
                    value
                }
                val value = future.get()
                player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(bankAmount().replace("%valuename", dataeconomyvalue).replace("%amount", value.toString())))))
                if(sound != "NONE") {
                    player.playSound(player.location, Sound.valueOf(sound), volume.toFloat(), pitch.toFloat())
                }
            }
        }
        return false
    }
}