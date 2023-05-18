@file:Suppress(
    "ReplaceSizeZeroCheckWithIsEmpty", "SameParameterValue", "UnnecessaryVariable",
    "ReplaceSizeCheckWithIsNotEmpty", "LocalVariableName", "SpellCheckingInspection", "SENSELESS_COMPARISON"
)

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
import space.kiyoshi.hexaecon.functions.TableFunction
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
import java.io.IOException
import java.sql.SQLException
import java.util.*


class PayCommand : CommandExecutor, TabCompleter {
    private val dataeconomyvalue = GetConfig.main().getString("DataBase.DataEconomyName")!!
    private val sound = GetConfig.main().getString("Sounds.OnKillMonsters.Sound")!!
    private val volume = GetConfig.main().getInt("Sounds.OnKillMonsters.Volume")
    private val pitch = GetConfig.main().getInt("Sounds.OnKillMonsters.Pitch")
    private val databasetype = GetConfig.main().getString("DataBase.Type")!!

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        val player = sender
        if (command.name == "pay") {
            if (args.size == 0) {
                player.sendMessage(
                    Format.hex(
                        Format.color(
                            IridiumColorAPI.process(
                                usageFormat().replace(
                                    "%u",
                                    usagePayment()!!
                                )
                            )
                        )
                    )
                )
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
                            player.sendMessage(Format.hex(Format.color(usageFormat().replace("%u", usagePayment()!!))))
                            if (sound != "NONE") {
                                if (sender is Player) {
                                    sender.playSound(
                                        sender.location,
                                        Sound.valueOf(sound),
                                        volume.toFloat(),
                                        pitch.toFloat()
                                    )
                                }
                            }
                        } else {
                            if (args.size < 2) {
                                player.sendMessage(
                                    Format.hex(
                                        Format.color(
                                            IridiumColorAPI.process(
                                                usageFormat().replace(
                                                    "%u",
                                                    usagePayment()!!
                                                )
                                            )
                                        )
                                    )
                                )
                                if (sound != "NONE") {
                                    if (sender is Player) {
                                        sender.playSound(
                                            sender.location,
                                            Sound.valueOf(sound),
                                            volume.toFloat(),
                                            pitch.toFloat()
                                        )
                                    }
                                }
                            } else {
                                val amount = args[0].toInt()
                                val targetname = args[1]
                                val target = Bukkit.getPlayer(targetname)
                                try {
                                    if (amount == null) {
                                        player.sendMessage(
                                            Format.hex(
                                                Format.color(
                                                    IridiumColorAPI.process(
                                                        invalidAmount().replace(
                                                            "%valuename",
                                                            dataeconomyvalue
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                        return true
                                    }
                                    if (amount == 0) {
                                        player.sendMessage(
                                            Format.hex(
                                                Format.color(
                                                    IridiumColorAPI.process(
                                                        invalidAmount().replace(
                                                            "%valuename",
                                                            dataeconomyvalue
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                        return true
                                    }

                                    if (targetname == null || targetname.isEmpty() || targetname.isBlank()) {
                                        player.sendMessage(
                                            Format.hex(
                                                Format.color(
                                                    IridiumColorAPI.process(
                                                        playerNotFound().replace("%p", args[2])
                                                    )
                                                )
                                            )
                                        )
                                        if (sound != "NONE") {
                                            if (sender is Player) {
                                                sender.playSound(
                                                    sender.location,
                                                    Sound.valueOf(sound),
                                                    volume.toFloat(),
                                                    pitch.toFloat()
                                                )
                                            }
                                        }
                                        return false
                                    }
                                    if (targetname == player.name) {
                                        player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(cannotpayself()))))
                                        if (sound != "NONE") {
                                            if (sender is Player) {
                                                sender.playSound(
                                                    sender.location,
                                                    Sound.valueOf(sound),
                                                    volume.toFloat(),
                                                    pitch.toFloat()
                                                )
                                            }
                                        }
                                        return false
                                    }
                                    if (!(Format.hasLetter(args[0]) ||
                                                Format.hasLetterAndSpecial(args[0]) ||
                                                Format.hasLetterAndMabyeDigit(args[0]) ||
                                                Format.hasLetterSpecialAndMaybeDigit(args[0]) ||
                                                Format.hasSpecial(args[0]) ||
                                                Format.hasLetter(args[0]))
                                    ) {
                                        if (amount >= 1) {
                                            val data_names_sqlite =
                                                File(plugin.dataFolder.toString() + "/data/" + target!!.name + "_SQLite.txt")
                                            val data_names_mysql =
                                                File(plugin.dataFolder.toString() + "/data/" + target.name + "_MySQL.txt")

                                            val data_names_sqlite_self =
                                                File(plugin.dataFolder.toString() + "/data/" + player.name + "_SQLite.txt")
                                            val data_names_mysql_self =
                                                File(plugin.dataFolder.toString() + "/data/" + player.name + "_MySQL.txt")

                                            val data_names_config_sqlite: FileConfiguration =
                                                YamlConfiguration.loadConfiguration(data_names_sqlite)
                                            val data_names_config_mysql: FileConfiguration =
                                                YamlConfiguration.loadConfiguration(data_names_mysql)

                                            val data_names_config_sqlite_self: FileConfiguration =
                                                YamlConfiguration.loadConfiguration(data_names_sqlite_self)
                                            val data_names_config_mysql_self: FileConfiguration =
                                                YamlConfiguration.loadConfiguration(data_names_mysql_self)

                                            val somasqlitepay =
                                                data_names_config_sqlite_self.getInt("data.${dataeconomyvalue}") - amount
                                            val somamysqlpay =
                                                data_names_config_mysql_self.getInt("data.${dataeconomyvalue}") - amount

                                            val somasqlitepayed =
                                                data_names_config_sqlite.getInt("data.${dataeconomyvalue}") + amount
                                            val somamysqlpayed =
                                                data_names_config_mysql.getInt("data.${dataeconomyvalue}") + amount
                                            if (databasetype == "h2") {
                                                if (data_names_config_sqlite_self.getInt("data.${dataeconomyvalue}") >= amount) {
                                                    try {
                                                        TableFunction.dropTableSQLite(player as Player)
                                                        TableFunction.dropTableSQLite(target)
                                                    } catch (_: SQLException) {
                                                    }
                                                    try {
                                                        TableFunction.createTableAmountSQLite(
                                                            player as Player,
                                                            somasqlitepay
                                                        )
                                                        TableFunction.createTableAmountSQLite(target, somasqlitepayed)
                                                    } catch (_: SQLException) {
                                                    }
                                                    try {
                                                        data_names_config_sqlite_self["data.${dataeconomyvalue}"] =
                                                            somasqlitepay
                                                        data_names_config_sqlite["data.${dataeconomyvalue}"] =
                                                            somasqlitepayed
                                                    } catch (_: IOException) {
                                                    }
                                                    try {
                                                        data_names_config_sqlite_self.save(data_names_sqlite_self)
                                                        data_names_config_sqlite.save(data_names_sqlite)
                                                    } catch (_: IOException) {
                                                    }
                                                    plugin.reloadConfig()
                                                    player.sendMessage(
                                                        Format.hex(
                                                            Format.color(
                                                                IridiumColorAPI.process(
                                                                    playerpayed().replace("%amount", amount.toString())
                                                                        .replace("%valuename", dataeconomyvalue)
                                                                        .replace("%p", target.name)
                                                                )
                                                            )
                                                        )
                                                    )
                                                    target.sendMessage(
                                                        Format.hex(
                                                            Format.color(
                                                                IridiumColorAPI.process(
                                                                    paymentrecived().replace(
                                                                        "%amount",
                                                                        amount.toString()
                                                                    ).replace("%valuename", dataeconomyvalue)
                                                                        .replace("%p", player.name)
                                                                )
                                                            )
                                                        )
                                                    )
                                                } else {
                                                    player.sendMessage(
                                                        Format.hex(
                                                            Format.color(
                                                                IridiumColorAPI.process(
                                                                    Language.walletWithdrawNoEnoughAmount()
                                                                        .replace(
                                                                            "%amount",
                                                                            data_names_config_sqlite_self.getInt("data.${dataeconomyvalue}")
                                                                                .toString()
                                                                        ).replace("%valuename", dataeconomyvalue)
                                                                )
                                                            )
                                                        )
                                                    )
                                                }
                                            } else {
                                                if (data_names_config_mysql_self.getInt("data.${dataeconomyvalue}") >= amount) {
                                                    try {
                                                        TableFunction.dropTable(player as Player)
                                                        TableFunction.dropTable(target)
                                                    } catch (_: SQLException) {
                                                    }
                                                    try {
                                                        TableFunction.createTableAmount(player as Player, somamysqlpay)
                                                        TableFunction.createTableAmount(target, somamysqlpayed)
                                                    } catch (_: SQLException) {
                                                    }
                                                    try {
                                                        data_names_config_mysql_self["data.${dataeconomyvalue}"] =
                                                            somamysqlpay
                                                        data_names_config_mysql["data.${dataeconomyvalue}"] =
                                                            somamysqlpayed
                                                    } catch (_: IOException) {
                                                    }
                                                    try {
                                                        data_names_config_mysql_self.save(data_names_mysql)
                                                        data_names_config_mysql.save(data_names_mysql)
                                                    } catch (_: IOException) {
                                                    }
                                                    plugin.reloadConfig()
                                                    player.sendMessage(
                                                        Format.hex(
                                                            Format.color(
                                                                IridiumColorAPI.process(
                                                                    playerpayed().replace("%amount", amount.toString())
                                                                        .replace("%valuename", dataeconomyvalue)
                                                                        .replace("%p", target.name)
                                                                )
                                                            )
                                                        )
                                                    )
                                                    target.sendMessage(
                                                        Format.hex(
                                                            Format.color(
                                                                IridiumColorAPI.process(
                                                                    paymentrecived().replace(
                                                                        "%amount",
                                                                        amount.toString()
                                                                    ).replace("%valuename", dataeconomyvalue)
                                                                        .replace("%p", player.name)
                                                                )
                                                            )
                                                        )
                                                    )
                                                } else {
                                                    player.sendMessage(
                                                        Format.hex(
                                                            Format.color(
                                                                IridiumColorAPI.process(
                                                                    Language.walletWithdrawNoEnoughAmount()
                                                                        .replace(
                                                                            "%amount",
                                                                            data_names_config_mysql_self.getInt("data.${dataeconomyvalue}")
                                                                                .toString()
                                                                        ).replace("%valuename", dataeconomyvalue)
                                                                )
                                                            )
                                                        )
                                                    )
                                                }
                                            }
                                        } else {
                                            player.sendMessage(
                                                Format.hex(
                                                    Format.color(
                                                        IridiumColorAPI.process(
                                                            invalidAmount().replace("%valuename", dataeconomyvalue)
                                                        )
                                                    )
                                                )
                                            )
                                        }
                                    }
                                    return true
                                } catch (e: Exception) {
                                    player.sendMessage(
                                        Format.hex(
                                            Format.color(
                                                IridiumColorAPI.process(
                                                    invalidAmount().replace(
                                                        "%valuename",
                                                        dataeconomyvalue
                                                    )
                                                )
                                            )
                                        )
                                    )
                                }
                            }
                        }
                    } catch (e: NumberFormatException) {
                        player.sendMessage(
                            Format.hex(
                                Format.color(
                                    IridiumColorAPI.process(
                                        invalidAmount().replace(
                                            "%valuename",
                                            dataeconomyvalue
                                        )
                                    )
                                )
                            )
                        )
                    }
                } else {
                    player.sendMessage(
                        Format.hex(
                            Format.color(
                                IridiumColorAPI.process(
                                    accessDenied().replace(
                                        "%perm",
                                        "hexaecon.permissions.pay"
                                    )
                                )
                            )
                        )
                    )
                }
            }
        }
        return false
    }

    private fun a(s: MutableList<String>, arg: String, test: String) {
        if (test.startsWith(arg.lowercase(Locale.getDefault()))) s.add(test)
    }

    override fun onTabComplete(arg0: CommandSender, arg1: Command, arg2: String, args: Array<String>): List<String>? {
        val s: MutableList<String> = mutableListOf()
        if (arg1.name == "pay") {
            if (args.size == 1) {
                for (i in 1..100) {
                    a(s, args[0], i.toString())
                }
                s.sort()
                return s
            }
            return mutableListOf()
        }
        return null
    }
}