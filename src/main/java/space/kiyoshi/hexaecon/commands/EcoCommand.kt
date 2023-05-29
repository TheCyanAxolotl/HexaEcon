/*
 * Copyright (c) 2023. KiyoshiDevelopment
 *   All rights reserved.
 */

package space.kiyoshi.hexaecon.commands

import com.iridium.iridiumcolorapi.IridiumColorAPI
import org.bukkit.Bukkit
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
import space.kiyoshi.hexaecon.utils.DataManager
import space.kiyoshi.hexaecon.utils.Language
import space.kiyoshi.hexaecon.utils.Language.accessDenied
import space.kiyoshi.hexaecon.utils.Language.bankAmount
import space.kiyoshi.hexaecon.utils.Language.bankAmountOthers
import space.kiyoshi.hexaecon.utils.Language.isConsolePlayer

class EcoCommand : CommandExecutor {
    private val dataeconomyvalue = DataManager.main().getString("DataBase.DataEconomyName")!!
    private val soundeco = DataManager.main().getString("Sounds.EcoCommand.Sound")!!
    private val volumeeco = DataManager.main().getInt("Sounds.EcoCommand.Volume")
    private val pitcheco = DataManager.main().getInt("Sounds.EcoCommand.Pitch")
    private val databasetype = DataManager.main().getString("DataBase.Type")!!
    private val soundnoperm = DataManager.main().getString("Sounds.NoPermission.Sound")!!
    private val pitchnoperm = DataManager.main().getInt("Sounds.NoPermission.Pitch")
    private val volumenoperm = DataManager.main().getInt("Sounds.NoPermission.Volume")
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(Format.hex(Format.color(isConsolePlayer())))
            return true
        }
        val player = sender
        if (command.name == "eco") {
            if (player.hasPermission("hexaecon.permissions.balance")) {
                if (args.isEmpty()) {
                    displayBalance(player, player)
                } else {
                    val targetPlayer = Bukkit.getPlayer(args[0])
                    if (targetPlayer != null && targetPlayer.isOnline) {
                        displayBalanceOthers(player, targetPlayer)
                    } else {
                        player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(Language.playerNotFound().replace("%p%", args[0])))))
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

    private fun displayBalance(sender: Player, targetPlayer: Player) {
        val balance: String = when (databasetype) {
            "h2" -> {
                Economy.formatBalance(TableFunctionSQL.selectAllFromTableAsStringSQLite(targetPlayer.name).toString().replace("[", "").replace("]", ""))
            }
            "MongoDB" -> {
                Economy.formatBalance(TableFunctionMongo.selectAllFromCollectionAsString(targetPlayer.name).toString().replace("[", "").replace("]", ""))
            }
            "MySQL" -> {
                Economy.formatBalance(getValueFromSQL(targetPlayer, dataeconomyvalue).toString())
            }
            "Redis" -> {
                Economy.formatBalance(TableFunctionRedis.selectAllFromCollectionAsStringRedis(targetPlayer.name).toString().replace("[", "").replace("]", ""))
            }
            else -> ""
        }
        sender.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(bankAmount().replace("%valuename%", dataeconomyvalue).replace("%amountformatted%", balance).replace("%amount%", balance)))))
        if (soundeco != "NONE") {
            sender.playSound(sender.location, Sound.valueOf(soundeco), volumeeco.toFloat(), pitcheco.toFloat())
        }
    }
    private fun displayBalanceOthers(sender: Player, targetPlayer: Player) {
        val balance: String = when (databasetype) {
            "h2" -> {
                Economy.formatBalance(TableFunctionSQL.selectAllFromTableAsStringSQLite(targetPlayer.name).toString().replace("[", "").replace("]", ""))
            }
            "MongoDB" -> {
                Economy.formatBalance(TableFunctionMongo.selectAllFromCollectionAsString(targetPlayer.name).toString().replace("[", "").replace("]", ""))
            }
            "MySQL" -> {
                Economy.formatBalance(getValueFromSQL(targetPlayer, dataeconomyvalue).toString())
            }
            "Redis" -> {
                Economy.formatBalance(TableFunctionRedis.selectAllFromCollectionAsStringRedis(targetPlayer.name).toString().replace("[", "").replace("]", ""))
            }
            else -> ""
        }
        sender.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(bankAmountOthers().replace("%valuename%", dataeconomyvalue).replace("%p%", targetPlayer.name).replace("%amountformatted%", balance).replace("%amount%", balance)))))
        if (soundeco != "NONE") {
            sender.playSound(sender.location, Sound.valueOf(soundeco), volumeeco.toFloat(), pitcheco.toFloat())
        }
    }


}