/*
 * Copyright (c) 2023. KiyoshiDevelopment
 *   All rights reserved.
 */

package space.kiyoshi.hexaecon.commands

import com.iridium.iridiumcolorapi.IridiumColorAPI
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import space.kiyoshi.hexaecon.functions.TableFunctionMongo
import space.kiyoshi.hexaecon.functions.TableFunctionRedis
import space.kiyoshi.hexaecon.functions.TableFunctionSQL
import space.kiyoshi.hexaecon.functions.TableFunctionSQL.getValueFromSQL
import space.kiyoshi.hexaecon.utils.Economy
import space.kiyoshi.hexaecon.utils.Format
import space.kiyoshi.hexaecon.utils.GetConfig
import space.kiyoshi.hexaecon.utils.Language.accessDenied
import space.kiyoshi.hexaecon.utils.Language.bankAmount
import space.kiyoshi.hexaecon.utils.Language.isConsolePlayer

class EcoCommand : CommandExecutor {
    private val dataeconomyvalue = GetConfig.main().getString("DataBase.DataEconomyName")!!
    private val soundeco = GetConfig.main().getString("Sounds.NoPermission.Sound")!!
    private val volumeeco = GetConfig.main().getInt("Sounds.EcoCommand.Volume")
    private val pitcheco = GetConfig.main().getInt("Sounds.EcoCommand.Pitch")
    private val databasetype = GetConfig.main().getString("DataBase.Type")!!
    private val soundnoperm = GetConfig.main().getString("Sounds.NoPermission.Sound")!!
    private val pitchnoperm = GetConfig.main().getInt("Sounds.NoPermission.Pitch")
    private val volumenoperm = GetConfig.main().getInt("Sounds.NoPermission.Volume")
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(Format.hex(Format.color(isConsolePlayer())))
            return true
        }
        val player = sender
        if (command.name == "eco") {
            if(player.hasPermission("hexaecon.permissions.balance")) {
                if (args.size == 0) {
                    when (databasetype) {
                        "h2" -> {
                            player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(bankAmount().replace("%valuename%", dataeconomyvalue).replace("%amountformatted%", Economy.formatBalance(TableFunctionSQL.selectAllFromTableAsStringSQLite(player.name).toString().replace("[", "").replace("]", ""))).replace("%amount%", TableFunctionSQL.selectAllFromTableAsStringSQLite(player.name).toString().replace("[", "").replace("]", ""))))))
                        }
                        "MongoDB" -> {
                            player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(bankAmount().replace("%valuename%", dataeconomyvalue).replace("%amountformatted%", Economy.formatBalance(TableFunctionMongo.selectAllFromCollectionAsString(player.name).toString().replace("[", "").replace("]", ""))).replace("%amount%", TableFunctionMongo.selectAllFromCollectionAsString(player.name).toString().replace("[", "").replace("]", ""))))))
                        }
                        "MySQL" -> {
                            player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(bankAmount().replace("%valuename%", dataeconomyvalue).replace("%amountformatted%", Economy.formatBalance(getValueFromSQL(player, dataeconomyvalue).toString())).replace("%amount%", getValueFromSQL(player, dataeconomyvalue).toString())))))
                        }
                        "Redis" -> {
                            player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(bankAmount().replace("%valuename%", dataeconomyvalue).replace("%amountformatted%", Economy.formatBalance(TableFunctionRedis.selectAllFromCollectionAsStringRedis(player.name).toString().replace("[", "").replace("]", ""))).replace("%amount%", TableFunctionRedis.selectAllFromCollectionAsStringRedis(player.name).toString())))))
                        }
                    }
                    if (soundeco != "NONE") {
                        player.playSound(player.location, Sound.valueOf(soundeco), volumeeco.toFloat(), pitcheco.toFloat())
                    }
                }
            } else {
                player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(accessDenied().replace("%perm%", "hexaecon.permissions.balance")))))
                if (soundnoperm != "NONE") {
                    player.playSound(player.location, Sound.valueOf(soundnoperm), volumenoperm.toFloat(), pitchnoperm.toFloat())
                }
            }
        }
        return false
    }
}