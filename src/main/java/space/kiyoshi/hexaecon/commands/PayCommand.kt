/*
 * Copyright (c) 2023. KiyoshiDevelopment
 *   All rights reserved.
 */

@file:Suppress("SENSELESS_COMPARISON")

package space.kiyoshi.hexaecon.commands

import com.iridium.iridiumcolorapi.IridiumColorAPI
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import space.kiyoshi.hexaecon.HexaEcon.Companion.plugin
import space.kiyoshi.hexaecon.functions.TableFunctionMongo
import space.kiyoshi.hexaecon.functions.TableFunctionRedis
import space.kiyoshi.hexaecon.functions.TableFunctionSQL
import space.kiyoshi.hexaecon.utils.Economy
import space.kiyoshi.hexaecon.utils.Format
import space.kiyoshi.hexaecon.utils.GetConfig
import space.kiyoshi.hexaecon.utils.Language
import space.kiyoshi.hexaecon.utils.Language.accessDenied
import space.kiyoshi.hexaecon.utils.Language.cannotpayself
import space.kiyoshi.hexaecon.utils.Language.invalidAmount
import space.kiyoshi.hexaecon.utils.Language.paymentrecived
import space.kiyoshi.hexaecon.utils.Language.playerNotFound
import space.kiyoshi.hexaecon.utils.Language.playerpayed
import space.kiyoshi.hexaecon.utils.Language.usageFormat
import space.kiyoshi.hexaecon.utils.Language.usagePayment
import java.io.File
import java.util.*


class PayCommand : CommandExecutor, TabCompleter {
    private val dataeconomyvalue = GetConfig.main().getString("DataBase.DataEconomyName")!!
    private val sound = GetConfig.main().getString("Sounds.OnKillMonsters.Sound")!!
    private val volume = GetConfig.main().getInt("Sounds.OnKillMonsters.Volume")
    private val pitch = GetConfig.main().getInt("Sounds.OnKillMonsters.Pitch")
    private val databasetype = GetConfig.main().getString("DataBase.Type")!!
    private val soundnoperm = GetConfig.main().getString("Sounds.NoPermission.Sound")!!
    private val pitchnoperm = GetConfig.main().getInt("Sounds.NoPermission.Pitch")
    private val volumenoperm = GetConfig.main().getInt("Sounds.NoPermission.Volume")

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        val player = sender
        if (command.name == "pay") {
            if (args.size == 0) {
                player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(usageFormat().replace("%u", usagePayment()!!)))))
                if (sound != "NONE") {
                    if (sender is Player) {
                        sender.playSound(sender.location, Sound.valueOf(sound), volume.toFloat(), pitch.toFloat())
                    }
                }
            }
            if (args.size >= 1) {
                if (player.hasPermission("hexaecon.permissions.pay")) {
                    try {
                        if (args.size < 1) {
                            player.sendMessage(Format.hex(Format.color(usageFormat().replace("%u%", usagePayment()!!))))
                            if (sound != "NONE") {
                                if (sender is Player) {
                                    sender.playSound(sender.location, Sound.valueOf(sound), volume.toFloat(),pitch.toFloat()
                                    )
                                }
                            }
                        } else {
                            if (args.size < 2) {
                                player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(usageFormat().replace("%u%", usagePayment()!!)))))
                                if (sound != "NONE") {
                                    if (sender is Player) {
                                        sender.playSound(sender.location, Sound.valueOf(sound), volume.toFloat(), pitch.toFloat())
                                    }
                                }
                            } else {
                                val amount = args[0].toLong()
                                val targetname = args[1]
                                val target = Bukkit.getPlayer(targetname)
                                try {
                                    if (amount == null) {
                                        player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(invalidAmount().replace("%valuename%", dataeconomyvalue)))))
                                        return true
                                    }
                                    if (amount == 0L) {
                                        player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(invalidAmount().replace("%valuename%", dataeconomyvalue)))))
                                        return true
                                    }
                                    if (targetname == null || targetname.isEmpty() || targetname.isBlank()) {
                                        player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(playerNotFound().replace("%p%", args[2])))))
                                        if (sound != "NONE") {
                                            if (sender is Player) {
                                                sender.playSound(sender.location, Sound.valueOf(sound), volume.toFloat(), pitch.toFloat())
                                            }
                                        }
                                        return false
                                    }
                                    if (targetname == player.name) {
                                        player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(cannotpayself()))))
                                        if (sound != "NONE") {
                                            if (sender is Player) {
                                                sender.playSound(sender.location, Sound.valueOf(sound), volume.toFloat(), pitch.toFloat())
                                            }
                                        }
                                        return false
                                    }
                                    if (!(Format.hasLetter(args[0]) || Format.hasLetterAndSpecial(args[0]) || Format.hasLetterAndMabyeDigit(args[0]) || Format.hasLetterSpecialAndMaybeDigit(args[0]) || Format.hasSpecial(args[0]) || Format.hasLetter(args[0]))) {
                                        if (amount >= 1) {
                                            val data_names_sqlite =
                                                File(plugin.dataFolder.toString() + "/data/${target!!.name}/" + target.name + "_SQLite.txt")
                                            val data_names_mysql =
                                                File(plugin.dataFolder.toString() + "/data/${target.name}/" + target.name + "_MySQL.txt")
                                            val data_names_mongodb =
                                                File(plugin.dataFolder.toString() + "/data/${target.name}/" + target.name + "_MongoDB.txt")
                                            val data_names_redis =
                                                File(plugin.dataFolder.toString() + "/data/${target.name}/" + target.name + "_Redis.txt")
                                            val data_names_sqlite_self =
                                                File(plugin.dataFolder.toString() + "/data/${player.name}/" + player.name + "_SQLite.txt")
                                            val data_names_mysql_self =
                                                File(plugin.dataFolder.toString() + "/data/${player.name}/" + player.name + "_MySQL.txt")
                                            val data_names_mongodb_self =
                                                File(plugin.dataFolder.toString() + "/data/${player.name}/" + player.name + "_MongoDB.txt")
                                            val data_names_redis_self =
                                                File(plugin.dataFolder.toString() + "/data/${player.name}/" + player.name + "_MongoDB.txt")
                                            val data_names_config_sqlite: FileConfiguration =
                                                YamlConfiguration.loadConfiguration(data_names_sqlite)
                                            val data_names_config_mysql: FileConfiguration =
                                                YamlConfiguration.loadConfiguration(data_names_mysql)
                                            val data_names_config_mongodb: FileConfiguration =
                                                YamlConfiguration.loadConfiguration(data_names_mongodb)
                                            val data_names_config_redis: FileConfiguration =
                                                YamlConfiguration.loadConfiguration(data_names_redis)
                                            val data_names_config_sqlite_self: FileConfiguration =
                                                YamlConfiguration.loadConfiguration(data_names_sqlite_self)
                                            val data_names_config_mysql_self: FileConfiguration =
                                                YamlConfiguration.loadConfiguration(data_names_mysql_self)
                                            val data_names_config_mongodb_self: FileConfiguration =
                                                YamlConfiguration.loadConfiguration(data_names_mongodb_self)
                                            val data_names_config_redis_self: FileConfiguration =
                                                YamlConfiguration.loadConfiguration(data_names_redis_self)
                                            val somasqlitepay =
                                                data_names_config_sqlite_self.getLong("data.${dataeconomyvalue}") - amount
                                            val somamysqlpay =
                                                data_names_config_mysql_self.getLong("data.${dataeconomyvalue}") - amount
                                            val somamongodbpay =
                                                data_names_config_mongodb_self.getLong("data.${dataeconomyvalue}") - amount
                                            val somaredispay =
                                                data_names_config_redis_self.getLong("data.${dataeconomyvalue}") - amount
                                            val somasqlitepayed =
                                                data_names_config_sqlite.getLong("data.${dataeconomyvalue}") + amount
                                            val somamysqlpayed =
                                                data_names_config_mysql.getLong("data.${dataeconomyvalue}") + amount
                                            val somamongodbpayed =
                                                data_names_config_mongodb.getLong("data.${dataeconomyvalue}") + amount
                                            val somaredispayed =
                                                data_names_config_redis.getLong("data.${dataeconomyvalue}") + amount
                                            if (databasetype == "h2") {
                                                if (data_names_config_sqlite_self.getLong("data.${dataeconomyvalue}") >= amount) {
                                                    TableFunctionSQL.dropTableSQLite(player as Player)
                                                    TableFunctionSQL.dropTableSQLite(target)
                                                    TableFunctionSQL.createTableAmountSQLite(player, somasqlitepay)
                                                    TableFunctionSQL.createTableAmountSQLite(target, somasqlitepayed)
                                                    data_names_config_sqlite_self["data.${dataeconomyvalue}"] = somasqlitepay
                                                    data_names_config_sqlite["data.${dataeconomyvalue}"] = somasqlitepayed
                                                    data_names_config_sqlite_self.save(data_names_sqlite_self)
                                                    data_names_config_sqlite.save(data_names_sqlite)
                                                    plugin.reloadConfig()
                                                    player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(playerpayed().replace("%amountformatted%", Economy.formatBalance(amount.toString())).replace("%valuename%", dataeconomyvalue).replace("%p%", target.name).replace("%amount%", amount.toString())))))
                                                    target.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(paymentrecived().replace("%amountformatted%", Economy.formatBalance(amount.toString())).replace("%valuename%", dataeconomyvalue).replace("%p%", player.name).replace("%amount%", amount.toString())))))
                                                } else {
                                                    player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(Language.walletWithdrawNoEnoughAmount().replace("%amountformatted%", Economy.formatBalance(data_names_config_sqlite_self.getLong("data.${dataeconomyvalue}").toString())).replace("%valuename%", dataeconomyvalue).replace("%amount%", data_names_config_sqlite_self.getLong("data.${dataeconomyvalue}").toString())))))
                                                }
                                            } else if (databasetype == "MongoDB") {
                                                if (data_names_config_mongodb_self.getLong("data.${dataeconomyvalue}") >= amount) {
                                                    TableFunctionMongo.dropCollection(player.name)
                                                    TableFunctionMongo.dropCollection(target.name)
                                                    TableFunctionMongo.createCollectionAmount(player.name, somamongodbpay)
                                                    TableFunctionMongo.createCollectionAmount(target.name, somamongodbpayed)
                                                    data_names_config_mongodb_self["data.${dataeconomyvalue}"] = somamongodbpay
                                                    data_names_config_mongodb["data.${dataeconomyvalue}"] = somamongodbpayed
                                                    data_names_config_mongodb_self.save(data_names_mongodb_self)
                                                    data_names_config_mongodb.save(data_names_mongodb)
                                                    plugin.reloadConfig()
                                                    player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(playerpayed().replace("%amountformatted%", Economy.formatBalance(amount.toString())).replace("%valuename%", dataeconomyvalue).replace("%p%", target.name).replace("%amount%", amount.toString())))))
                                                    target.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(paymentrecived().replace("%amountformatted%", Economy.formatBalance(amount.toString())).replace("%valuename%", dataeconomyvalue).replace("%p%", player.name).replace("%amount%", amount.toString())))))
                                                } else {
                                                    player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(Language.walletWithdrawNoEnoughAmount().replace("%amountformatted%", Economy.formatBalance(data_names_config_mongodb_self.getLong("data.${dataeconomyvalue}").toString())).replace("%valuename%", dataeconomyvalue).replace("%amount%", data_names_config_mongodb_self.getLong("data.${dataeconomyvalue}").toString())))))
                                                }
                                            } else if (databasetype == "MySQL") {
                                                if (data_names_config_mysql_self.getLong("data.${dataeconomyvalue}") >= amount) {
                                                    TableFunctionSQL.dropTable(player as Player)
                                                    TableFunctionSQL.dropTable(target)
                                                    TableFunctionSQL.createTableAmount(player, somamysqlpay)
                                                    TableFunctionSQL.createTableAmount(target, somamysqlpayed)
                                                    data_names_config_mysql_self["data.${dataeconomyvalue}"] = somamysqlpay
                                                    data_names_config_mysql["data.${dataeconomyvalue}"] = somamysqlpayed
                                                    data_names_config_mysql_self.save(data_names_mysql)
                                                    data_names_config_mysql.save(data_names_mysql)
                                                    plugin.reloadConfig()
                                                    player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(playerpayed().replace("%amountformatted%", Economy.formatBalance(amount.toString())).replace("%valuename%", dataeconomyvalue).replace("%p%", target.name).replace("%amount%", amount.toString())))))
                                                    target.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(paymentrecived().replace("%amountformatted%", Economy.formatBalance(amount.toString())).replace("%valuename%", dataeconomyvalue).replace("%p%", player.name).replace("%amount%", amount.toString())))))
                                                } else {
                                                    player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(Language.walletWithdrawNoEnoughAmount().replace("%amountformatted%", Economy.formatBalance(data_names_config_mysql_self.getLong("data.${dataeconomyvalue}").toString())).replace("%valuename%", dataeconomyvalue).replace("%amount%", data_names_config_mysql_self.getLong("data.${dataeconomyvalue}").toString())))))
                                                }
                                            } else if (databasetype == "Redis") {
                                                if (data_names_config_redis_self.getLong("data.${dataeconomyvalue}") >= amount) {
                                                    TableFunctionRedis.dropTable(player.name)
                                                    TableFunctionRedis.dropTable(target.name)
                                                    TableFunctionRedis.createTableAmount(player.name, somaredispay)
                                                    TableFunctionRedis.createTableAmount(target.name, somaredispayed)
                                                    data_names_config_redis_self["data.${dataeconomyvalue}"] = somaredispay
                                                    data_names_config_redis["data.${dataeconomyvalue}"] = somaredispayed
                                                    data_names_config_redis_self.save(data_names_redis_self)
                                                    data_names_config_redis.save(data_names_redis)
                                                    plugin.reloadConfig()
                                                    player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(playerpayed().replace("%amountformatted%", Economy.formatBalance(amount.toString())).replace("%valuename%", dataeconomyvalue).replace("%p%", target.name).replace("%amount%", amount.toString())))))
                                                    target.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(paymentrecived().replace("%amountformatted%", Economy.formatBalance(amount.toString())).replace("%valuename%", dataeconomyvalue).replace("%p%", player.name).replace("%amount%", amount.toString())))))
                                                } else {
                                                    player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(Language.walletWithdrawNoEnoughAmount().replace("%amountformatted%", Economy.formatBalance(data_names_config_redis_self.getLong("data.${dataeconomyvalue}").toString())).replace("%valuename%", dataeconomyvalue).replace("%amount%", data_names_config_redis_self.getLong("data.${dataeconomyvalue}").toString())))))
                                                }
                                            }
                                        } else {
                                            player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(invalidAmount().replace("%valuename%", dataeconomyvalue)))))
                                        }
                                    }
                                    return true
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(invalidAmount().replace("%valuename%", dataeconomyvalue)))))
                                }
                            }
                        }
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                        player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(invalidAmount().replace("%valuename%", dataeconomyvalue)))))
                    }
                } else {
                    if(soundnoperm != "NONE") {
                        (player as Player).player?.playSound(player.location, Sound.valueOf(soundnoperm), volumenoperm.toFloat(), pitchnoperm.toFloat())
                    }
                    player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(accessDenied().replace("%perm%", "hexaecon.permissions.pay")))))
                }
            }
        }
        return false
    }

    private fun a(s: MutableList<String>, arg: String, test: String) {
        if (test.startsWith(arg.lowercase(Locale.getDefault()))) s.add(test)
    }

    override fun onTabComplete(arg0: CommandSender, arg1: Command, arg2: String, args: Array<String>): List<String>? {
        if (arg1.name == "pay") {
            if (args.size == 1) {
                val s: MutableList<String> = mutableListOf()
                for (i in 1..100) {
                    a(s, args[0], i.toString())
                }
                s.sortBy { it.toInt() }
                return s
            }
        }
        return null
    }

}