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
import space.kiyoshi.hexaecon.HexaEcon
import space.kiyoshi.hexaecon.HexaEcon.Companion.plugin
import space.kiyoshi.hexaecon.functions.TableFunction
import space.kiyoshi.hexaecon.utils.*
import space.kiyoshi.hexaecon.utils.Language.accessDenied
import space.kiyoshi.hexaecon.utils.Language.cannotRemoveEconFromPlayer
import space.kiyoshi.hexaecon.utils.Language.configurationReloaded
import space.kiyoshi.hexaecon.utils.Language.generateToOther
import space.kiyoshi.hexaecon.utils.Language.invalidAmount
import space.kiyoshi.hexaecon.utils.Language.playerNotFound
import space.kiyoshi.hexaecon.utils.Language.removedEconFromPlayer
import space.kiyoshi.hexaecon.utils.Language.usageConvertDeposit
import space.kiyoshi.hexaecon.utils.Language.usageFormat
import space.kiyoshi.hexaecon.utils.Language.walletWithdrawAmount
import space.kiyoshi.hexaecon.utils.Language.walletWithdrawConverted
import space.kiyoshi.hexaecon.utils.Language.walletWithdrawNoEnoughAmount
import space.kiyoshi.hexaecon.utils.Language.walletWithdrawRemainingAmount
import java.io.File
import java.io.IOException
import java.sql.SQLException
import java.util.*
import java.util.logging.Level
import java.util.logging.LogRecord


class WalletCommand : CommandExecutor, TabCompleter {
    private val dataeconomyvalue = GetConfig.main().getString("DataBase.DataEconomyName")!!
    private val sound = GetConfig.main().getString("Sounds.OnKillMonsters.Sound")!!
    private val volume = GetConfig.main().getInt("Sounds.OnKillMonsters.Volume")
    private val pitch = GetConfig.main().getInt("Sounds.OnKillMonsters.Pitch")
    private val databasetype = GetConfig.main().getString("DataBase.Type")!!

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        val player = sender
        if (command.name == "wallet") {
            if (args.size == 0) {
                player.sendMessage(
                    Format.hex(
                        Format.color(
                            IridiumColorAPI.process(
                                usageFormat().replace(
                                    "%u",
                                    usageConvertDeposit()!!
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
                if (args[0] == "reload") {
                    if (player.hasPermission("hexaecon.permissions.reload")) {
                        reloadPlugin()
                        if (sender is Player) {
                            player.sendMessage(Format.hex(Format.color(IridiumColorAPI.process(configurationReloaded()))))
                        }
                        KiyoshiLogger.log(
                            LogRecord(Level.INFO, "[HexaEcon] configuration successfully reloaded."),
                            "HexaEcon"
                        )
                    } else {
                        player.sendMessage(
                            Format.hex(
                                Format.color(
                                    IridiumColorAPI.process(
                                        accessDenied().replace(
                                            "%perm",
                                            "hexaecon.permissions.reload"
                                        )
                                    )
                                )
                            )
                        )
                    }
                }
                if (args[0] == "withdraw") {
                    if (player.hasPermission("hexaecon.permissions.withdraw")) {
                        try {
                            if (args.size < 2) {
                                sender.sendMessage(
                                    Format.hex(
                                        Format.color(
                                            IridiumColorAPI.process(
                                                usageFormat().replace(
                                                    "%u",
                                                    usageConvertDeposit()!!
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
                                if (!(Format.hasLetter(args[1]) ||
                                            Format.hasLetterAndSpecial(args[1]) ||
                                            Format.hasLetterAndMabyeDigit(args[1]) ||
                                            Format.hasLetterSpecialAndMaybeDigit(args[1]) ||
                                            Format.hasSpecial(args[1]) ||
                                            Format.hasLetter(args[1]))
                                ) {
                                    val remove = args[1].toInt()
                                    if (remove == 0) {
                                        sender.sendMessage(
                                            Format.hex(
                                                IridiumColorAPI.process(
                                                    Format.color(
                                                        invalidAmount().replace(
                                                            "%valuename",
                                                            dataeconomyvalue
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    }
                                    if (remove >= 1) {
                                        val data_names_sqlite =
                                            File(plugin.dataFolder.toString() + "/data/" + sender.name + "_SQLite.txt")
                                        val data_names_mysql =
                                            File(plugin.dataFolder.toString() + "/data/" + sender.name + "_MySQL.txt")
                                        val data_names_config_sqlite: FileConfiguration =
                                            YamlConfiguration.loadConfiguration(data_names_sqlite)
                                        val data_names_config_mysql: FileConfiguration =
                                            YamlConfiguration.loadConfiguration(data_names_mysql)
                                        val somasqlite =
                                            data_names_config_sqlite.getInt("data.${dataeconomyvalue}") - remove
                                        val somamysql =
                                            data_names_config_mysql.getInt("data.${dataeconomyvalue}") - remove
                                        if (databasetype == "h2") {
                                            if (data_names_config_sqlite.getInt("data.${dataeconomyvalue}") >= remove) {
                                                try {
                                                    if (databasetype == "h2") {
                                                        TableFunction.dropTableSQLite(sender as Player)
                                                    } else {
                                                        TableFunction.dropTable(sender as Player)
                                                    }
                                                } catch (_: SQLException) {
                                                }
                                                try {
                                                    if (databasetype == "h2") {
                                                        TableFunction.createTableAmountSQLite(
                                                            player as Player,
                                                            somasqlite
                                                        )
                                                    } else {
                                                        TableFunction.createTableAmount(player as Player, somamysql)
                                                    }
                                                } catch (e: SQLException) {
                                                    throw RuntimeException(e)
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
                                                } catch (e: IOException) {
                                                    throw RuntimeException(e)
                                                }
                                                plugin.reloadConfig()
                                                player.sendMessage(
                                                    Format.hex(
                                                        Format.color(
                                                            IridiumColorAPI.process(
                                                                walletWithdrawAmount().replace(
                                                                    "%amount",
                                                                    remove.toString()
                                                                )
                                                                    .replace("%valuename", dataeconomyvalue)
                                                            )
                                                        )
                                                    )
                                                )
                                                player.sendMessage(
                                                    Format.hex(
                                                        Format.color(
                                                            IridiumColorAPI.process(
                                                                walletWithdrawConverted().replace(
                                                                    "%amount",
                                                                    remove.toString()
                                                                ).replace("%valuename", dataeconomyvalue)
                                                            )
                                                        )
                                                    )
                                                )
                                                if (databasetype == "h2") {
                                                    player.sendMessage(
                                                        Format.hex(
                                                            Format.color(
                                                                IridiumColorAPI.process(
                                                                    walletWithdrawRemainingAmount().replace(
                                                                        "%amount",
                                                                        data_names_config_sqlite.getInt("data.${dataeconomyvalue}")
                                                                            .toString()
                                                                    ).replace("%valuename", dataeconomyvalue)
                                                                )
                                                            )
                                                        )
                                                    )
                                                } else {
                                                    player.sendMessage(
                                                        Format.hex(
                                                            Format.color(
                                                                IridiumColorAPI.process(
                                                                    walletWithdrawRemainingAmount().replace(
                                                                        "%amount",
                                                                        data_names_config_mysql.getInt("data.${dataeconomyvalue}")
                                                                            .toString()
                                                                    ).replace("%valuename", dataeconomyvalue)
                                                                )
                                                            )
                                                        )
                                                    )
                                                }
                                                player.inventory.addItem(Economy.addEconomy(player, remove))
                                            } else {
                                                if (databasetype == "h2") {
                                                    player.sendMessage(
                                                        Format.hex(
                                                            Format.color(
                                                                IridiumColorAPI.process(
                                                                    walletWithdrawNoEnoughAmount().replace(
                                                                        "%amount",
                                                                        data_names_config_sqlite.getInt("data.${dataeconomyvalue}")
                                                                            .toString()
                                                                    ).replace("%valuename", dataeconomyvalue)
                                                                )
                                                            )
                                                        )
                                                    )
                                                } else {
                                                    player.sendMessage(
                                                        Format.hex(
                                                            Format.color(
                                                                IridiumColorAPI.process(
                                                                    walletWithdrawNoEnoughAmount().replace(
                                                                        "%amount",
                                                                        data_names_config_mysql.getInt("data.${dataeconomyvalue}")
                                                                            .toString()
                                                                    ).replace("%valuename", dataeconomyvalue)
                                                                )
                                                            )
                                                        )
                                                    )
                                                }
                                            }
                                        } else {
                                            if (data_names_config_mysql.getInt("data.${dataeconomyvalue}") >= remove) {
                                                try {
                                                    if (databasetype == "h2") {
                                                        TableFunction.dropTableSQLite(sender as Player)
                                                    } else {
                                                        TableFunction.dropTable(sender as Player)
                                                    }
                                                } catch (_: SQLException) {
                                                }
                                                try {
                                                    if (databasetype == "h2") {
                                                        TableFunction.createTableAmountSQLite(
                                                            player as Player,
                                                            somasqlite
                                                        )
                                                    } else {
                                                        TableFunction.createTableAmount(player as Player, somamysql)
                                                    }
                                                } catch (e: SQLException) {
                                                    throw RuntimeException(e)
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
                                                } catch (e: IOException) {
                                                    throw RuntimeException(e)
                                                }
                                                plugin.reloadConfig()
                                                player.sendMessage(
                                                    Format.hex(
                                                        Format.color(
                                                            IridiumColorAPI.process(
                                                                walletWithdrawAmount().replace(
                                                                    "%amount",
                                                                    remove.toString()
                                                                )
                                                                    .replace("%valuename", dataeconomyvalue)
                                                            )
                                                        )
                                                    )
                                                )
                                                player.sendMessage(
                                                    Format.hex(
                                                        Format.color(
                                                            IridiumColorAPI.process(
                                                                walletWithdrawConverted().replace(
                                                                    "%amount",
                                                                    remove.toString()
                                                                ).replace("%valuename", dataeconomyvalue)
                                                            )
                                                        )
                                                    )
                                                )
                                                if (databasetype == "h2") {
                                                    player.sendMessage(
                                                        Format.hex(
                                                            Format.color(
                                                                IridiumColorAPI.process(
                                                                    walletWithdrawRemainingAmount().replace(
                                                                        "%amount",
                                                                        data_names_config_sqlite.getInt("data.${dataeconomyvalue}")
                                                                            .toString()
                                                                    ).replace("%valuename", dataeconomyvalue)
                                                                )
                                                            )
                                                        )
                                                    )
                                                } else {
                                                    player.sendMessage(
                                                        Format.hex(
                                                            Format.color(
                                                                IridiumColorAPI.process(
                                                                    walletWithdrawRemainingAmount().replace(
                                                                        "%amount",
                                                                        data_names_config_mysql.getInt("data.${dataeconomyvalue}")
                                                                            .toString()
                                                                    ).replace("%valuename", dataeconomyvalue)
                                                                )
                                                            )
                                                        )
                                                    )
                                                }
                                                player.inventory.addItem(Economy.addEconomy(player, remove))
                                            } else {
                                                if (databasetype == "h2") {
                                                    player.sendMessage(
                                                        Format.hex(
                                                            Format.color(
                                                                IridiumColorAPI.process(
                                                                    walletWithdrawNoEnoughAmount().replace(
                                                                        "%amount",
                                                                        data_names_config_sqlite.getInt("data.${dataeconomyvalue}")
                                                                            .toString()
                                                                    ).replace("%valuename", dataeconomyvalue)
                                                                )
                                                            )
                                                        )
                                                    )
                                                } else {
                                                    player.sendMessage(
                                                        Format.hex(
                                                            Format.color(
                                                                IridiumColorAPI.process(
                                                                    walletWithdrawNoEnoughAmount().replace(
                                                                        "%amount",
                                                                        data_names_config_mysql.getInt("data.${dataeconomyvalue}")
                                                                            .toString()
                                                                    ).replace("%valuename", dataeconomyvalue)
                                                                )
                                                            )
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
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
                                            "hexaecon.permissions.withdraw"
                                        )
                                    )
                                )
                            )
                        )
                    }
                }
                if (args[0] == "remove") {
                    if (player.hasPermission("hexaecon.permissions.remove")) {
                        try {
                            if (args.size < 2) {
                                sender.sendMessage(
                                    Format.hex(
                                        Format.color(
                                            IridiumColorAPI.process(
                                                usageFormat().replace(
                                                    "%u",
                                                    usageConvertDeposit()!!
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
                                if (!(Format.hasLetter(args[1]) ||
                                            Format.hasLetterAndSpecial(args[1]) ||
                                            Format.hasLetterAndMabyeDigit(args[1]) ||
                                            Format.hasLetterSpecialAndMaybeDigit(args[1]) ||
                                            Format.hasSpecial(args[1]) ||
                                            Format.hasLetter(args[1]))
                                ) {
                                    val remove = args[1].toInt()
                                    if (remove == 0) {
                                        sender.sendMessage(
                                            Format.hex(
                                                IridiumColorAPI.process(
                                                    Format.color(
                                                        invalidAmount().replace(
                                                            "%valuename",
                                                            dataeconomyvalue
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    }
                                    if (remove >= 1) {
                                        val targetname = args[2]
                                        val target = Bukkit.getPlayer(targetname)
                                        if (targetname == null || targetname.isEmpty() || targetname.isBlank()) {
                                            player.sendMessage(
                                                Format.hex(
                                                    Format.hex(
                                                        Format.color(
                                                            IridiumColorAPI.process(
                                                                playerNotFound().replace("%p", args[2])
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
                                            return false
                                        }
                                        val data_names_sqlite =
                                            File(plugin.dataFolder.toString() + "/data/" + target!!.name + "_SQLite.txt")
                                        val data_names_mysql =
                                            File(plugin.dataFolder.toString() + "/data/" + target.name + "_MySQL.txt")
                                        val data_names_config_sqlite: FileConfiguration =
                                            YamlConfiguration.loadConfiguration(data_names_sqlite)
                                        val data_names_config_mysql: FileConfiguration =
                                            YamlConfiguration.loadConfiguration(data_names_mysql)
                                        val somasqlite =
                                            data_names_config_sqlite.getInt("data.${dataeconomyvalue}") - remove
                                        val somamysql =
                                            data_names_config_mysql.getInt("data.${dataeconomyvalue}") - remove
                                        if (databasetype == "h2") {
                                            if (data_names_config_sqlite.getInt("data.${dataeconomyvalue}") >= remove) {
                                                try {
                                                    if (databasetype == "h2") {
                                                        TableFunction.dropTableSQLite(target)
                                                    } else {
                                                        TableFunction.dropTable(target)
                                                    }
                                                } catch (_: SQLException) {
                                                }
                                                try {
                                                    if (databasetype == "h2") {
                                                        TableFunction.createTableAmountSQLite(target, somasqlite)
                                                    } else {
                                                        TableFunction.createTableAmount(target, somamysql)
                                                    }
                                                } catch (e: SQLException) {
                                                    throw RuntimeException(e)
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
                                                } catch (e: IOException) {
                                                    throw RuntimeException(e)
                                                }
                                                sender.sendMessage(
                                                    Format.hex(
                                                        Format.color(
                                                            IridiumColorAPI.process(
                                                                removedEconFromPlayer().replace(
                                                                    "%amount",
                                                                    remove.toString()
                                                                )
                                                                    .replace("%valuename", dataeconomyvalue)
                                                                    .replace("%p", target.name)
                                                            )
                                                        )
                                                    )
                                                )
                                                plugin.reloadConfig()
                                            } else {
                                                sender.sendMessage(
                                                    Format.hex(
                                                        Format.color(
                                                            IridiumColorAPI.process(
                                                                cannotRemoveEconFromPlayer().replace(
                                                                    "%amount",
                                                                    remove.toString()
                                                                )
                                                                    .replace("%valuename", dataeconomyvalue)
                                                                    .replace("%p", target.name)
                                                                    .replace(
                                                                        "%targetbalance",
                                                                        data_names_config_sqlite.getInt("data.${dataeconomyvalue}")
                                                                            .toString()
                                                                    )
                                                            )
                                                        )
                                                    )
                                                )
                                            }
                                        } else {
                                            if (data_names_config_mysql.getInt("data.${dataeconomyvalue}") >= remove) {
                                                try {
                                                    if (databasetype == "h2") {
                                                        TableFunction.dropTableSQLite(target)
                                                    } else {
                                                        TableFunction.dropTable(target)
                                                    }
                                                } catch (_: SQLException) {
                                                }
                                                try {
                                                    if (databasetype == "h2") {
                                                        TableFunction.createTableAmountSQLite(target, somasqlite)
                                                    } else {
                                                        TableFunction.createTableAmount(target, somamysql)
                                                    }
                                                } catch (e: SQLException) {
                                                    throw RuntimeException(e)
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
                                                } catch (e: IOException) {
                                                    throw RuntimeException(e)
                                                }
                                                sender.sendMessage(
                                                    Format.hex(
                                                        Format.color(
                                                            IridiumColorAPI.process(
                                                                removedEconFromPlayer().replace(
                                                                    "%amount",
                                                                    remove.toString()
                                                                )
                                                                    .replace("%valuename", dataeconomyvalue)
                                                                    .replace("%p", target.name)
                                                            )
                                                        )
                                                    )
                                                )
                                                plugin.reloadConfig()
                                            } else {
                                                sender.sendMessage(
                                                    Format.hex(
                                                        Format.color(
                                                            IridiumColorAPI.process(
                                                                cannotRemoveEconFromPlayer().replace(
                                                                    "%amount",
                                                                    remove.toString()
                                                                )
                                                                    .replace("%valuename", dataeconomyvalue)
                                                                    .replace("%p", target.name)
                                                                    .replace(
                                                                        "%targetbalance",
                                                                        data_names_config_mysql.getInt("data.${dataeconomyvalue}")
                                                                            .toString()
                                                                    )
                                                            )
                                                        )
                                                    )
                                                )
                                            }
                                        }
                                    }
                                } else {
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
                                            "hexaecon.permissions.remove"
                                        )
                                    )
                                )
                            )
                        )
                    }
                }
                if (args[0] == "generate") {
                    if (player.hasPermission("hexaecon.permissions.generate")) {
                        try {
                            if (args.size < 2) {
                                player.sendMessage(
                                    Format.hex(
                                        Format.color(
                                            usageFormat().replace(
                                                "%u",
                                                usageConvertDeposit()!!
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
                                if (args.size < 3) {
                                    player.sendMessage(
                                        Format.hex(
                                            Format.color(
                                                IridiumColorAPI.process(
                                                    usageFormat().replace(
                                                        "%u",
                                                        usageConvertDeposit()!!
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
                                    val amount = args[1].toInt()
                                    val targetname = args[2]
                                    val target = Bukkit.getPlayer(targetname)
                                    try {
                                        if (amount == null) {
                                            player.sendMessage(
                                                Format.hex(
                                                    Format.color(
                                                        IridiumColorAPI.process(
                                                            invalidAmount().replace("%valuename", dataeconomyvalue)
                                                        )
                                                    )
                                                )
                                            )
                                            return true
                                        }
                                        if (amount < 1) {
                                            player.sendMessage(
                                                Format.hex(
                                                    Format.color(
                                                        IridiumColorAPI.process(
                                                            invalidAmount().replace("%valuename", dataeconomyvalue)
                                                        )
                                                    )
                                                )
                                            )
                                            return true
                                        }

                                        if (targetname == null || targetname.isEmpty() || targetname.isBlank()) {
                                            player.sendMessage(
                                                Format.hex(
                                                    Format.hex(
                                                        Format.color(
                                                            IridiumColorAPI.process(
                                                                playerNotFound().replace("%p", args[2])
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
                                            return false
                                        }
                                        val data_names2_sqlite =
                                            File(plugin.dataFolder.toString() + "/data/" + target!!.name + "_SQLite.txt")
                                        val data_names2_mysql =
                                            File(plugin.dataFolder.toString() + "/data/" + target.name + "_MySQL.txt")
                                        val data_names_config2_sqlite: FileConfiguration =
                                            YamlConfiguration.loadConfiguration(data_names2_sqlite)
                                        val data_names_config2_mysql: FileConfiguration =
                                            YamlConfiguration.loadConfiguration(data_names2_mysql)
                                        val data_names_sqlite = File(
                                            plugin.dataFolder.toString() + "/data/" + target
                                                .name + "_SQLite.txt"
                                        )
                                        val data_names_mysql = File(
                                            plugin.dataFolder.toString() + "/data/" + target
                                                .name + "_MySQL.txt"
                                        )
                                        val somasqlite =
                                            data_names_config2_sqlite.getInt("data.${dataeconomyvalue}") + amount
                                        val somamysql =
                                            data_names_config2_mysql.getInt("data.${dataeconomyvalue}") + amount
                                        try {
                                            if (databasetype == "h2") {
                                                TableFunction.dropTableSQLite(target)
                                            } else {
                                                TableFunction.dropTable(target)
                                            }
                                        } catch (_: SQLException) {
                                        }
                                        try {
                                            if (databasetype == "h2") {
                                                TableFunction.createTableAmountSQLite(target, somasqlite)
                                            } else {
                                                TableFunction.createTableAmount(target, somamysql)
                                            }
                                        } catch (_: SQLException) {
                                        }
                                        if (databasetype == "h2") {
                                            data_names_config2_sqlite["data.${dataeconomyvalue}"] = somasqlite
                                        } else {
                                            data_names_config2_mysql["data.${dataeconomyvalue}"] = somamysql
                                        }
                                        try {
                                            if (databasetype == "h2") {
                                                data_names_config2_sqlite.save(data_names_sqlite)
                                            } else {
                                                data_names_config2_mysql.save(data_names_mysql)
                                            }
                                        } catch (_: IOException) {
                                        }
                                        plugin.reloadConfig()
                                        target.sendMessage(
                                            Format.hex(
                                                Format.color(
                                                    IridiumColorAPI.process(
                                                        generateToOther().replace("%amount", amount.toString())
                                                            .replace("%valuename", dataeconomyvalue)
                                                            .replace("%p", player.name)
                                                    )
                                                )
                                            )
                                        )
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
                                            "hexaecon.permissions.generate"
                                        )
                                    )
                                )
                            )
                        )
                    }
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
        if (arg1.name == "wallet") {
            if (args.size == 1) {
                a(s, args[0], "generate")
                a(s, args[0], "withdraw")
                a(s, args[0], "remove")
                a(s, args[0], "reload")
                return s
            }
            if (args.size == 2) {
                if (args[0] == "withdraw") {
                    for (i in 1..100) {
                        a(s, args[1], i.toString())
                    }
                    s.sort()
                    return s
                }
                if (args[0] == "remove") {
                    for (i in 1..100) {
                        a(s, args[1], i.toString())
                    }
                    s.sort()
                    return s
                }
                if (args[0] == "generate") {
                    for (i in 1..100) {
                        a(s, args[1], i.toString())
                    }
                    s.sort()
                    return s
                }
                return mutableListOf()
            }
        }
        return null
    }

    private fun reloadPlugin() {
        plugin.reloadLanguages()
        HexaEcon.PAPI = plugin.server.pluginManager.getPlugin("PlaceholderAPI") != null
        plugin.reloadConfig()
        if (HexaEcon.PAPI) {
            HexaEconPlaceHolders().unregister()
            HexaEconPlaceHolders().register()
        }
    }
}