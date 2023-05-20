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
import space.kiyoshi.hexaecon.functions.TableFunctionMongo
import space.kiyoshi.hexaecon.functions.TableFunctionRedis
import space.kiyoshi.hexaecon.functions.TableFunctionSQL
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
                                            File(plugin.dataFolder.toString() + "/data/${sender.name}/" + sender.name + "_SQLite.txt")
                                        val data_names_mysql =
                                            File(plugin.dataFolder.toString() + "/data/${sender.name}/" + sender.name + "_MySQL.txt")
                                        val data_names_mongodb =
                                            File(plugin.dataFolder.toString() + "/data/${sender.name}/" + sender.name + "_MongoDB.txt")
                                        val data_names_redis =
                                            File(plugin.dataFolder.toString() + "/data/${sender.name}/" + sender.name + "_Redis.txt")
                                        val data_names_config_sqlite: FileConfiguration =
                                            YamlConfiguration.loadConfiguration(data_names_sqlite)
                                        val data_names_config_mysql: FileConfiguration =
                                            YamlConfiguration.loadConfiguration(data_names_mysql)
                                        val data_names_config_mongodb: FileConfiguration =
                                            YamlConfiguration.loadConfiguration(data_names_mongodb)
                                        val data_names_config_redis: FileConfiguration =
                                            YamlConfiguration.loadConfiguration(data_names_redis)
                                        val somasqlite =
                                            data_names_config_sqlite.getInt("data.${dataeconomyvalue}") - remove
                                        val somamysql =
                                            data_names_config_mysql.getInt("data.${dataeconomyvalue}") - remove
                                        val somamongodb =
                                            data_names_config_mongodb.getInt("data.${dataeconomyvalue}") - remove
                                        val somaredis =
                                            data_names_config_redis.getInt("data.${dataeconomyvalue}") - remove

                                        if (databasetype == "h2") {
                                            if (data_names_config_sqlite.getInt("data.${dataeconomyvalue}") >= remove) {
                                                try {
                                                    when (databasetype) {
                                                        "h2" -> {
                                                            TableFunctionSQL.dropTableSQLite(sender as Player)
                                                        }
                                                        "MongoDB" -> {
                                                            TableFunctionMongo.dropCollection(sender.name)
                                                        }
                                                        "MySQL" -> {
                                                            TableFunctionSQL.dropTable(sender as Player)
                                                        }
                                                        "Redis" -> {
                                                            TableFunctionRedis.dropTable(sender.name)
                                                        }
                                                    }
                                                } catch (_: SQLException) {}
                                                try {
                                                    when (databasetype) {
                                                        "h2" -> {
                                                            TableFunctionSQL.createTableAmountSQLite(player as Player, somasqlite)
                                                        }
                                                        "MongoDB" -> {
                                                            TableFunctionMongo.createCollectionAmount(sender.name, somamongodb)
                                                        }
                                                        "MySQL" -> {
                                                            TableFunctionSQL.createTableAmount(player as Player, somamysql)
                                                        }
                                                        "Redis" -> {
                                                            TableFunctionRedis.createTableAmount(sender.name, somaredis)
                                                        }
                                                    }
                                                } catch (e: SQLException) {
                                                    throw RuntimeException(e)
                                                }
                                                when (databasetype) {
                                                    "h2" -> {
                                                        data_names_config_sqlite["data.${dataeconomyvalue}"] = somasqlite
                                                    }
                                                    "MongoDB" -> {
                                                        data_names_config_mongodb["data.${dataeconomyvalue}"] = somamongodb
                                                    }
                                                    "MySQL" -> {
                                                        data_names_config_mysql["data.${dataeconomyvalue}"] = somamysql
                                                    }
                                                    "Redis" -> {
                                                        data_names_config_redis["data.${dataeconomyvalue}"] = somaredis
                                                    }
                                                }
                                                try {
                                                    when (databasetype) {
                                                        "h2" -> {
                                                            data_names_config_sqlite.save(data_names_sqlite)
                                                        }
                                                        "MongoDB" -> {
                                                            data_names_config_mongodb.save(data_names_mongodb)
                                                        }
                                                        "MySQL" -> {
                                                            data_names_config_mysql.save(data_names_mysql)
                                                        }
                                                        "Redis" -> {
                                                            data_names_config_redis.save(data_names_redis)
                                                        }
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
                                                when (databasetype) {
                                                    "h2" -> {
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
                                                    }
                                                    "MongoDB" -> {
                                                        player.sendMessage(
                                                            Format.hex(
                                                                Format.color(
                                                                    IridiumColorAPI.process(
                                                                        walletWithdrawRemainingAmount().replace(
                                                                            "%amount",
                                                                            data_names_config_mongodb.getInt("data.${dataeconomyvalue}")
                                                                                .toString()
                                                                        ).replace("%valuename", dataeconomyvalue)
                                                                    )
                                                                )
                                                            )
                                                        )
                                                    }
                                                    "MySQL" -> {
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
                                                    "Redis" -> {
                                                        player.sendMessage(
                                                            Format.hex(
                                                                Format.color(
                                                                    IridiumColorAPI.process(
                                                                        walletWithdrawRemainingAmount().replace(
                                                                            "%amount",
                                                                            data_names_config_redis.getInt("data.${dataeconomyvalue}")
                                                                                .toString()
                                                                        ).replace("%valuename", dataeconomyvalue)
                                                                    )
                                                                )
                                                            )
                                                        )
                                                    }
                                                }
                                                (player as Player).inventory.addItem(Economy.addEconomy(player, remove))
                                            } else {
                                                when (databasetype) {
                                                    "h2" -> {
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
                                                    }
                                                    "MongoDB" -> {
                                                        player.sendMessage(
                                                            Format.hex(
                                                                Format.color(
                                                                    IridiumColorAPI.process(
                                                                        walletWithdrawNoEnoughAmount().replace(
                                                                            "%amount",
                                                                            data_names_config_mongodb.getInt("data.${dataeconomyvalue}")
                                                                                .toString()
                                                                        ).replace("%valuename", dataeconomyvalue)
                                                                    )
                                                                )
                                                            )
                                                        )
                                                    }
                                                    "MySQL" -> {
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
                                                    "Redis" -> {
                                                        player.sendMessage(
                                                            Format.hex(
                                                                Format.color(
                                                                    IridiumColorAPI.process(
                                                                        walletWithdrawNoEnoughAmount().replace(
                                                                            "%amount",
                                                                            data_names_config_redis.getInt("data.${dataeconomyvalue}")
                                                                                .toString()
                                                                        ).replace("%valuename", dataeconomyvalue)
                                                                    )
                                                                )
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        } else if (databasetype == "MongoDB") {
                                            if (data_names_config_mongodb.getInt("data.${dataeconomyvalue}") >= remove) {
                                                try {
                                                    when (databasetype) {
                                                        "h2" -> {
                                                            TableFunctionSQL.dropTableSQLite(sender as Player)
                                                        }
                                                        "MongoDB" -> {
                                                            TableFunctionMongo.dropCollection(sender.name)
                                                        }
                                                        "MySQL" -> {
                                                            TableFunctionSQL.dropTable(sender as Player)
                                                        }
                                                        "Redis" -> {
                                                            TableFunctionRedis.dropTable(sender.name)
                                                        }
                                                    }
                                                } catch (_: SQLException) {}
                                                try {
                                                    when (databasetype) {
                                                        "h2" -> {
                                                            TableFunctionSQL.createTableAmountSQLite(
                                                                player as Player,
                                                                somasqlite
                                                            )
                                                        }
                                                        "MongoDB" -> {
                                                            TableFunctionMongo.createCollectionAmount(player.name, somamongodb)
                                                        }
                                                        "MySQL" -> {
                                                            TableFunctionSQL.createTableAmount(player as Player, somamysql)
                                                        }
                                                        "Redis" -> {
                                                            TableFunctionRedis.createTableAmount(player.name, somaredis)
                                                        }
                                                    }
                                                } catch (e: SQLException) {
                                                    throw RuntimeException(e)
                                                }
                                                when (databasetype) {
                                                    "h2" -> {
                                                        data_names_config_sqlite["data.${dataeconomyvalue}"] = somasqlite
                                                    }
                                                    "MongoDB" -> {
                                                        data_names_config_mongodb["data.${dataeconomyvalue}"] = somamongodb
                                                    }
                                                    "MySQL" -> {
                                                        data_names_config_mysql["data.${dataeconomyvalue}"] = somamysql
                                                    }
                                                    "Redis" -> {
                                                        data_names_config_redis["data.${dataeconomyvalue}"] = somaredis
                                                    }
                                                }
                                                try {
                                                    when (databasetype) {
                                                        "h2" -> {
                                                            data_names_config_sqlite.save(data_names_sqlite)
                                                        }
                                                        "MongoDB" -> {
                                                            data_names_config_mongodb.save(data_names_mongodb)
                                                        }
                                                        "MySQL" -> {
                                                            data_names_config_mysql.save(data_names_mysql)
                                                        }
                                                        "Redis" -> {
                                                            data_names_config_redis.save(data_names_redis)
                                                        }
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
                                                when (databasetype) {
                                                    "h2" -> {
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
                                                    }
                                                    "MongoDB" -> {
                                                        player.sendMessage(
                                                            Format.hex(
                                                                Format.color(
                                                                    IridiumColorAPI.process(
                                                                        walletWithdrawRemainingAmount().replace(
                                                                            "%amount",
                                                                            data_names_config_mongodb.getInt("data.${dataeconomyvalue}")
                                                                                .toString()
                                                                        ).replace("%valuename", dataeconomyvalue)
                                                                    )
                                                                )
                                                            )
                                                        )
                                                    }
                                                    "MySQL" -> {
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
                                                    "Redis" -> {
                                                        player.sendMessage(
                                                            Format.hex(
                                                                Format.color(
                                                                    IridiumColorAPI.process(
                                                                        walletWithdrawRemainingAmount().replace(
                                                                            "%amount",
                                                                            data_names_config_redis.getInt("data.${dataeconomyvalue}")
                                                                                .toString()
                                                                        ).replace("%valuename", dataeconomyvalue)
                                                                    )
                                                                )
                                                            )
                                                        )
                                                    }
                                                }
                                                if(sender is Player) {
                                                    sender.inventory.addItem(Economy.addEconomy(sender, remove))
                                                }
                                            } else {
                                                when (databasetype) {
                                                    "h2" -> {
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
                                                    }
                                                    "MongoDB" -> {
                                                        player.sendMessage(
                                                            Format.hex(
                                                                Format.color(
                                                                    IridiumColorAPI.process(
                                                                        walletWithdrawNoEnoughAmount().replace(
                                                                            "%amount",
                                                                            data_names_config_mongodb.getInt("data.${dataeconomyvalue}")
                                                                                .toString()
                                                                        ).replace("%valuename", dataeconomyvalue)
                                                                    )
                                                                )
                                                            )
                                                        )
                                                    }
                                                    "MySQL" -> {
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
                                                    "Redis" -> {
                                                        player.sendMessage(
                                                            Format.hex(
                                                                Format.color(
                                                                    IridiumColorAPI.process(
                                                                        walletWithdrawNoEnoughAmount().replace(
                                                                            "%amount",
                                                                            data_names_config_redis.getInt("data.${dataeconomyvalue}")
                                                                                .toString()
                                                                        ).replace("%valuename", dataeconomyvalue)
                                                                    )
                                                                )
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        } else if (databasetype == "MySQL") {
                                            if (data_names_config_mysql.getInt("data.${dataeconomyvalue}") >= remove) {
                                                try {
                                                    when (databasetype) {
                                                        "h2" -> {
                                                            TableFunctionSQL.dropTableSQLite(sender as Player)
                                                        }
                                                        "MongoDB" -> {
                                                            TableFunctionMongo.dropCollection(sender.name)
                                                        }
                                                        "MySQL" -> {
                                                            TableFunctionSQL.dropTable(sender as Player)
                                                        }
                                                        "Redis" -> {
                                                            TableFunctionRedis.dropTable(sender.name)
                                                        }
                                                    }
                                                } catch (_: SQLException) {}
                                                try {
                                                    when (databasetype) {
                                                        "h2" -> {
                                                            TableFunctionSQL.createTableAmountSQLite(
                                                                player as Player,
                                                                somasqlite
                                                            )
                                                        }
                                                        "MongoDB" -> {
                                                            TableFunctionMongo.createCollectionAmount(
                                                                player.name,
                                                                somasqlite
                                                            )
                                                        }
                                                        "MySQL" -> {
                                                            TableFunctionSQL.createTableAmount(player as Player, somamysql)
                                                        }
                                                        "Redis" -> {
                                                            TableFunctionRedis.createTableAmount(player.name, somaredis)
                                                        }
                                                    }
                                                } catch (e: SQLException) {
                                                    throw RuntimeException(e)
                                                }
                                                when (databasetype) {
                                                    "h2" -> {
                                                        data_names_config_sqlite["data.${dataeconomyvalue}"] = somasqlite
                                                    }
                                                    "MongoDB" -> {
                                                        data_names_config_mongodb["data.${dataeconomyvalue}"] = somamongodb
                                                    }
                                                    "MySQL" -> {
                                                        data_names_config_mysql["data.${dataeconomyvalue}"] = somamysql
                                                    }
                                                    "Redis" -> {
                                                        data_names_config_redis["data.${dataeconomyvalue}"] = somaredis
                                                    }
                                                }
                                                try {
                                                    when (databasetype) {
                                                        "h2" -> {
                                                            data_names_config_sqlite.save(data_names_sqlite)
                                                        }
                                                        "MongoDB" -> {
                                                            data_names_config_mongodb.save(data_names_mongodb)
                                                        }
                                                        "MySQL" -> {
                                                            data_names_config_mysql.save(data_names_mysql)
                                                        }
                                                        "Redis" -> {
                                                            data_names_config_redis.save(data_names_redis)
                                                        }
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
                                                when (databasetype) {
                                                    "h2" -> {
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
                                                    }
                                                    "MongoDB" -> {
                                                        player.sendMessage(
                                                            Format.hex(
                                                                Format.color(
                                                                    IridiumColorAPI.process(
                                                                        walletWithdrawRemainingAmount().replace(
                                                                            "%amount",
                                                                            data_names_config_mongodb.getInt("data.${dataeconomyvalue}")
                                                                                .toString()
                                                                        ).replace("%valuename", dataeconomyvalue)
                                                                    )
                                                                )
                                                            )
                                                        )
                                                    }
                                                    "MySQL" -> {
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
                                                    "Redis" -> {
                                                        player.sendMessage(
                                                            Format.hex(
                                                                Format.color(
                                                                    IridiumColorAPI.process(
                                                                        walletWithdrawRemainingAmount().replace(
                                                                            "%amount",
                                                                            data_names_config_redis.getInt("data.${dataeconomyvalue}")
                                                                                .toString()
                                                                        ).replace("%valuename", dataeconomyvalue)
                                                                    )
                                                                )
                                                            )
                                                        )
                                                    }
                                                }
                                                if(sender is Player) {
                                                    sender.inventory.addItem(Economy.addEconomy(sender, remove))
                                                }
                                            } else {
                                                when (databasetype) {
                                                    "h2" -> {
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
                                                    }
                                                    "MongoDB" -> {
                                                        player.sendMessage(
                                                            Format.hex(
                                                                Format.color(
                                                                    IridiumColorAPI.process(
                                                                        walletWithdrawNoEnoughAmount().replace(
                                                                            "%amount",
                                                                            data_names_config_mongodb.getInt("data.${dataeconomyvalue}")
                                                                                .toString()
                                                                        ).replace("%valuename", dataeconomyvalue)
                                                                    )
                                                                )
                                                            )
                                                        )
                                                    }
                                                    "MySQL" -> {
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
                                                    "Redis" -> {
                                                        player.sendMessage(
                                                            Format.hex(
                                                                Format.color(
                                                                    IridiumColorAPI.process(
                                                                        walletWithdrawNoEnoughAmount().replace(
                                                                            "%amount",
                                                                            data_names_config_redis.getInt("data.${dataeconomyvalue}")
                                                                                .toString()
                                                                        ).replace("%valuename", dataeconomyvalue)
                                                                    )
                                                                )
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        } else if (databasetype == "Redis") {
                                            if (data_names_config_redis.getInt("data.${dataeconomyvalue}") >= remove) {
                                                try {
                                                    when (databasetype) {
                                                        "h2" -> {
                                                            TableFunctionSQL.dropTableSQLite(sender as Player)
                                                        }
                                                        "MongoDB" -> {
                                                            TableFunctionMongo.dropCollection(sender.name)
                                                        }
                                                        "MySQL" -> {
                                                            TableFunctionSQL.dropTable(sender as Player)
                                                        }
                                                        "Redis" -> {
                                                            TableFunctionRedis.dropTable(sender.name)
                                                        }
                                                    }
                                                } catch (_: SQLException) {}
                                                try {
                                                    when (databasetype) {
                                                        "h2" -> {
                                                            TableFunctionSQL.createTableAmountSQLite(
                                                                player as Player,
                                                                somasqlite
                                                            )
                                                        }
                                                        "MongoDB" -> {
                                                            TableFunctionMongo.createCollectionAmount(player.name, somamongodb)
                                                        }
                                                        "MySQL" -> {
                                                            TableFunctionSQL.createTableAmount(player as Player, somamysql)
                                                        }
                                                        "Redis" -> {
                                                            TableFunctionRedis.createTableAmount(player.name, somaredis)
                                                        }
                                                    }
                                                } catch (e: SQLException) {
                                                    throw RuntimeException(e)
                                                }
                                                when (databasetype) {
                                                    "h2" -> {
                                                        data_names_config_sqlite["data.${dataeconomyvalue}"] = somasqlite
                                                    }
                                                    "MongoDB" -> {
                                                        data_names_config_mongodb["data.${dataeconomyvalue}"] = somamongodb
                                                    }
                                                    "MySQL" -> {
                                                        data_names_config_mysql["data.${dataeconomyvalue}"] = somamysql
                                                    }
                                                    "Redis" -> {
                                                        data_names_config_redis["data.${dataeconomyvalue}"] = somaredis
                                                    }
                                                }
                                                try {
                                                    when (databasetype) {
                                                        "h2" -> {
                                                            data_names_config_sqlite.save(data_names_sqlite)
                                                        }
                                                        "MongoDB" -> {
                                                            data_names_config_mongodb.save(data_names_mongodb)
                                                        }
                                                        "MySQL" -> {
                                                            data_names_config_mysql.save(data_names_mysql)
                                                        }
                                                        "Redis" -> {
                                                            data_names_config_redis.save(data_names_redis)
                                                        }
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
                                                when (databasetype) {
                                                    "h2" -> {
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
                                                    }
                                                    "MongoDB" -> {
                                                        player.sendMessage(
                                                            Format.hex(
                                                                Format.color(
                                                                    IridiumColorAPI.process(
                                                                        walletWithdrawRemainingAmount().replace(
                                                                            "%amount",
                                                                            data_names_config_mongodb.getInt("data.${dataeconomyvalue}")
                                                                                .toString()
                                                                        ).replace("%valuename", dataeconomyvalue)
                                                                    )
                                                                )
                                                            )
                                                        )
                                                    }
                                                    "MySQL" -> {
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
                                                    "Redis" -> {
                                                        player.sendMessage(
                                                            Format.hex(
                                                                Format.color(
                                                                    IridiumColorAPI.process(
                                                                        walletWithdrawRemainingAmount().replace(
                                                                            "%amount",
                                                                            data_names_config_redis.getInt("data.${dataeconomyvalue}")
                                                                                .toString()
                                                                        ).replace("%valuename", dataeconomyvalue)
                                                                    )
                                                                )
                                                            )
                                                        )
                                                    }
                                                }
                                                if(sender is Player) {
                                                    sender.inventory.addItem(Economy.addEconomy(sender, remove))
                                                }
                                            } else {
                                                when (databasetype) {
                                                    "h2" -> {
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
                                                    }
                                                    "MongoDB" -> {
                                                        player.sendMessage(
                                                            Format.hex(
                                                                Format.color(
                                                                    IridiumColorAPI.process(
                                                                        walletWithdrawNoEnoughAmount().replace(
                                                                            "%amount",
                                                                            data_names_config_mongodb.getInt("data.${dataeconomyvalue}")
                                                                                .toString()
                                                                        ).replace("%valuename", dataeconomyvalue)
                                                                    )
                                                                )
                                                            )
                                                        )
                                                    }
                                                    "MySQL" -> {
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
                                                    "Redis" -> {
                                                        player.sendMessage(
                                                            Format.hex(
                                                                Format.color(
                                                                    IridiumColorAPI.process(
                                                                        walletWithdrawNoEnoughAmount().replace(
                                                                            "%amount",
                                                                            data_names_config_redis.getInt("data.${dataeconomyvalue}")
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
                                            File(plugin.dataFolder.toString() + "/data/${target!!.name}/" + target.name + "_SQLite.txt")
                                        val data_names_mysql =
                                            File(plugin.dataFolder.toString() + "/data/${target.name}/" + target.name + "_MySQL.txt")
                                        val data_names_mongodb =
                                            File(plugin.dataFolder.toString() + "/data/${target.name}/" + target.name + "_MongoDB.txt")
                                        val data_names_redis =
                                            File(plugin.dataFolder.toString() + "/data/${target.name}/" + target.name + "_Redis.txt")
                                        val data_names_config_sqlite: FileConfiguration =
                                            YamlConfiguration.loadConfiguration(data_names_sqlite)
                                        val data_names_config_mysql: FileConfiguration =
                                            YamlConfiguration.loadConfiguration(data_names_mysql)
                                        val data_names_config_mongodb: FileConfiguration =
                                            YamlConfiguration.loadConfiguration(data_names_mongodb)
                                        val data_names_config_redis: FileConfiguration =
                                            YamlConfiguration.loadConfiguration(data_names_redis)
                                        val somasqlite =
                                            data_names_config_sqlite.getInt("data.${dataeconomyvalue}") - remove
                                        val somamysql =
                                            data_names_config_mysql.getInt("data.${dataeconomyvalue}") - remove
                                        val somamongodb =
                                            data_names_config_mongodb.getInt("data.${dataeconomyvalue}") - remove
                                        val somaredis =
                                            data_names_config_redis.getInt("data.${dataeconomyvalue}") - remove

                                        if (databasetype == "h2") {
                                            if (data_names_config_sqlite.getInt("data.${dataeconomyvalue}") >= remove) {
                                                try {
                                                    when (databasetype) {
                                                        "h2" -> {
                                                            TableFunctionSQL.dropTableSQLite(target)
                                                        }
                                                        "MongoDB" -> {
                                                            TableFunctionMongo.dropCollection(target.name)
                                                        }
                                                        "MySQL" -> {
                                                            TableFunctionSQL.dropTable(target)
                                                        }
                                                        "Redis" -> {
                                                            TableFunctionRedis.dropTable(target.name)
                                                        }
                                                    }
                                                } catch (_: SQLException) {}
                                                try {
                                                    when (databasetype) {
                                                        "h2" -> {
                                                            TableFunctionSQL.createTableAmountSQLite(target, somasqlite)
                                                        }
                                                        "MongoDB" -> {
                                                            TableFunctionMongo.createCollectionAmount(target.name, somamongodb)
                                                        }
                                                        "MySQL" -> {
                                                            TableFunctionSQL.createTableAmount(target, somamysql)
                                                        }
                                                        "Redis" -> {
                                                            TableFunctionRedis.createTableAmount(target.name, somaredis)
                                                        }
                                                    }
                                                } catch (e: SQLException) {
                                                    throw RuntimeException(e)
                                                }
                                                when (databasetype) {
                                                    "h2" -> {
                                                        data_names_config_sqlite["data.${dataeconomyvalue}"] = somasqlite
                                                    }
                                                    "MongoDB" -> {
                                                        data_names_config_mongodb["data.${dataeconomyvalue}"] = somamongodb
                                                    }
                                                    "MySQL" -> {
                                                        data_names_config_mysql["data.${dataeconomyvalue}"] = somamysql
                                                    }
                                                    "Redis" -> {
                                                        data_names_config_redis["data.${dataeconomyvalue}"] = somaredis
                                                    }
                                                }
                                                try {
                                                    when (databasetype) {
                                                        "h2" -> {
                                                            data_names_config_sqlite.save(data_names_sqlite)
                                                        }
                                                        "MongoDB" -> {
                                                            data_names_config_mongodb.save(data_names_mongodb)
                                                        }
                                                        "MySQL" -> {
                                                            data_names_config_mysql.save(data_names_mysql)
                                                        }
                                                        "Redis" -> {
                                                            data_names_config_redis.save(data_names_redis)
                                                        }
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
                                        } else if (databasetype == "MongoDB") {
                                            if (data_names_config_mongodb.getInt("data.${dataeconomyvalue}") >= remove) {
                                                try {
                                                    when (databasetype) {
                                                        "h2" -> {
                                                            TableFunctionSQL.dropTableSQLite(target)
                                                        }
                                                        "MongoDB" -> {
                                                            TableFunctionMongo.dropCollection(target.name)
                                                        }
                                                        "MySQL" -> {
                                                            TableFunctionSQL.dropTable(target)
                                                        }
                                                        "Redis" -> {
                                                            TableFunctionRedis.dropTable(target.name)
                                                        }
                                                    }
                                                } catch (_: SQLException) {}
                                                try {
                                                    when (databasetype) {
                                                        "h2" -> {
                                                            TableFunctionSQL.createTableAmountSQLite(target, somasqlite)
                                                        }
                                                        "MongoDB" -> {
                                                            TableFunctionMongo.createCollectionAmount(target.name, somamongodb)
                                                        }
                                                        "MySQL" -> {
                                                            TableFunctionSQL.createTableAmount(target, somamysql)
                                                        }
                                                        "Redis" -> {
                                                            TableFunctionRedis.createTableAmount(target.name, somaredis)
                                                        }
                                                    }
                                                } catch (e: SQLException) {
                                                    throw RuntimeException(e)
                                                }
                                                when (databasetype) {
                                                    "h2" -> {
                                                        data_names_config_sqlite["data.${dataeconomyvalue}"] = somasqlite
                                                    }
                                                    "MongoDB" -> {
                                                        data_names_config_mongodb["data.${dataeconomyvalue}"] = somamongodb
                                                    }
                                                    "MySQL" -> {
                                                        data_names_config_mysql["data.${dataeconomyvalue}"] = somamysql
                                                    }
                                                    "Redis" -> {
                                                        data_names_config_redis["data.${dataeconomyvalue}"] = somaredis
                                                    }
                                                }
                                                try {
                                                    when (databasetype) {
                                                        "h2" -> {
                                                            data_names_config_sqlite.save(data_names_sqlite)
                                                        }
                                                        "MongoDB" -> {
                                                            data_names_config_mongodb.save(data_names_mongodb)
                                                        }
                                                        "MySQL" -> {
                                                            data_names_config_mysql.save(data_names_mysql)
                                                        }
                                                        "Redis" -> {
                                                            data_names_config_redis.save(data_names_redis)
                                                        }
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
                                                                        data_names_config_mongodb.getInt("data.${dataeconomyvalue}")
                                                                            .toString()
                                                                    )
                                                            )
                                                        )
                                                    )
                                                )
                                            }
                                        } else if (databasetype == "MySQL") {
                                            if (data_names_config_mysql.getInt("data.${dataeconomyvalue}") >= remove) {
                                                try {
                                                    when (databasetype) {
                                                        "h2" -> {
                                                            TableFunctionSQL.dropTableSQLite(target)
                                                        }
                                                        "MongoDB" -> {
                                                            TableFunctionMongo.dropCollection(target.name)
                                                        }
                                                        "MySQL" -> {
                                                            TableFunctionSQL.dropTable(target)
                                                        }
                                                        "Redis" -> {
                                                            TableFunctionRedis.dropTable(target.name)
                                                        }
                                                    }
                                                } catch (_: SQLException) {}
                                                try {
                                                    when (databasetype) {
                                                        "h2" -> {
                                                            TableFunctionSQL.createTableAmountSQLite(target, somasqlite)
                                                        }
                                                        "MongoDB" -> {
                                                            TableFunctionMongo.createCollectionAmount(target.name, somamongodb)
                                                        }
                                                        "MySQL" -> {
                                                            TableFunctionSQL.createTableAmount(target, somamysql)
                                                        }
                                                        "Redis" -> {
                                                            TableFunctionRedis.createTableAmount(target.name, somaredis)
                                                        }
                                                    }
                                                } catch (e: SQLException) {
                                                    throw RuntimeException(e)
                                                }
                                                when (databasetype) {
                                                    "h2" -> {
                                                        data_names_config_sqlite["data.${dataeconomyvalue}"] = somasqlite
                                                    }
                                                    "MongoDB" -> {
                                                        data_names_config_mongodb["data.${dataeconomyvalue}"] = somamongodb
                                                    }
                                                    "MySQL" -> {
                                                        data_names_config_mysql["data.${dataeconomyvalue}"] = somamysql
                                                    }
                                                    "Redis" -> {
                                                        data_names_config_redis["data.${dataeconomyvalue}"] = somaredis
                                                    }
                                                }
                                                try {
                                                    when (databasetype) {
                                                        "h2" -> {
                                                            data_names_config_sqlite.save(data_names_sqlite)
                                                        }
                                                        "MongoDB" -> {
                                                            data_names_config_mongodb.save(data_names_mongodb)
                                                        }
                                                        "MySQL" -> {
                                                            data_names_config_mysql.save(data_names_mysql)
                                                        }
                                                        "Redis" -> {
                                                            data_names_config_redis.save(data_names_redis)
                                                        }
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
                                        } else if (databasetype == "Redis") {
                                            if (data_names_config_redis.getInt("data.${dataeconomyvalue}") >= remove) {
                                                try {
                                                    when (databasetype) {
                                                        "h2" -> {
                                                            TableFunctionSQL.dropTableSQLite(target)
                                                        }
                                                        "MongoDB" -> {
                                                            TableFunctionMongo.dropCollection(target.name)
                                                        }
                                                        "MySQL" -> {
                                                            TableFunctionSQL.dropTable(target)
                                                        }
                                                        "Redis" -> {
                                                            TableFunctionRedis.dropTable(target.name)
                                                        }
                                                    }
                                                } catch (_: SQLException) {}
                                                try {
                                                    when (databasetype) {
                                                        "h2" -> {
                                                            TableFunctionSQL.createTableAmountSQLite(target, somasqlite)
                                                        }
                                                        "MongoDB" -> {
                                                            TableFunctionMongo.createCollectionAmount(target.name, somamongodb)
                                                        }
                                                        "MySQL" -> {
                                                            TableFunctionSQL.createTableAmount(target, somamysql)
                                                        }
                                                        "Redis" -> {
                                                            TableFunctionRedis.createTableAmount(target.name, somaredis)
                                                        }
                                                    }
                                                } catch (e: SQLException) {
                                                    throw RuntimeException(e)
                                                }
                                                when (databasetype) {
                                                    "h2" -> {
                                                        data_names_config_sqlite["data.${dataeconomyvalue}"] = somasqlite
                                                    }
                                                    "MongoDB" -> {
                                                        data_names_config_mongodb["data.${dataeconomyvalue}"] = somamongodb
                                                    }
                                                    "MySQL" -> {
                                                        data_names_config_mysql["data.${dataeconomyvalue}"] = somamysql
                                                    }
                                                    "Redis" -> {
                                                        data_names_config_redis["data.${dataeconomyvalue}"] = somaredis
                                                    }
                                                }
                                                try {
                                                    when (databasetype) {
                                                        "h2" -> {
                                                            data_names_config_sqlite.save(data_names_sqlite)
                                                        }
                                                        "MongoDB" -> {
                                                            data_names_config_mongodb.save(data_names_mongodb)
                                                        }
                                                        "MySQL" -> {
                                                            data_names_config_mysql.save(data_names_mysql)
                                                        }
                                                        "Redis" -> {
                                                            data_names_config_redis.save(data_names_redis)
                                                        }
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
                                                                        data_names_config_mongodb.getInt("data.${dataeconomyvalue}")
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
                                            File(plugin.dataFolder.toString() + "/data/${target!!.name}/" + target.name + "_SQLite.txt")
                                        val data_names2_mysql =
                                            File(plugin.dataFolder.toString() + "/data/${target.name}/" + target.name + "_MySQL.txt")
                                        val data_names2_mongodb =
                                            File(plugin.dataFolder.toString() + "/data/${target.name}/" + target.name + "_MongoDB.txt")
                                        val data_names2_redis =
                                            File(plugin.dataFolder.toString() + "/data/${target.name}/" + target.name + "_Redis.txt")
                                        val data_names_config2_sqlite: FileConfiguration =
                                            YamlConfiguration.loadConfiguration(data_names2_sqlite)
                                        val data_names_config2_mysql: FileConfiguration =
                                            YamlConfiguration.loadConfiguration(data_names2_mysql)
                                        val data_names_config2_mongodb: FileConfiguration =
                                            YamlConfiguration.loadConfiguration(data_names2_mongodb)
                                        val data_names_config2_redis: FileConfiguration =
                                            YamlConfiguration.loadConfiguration(data_names2_redis)
                                        val data_names_sqlite = File(
                                            plugin.dataFolder.toString() + "/data/${target.name}/" + target
                                                .name + "_SQLite.txt"
                                        )
                                        val data_names_mysql = File(
                                            plugin.dataFolder.toString() + "/data/${target.name}/" + target
                                                .name + "_MySQL.txt"
                                        )
                                        val data_names_mongodb = File(
                                            plugin.dataFolder.toString() + "/data/${target.name}/" + target
                                                .name + "_MongoDB.txt"
                                        )
                                        val data_names_redis = File(
                                            plugin.dataFolder.toString() + "/data/${target.name}/" + target
                                                .name + "_Redis.txt"
                                        )
                                        val somasqlite =
                                            data_names_config2_sqlite.getInt("data.${dataeconomyvalue}") + amount
                                        val somamysql =
                                            data_names_config2_mysql.getInt("data.${dataeconomyvalue}") + amount
                                        val somamongodb =
                                            data_names_config2_mongodb.getInt("data.${dataeconomyvalue}") + amount
                                        val somaredis =
                                            data_names_config2_redis.getInt("data.${dataeconomyvalue}") + amount
                                        try {
                                            when (databasetype) {
                                                "h2" -> {
                                                    TableFunctionSQL.dropTableSQLite(target)
                                                }
                                                "MongoDB" -> {
                                                    TableFunctionMongo.dropCollection(target.name)
                                                }
                                                "MySQL" -> {
                                                    TableFunctionSQL.dropTable(target)
                                                }
                                                "Redis" -> {
                                                    TableFunctionRedis.dropTable(target.name)
                                                }
                                            }
                                        } catch (_: SQLException) {}
                                        try {
                                            when (databasetype) {
                                                "h2" -> {
                                                    TableFunctionSQL.createTableAmountSQLite(target, somasqlite)
                                                }
                                                "MongoDB" -> {
                                                    TableFunctionMongo.createCollectionAmount(target.name, somamongodb)
                                                }
                                                "MySQL" -> {
                                                    TableFunctionSQL.createTableAmount(target, somamysql)
                                                }
                                                "Redis" -> {
                                                    TableFunctionRedis.createTableAmount(target.name, somaredis)
                                                }
                                            }
                                        } catch (_: SQLException) {}
                                        when (databasetype) {
                                            "h2" -> {
                                                data_names_config2_sqlite["data.${dataeconomyvalue}"] = somasqlite
                                            }
                                            "MongoDB" -> {
                                                data_names_config2_mongodb["data.${dataeconomyvalue}"] = somamongodb
                                            }
                                            "MySQL" -> {
                                                data_names_config2_mysql["data.${dataeconomyvalue}"] = somamysql
                                            }
                                            "Redis" -> {
                                                data_names_config2_redis["data.${dataeconomyvalue}"] = somaredis
                                            }
                                        }
                                        try {
                                            when (databasetype) {
                                                "h2" -> {
                                                    data_names_config2_sqlite.save(data_names_sqlite)
                                                }
                                                "MongoDB" -> {
                                                    data_names_config2_mongodb.save(data_names_mongodb)
                                                }
                                                "MySQL" -> {
                                                    data_names_config2_mysql.save(data_names_mysql)
                                                }
                                                "Redis" -> {
                                                    data_names_config2_redis.save(data_names_redis)
                                                }
                                            }
                                        } catch (_: IOException) {}
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