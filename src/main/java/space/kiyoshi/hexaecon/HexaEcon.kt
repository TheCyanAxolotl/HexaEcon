/*
 * Copyright (c) 2023. KiyoshiDevelopment
 *   All rights reserved.
 */

@file:Suppress("SpellCheckingInspection", "SameParameterValue")

package space.kiyoshi.hexaecon

import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import space.kiyoshi.hexaecon.commands.EcoCommand
import space.kiyoshi.hexaecon.commands.PayCommand
import space.kiyoshi.hexaecon.commands.WalletCommand
import space.kiyoshi.hexaecon.listeners.EventListener
import space.kiyoshi.hexaecon.mongo.MongoDBManager
import space.kiyoshi.hexaecon.redis.RedisManager
import space.kiyoshi.hexaecon.sql.MySQLManager
import space.kiyoshi.hexaecon.sql.SQLiteManager
import space.kiyoshi.hexaecon.utils.HexaEconPlaceHolders
import space.kiyoshi.hexaecon.utils.KiyoshiLogger
import space.kiyoshi.hexaecon.utils.NMSUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.Channels
import java.util.logging.Level
import java.util.logging.LogRecord
import kotlin.properties.Delegates

class HexaEcon : JavaPlugin() {
    private val nms = NMSUtils
    private var lang = File(dataFolder, "language")
    private var languagefile = File("$dataFolder/language/language.yml")
    private var languageconfig: FileConfiguration = YamlConfiguration.loadConfiguration(languagefile)
    private val mongohost = config.getString("MongoDB.Host")!!
    private val redishost = config.getString("Redis.Host")!!
    private val redisport = config.getInt("Redis.Port")
    private fun initialize() {
        saveDefaultConfig()
        if(nms.checkLegacyVersion(nms.getCleanServerVersion())) {
            if(config.getString("config") == "latest") {
                replaceLegacyConfig()
            }
        }
        config.options().copyDefaults(true)
        if (nms.checkServerVersionUp(nms.getCleanServerVersion())) {
            KiyoshiLogger.log(
                LogRecord(
                    Level.INFO,
                    "Running HexaEcon on spigot version ${nms.getCleanServerVersion()}"
                ), "HexaEconSupport"
            )
            @Suppress("DEPRECATION")
            config.options().copyHeader(true)
        } else {
            KiyoshiLogger.log(
                LogRecord(
                    Level.INFO,
                    "Running HexaEcon on spigot version ${nms.getCleanServerVersion()}"
                ), "HexaEconSupport"
            )
            config.options().parseComments(true)
        }
        plugin = this
    }

    private fun configs() {
        if (!lang.exists()) {
            lang.mkdir()
        }
        if (!languagefile.exists()) {
            try {
                languagefile.createNewFile()
                languageconfig.createSection("Formatted")
                languageconfig["Formatted.Amount"] = "&e%amountformatted%"
                languageconfig.createSection("Language")
                languageconfig["Language.Prefix"] = "&e[&9HexaEcon&e]&r "
                languageconfig["Language.IsConsolePlayer"] = "&esorry but you cannot execute this command in console."
                languageconfig["Language.BankAmount"] = "&eyou have &6%amountformatted% &e%valuename%."
                languageconfig["Language.GenericEarn"] = "&e+ &6%amountformatted% &e%valuename%."
                languageconfig["Language.ConfigurationReloaded"] = "&econfiguration files successfully reloaded."
                languageconfig["Language.GenerateToOther"] = "&e+ &6%amountformatted% &e%valuename% generated by &6%p%"
                languageconfig["Language.AccessDenied"] =
                    "&eyou don't have access to this command permission required: &6%perm%"
                languageconfig["Language.InvalidAmount"] =
                    "&erequired minimum &61 &e%valuename% &eor check if is a valid number."
                languageconfig["Language.WalletWithdrawAmount"] = "&e- &6%amountformatted% &e%valuename%."
                languageconfig["Language.PlayerNotFound"] = "&eplayer &6%p% &enot found or not online."
                languageconfig["Language.WalletWithdrawConverted"] = "&6%amountformatted% &e%valuename% converted."
                languageconfig["Language.WalletWithdrawRemaningAmount"] = "&ethey still remain %valuename% &6%amountformatted%."
                languageconfig["Language.WalletWithdrawNoEnoughAmount"] =
                    "&eyou don't have enough %valuename%, you have &6%amountformatted% &e%valuename%."
                languageconfig["Language.PlayerPayed"] = "&eYou have successfully sent to &6%p &6%amountformatted% &e%valuename%."
                languageconfig["Language.PlayerPaymentRecived"] =
                    "&eYou recived a payment of &6%amountformatted &e%valuename% &eby &6%p%."
                languageconfig["Language.CannotPaySelf"] = "&eYou cannot pay yourself."
                languageconfig["Language.RemovedEconFromPlayer"] = "&eYou have successfully removed &6%amountformatted% &e%valuename% from &6%p%."
                languageconfig["Language.CannotRemoveEconFromPlayer"] = "&eCannot remove &6%amountformatted% &e%valuename% from &6%p% &ebecause player has &6%targetbalance% &e%valuename%."
                languageconfig["Language.UsageFormat"] = "&eUsage: %u%"
                languageconfig.createSection("Usages")
                languageconfig["Usages.EconConvertDeposit"] = "&e/wallet <generate/withdraw/remove> <amount> <player>"
                languageconfig["Usages.Pay"] = "&e/pay <amount> <player>"
                languageconfig.save(languagefile)
            } catch (_: IOException) {
                KiyoshiLogger.log(LogRecord(Level.SEVERE, "[Error] Error while creating language file."), "HexaEcon")
            }
        }
        getLanguages().options().copyDefaults(true)
        saveLanguages()
    }

    private fun database() {
        val databasetype = config.getString("DataBase.Type")!!
        if (databasetype.isEmpty() || databasetype.isBlank() || databasetype.equals(null)) {
            val worlds = Bukkit.getWorlds()
            for (world in worlds) {
                world.save()
            }
            KiyoshiLogger.log(
                LogRecord(
                    Level.WARNING,
                    "[DataBase] You need to connect the DataBase to use the plugin, check the config.yml"
                ), "HexaEcon"
            )
            server.shutdown()
        } else if (databasetype == "h2") {
            SQLiteManager = SQLiteManager("$dataFolder/data.db")
            SQLiteManager?.connect()
            if (SQLiteManager?.isConnected == true) {
                KiyoshiLogger.log(
                    LogRecord(Level.INFO, "[SQLite] pulling sqlite requests from HexaEcon [OK]"),
                    "HexaEcon"
                )
            }
        } else if (databasetype == "MongoDB") {
            KiyoshiLogger.log(
                LogRecord(Level.INFO, "[MongoDB] pulling mongodb requests from HexaEcon [OK]"),
                "HexaEcon"
            )
        } else if (databasetype == "MySQL") {
            MySQLManager = MySQLManager()
            MySQLManager?.connect()
            if (MySQLManager?.isConnected == true) {
                KiyoshiLogger.log(
                    LogRecord(Level.INFO, "[MySQL] pulling mysql requests from HexaEcon [OK]"),
                    "HexaEcon"
                )
            }
        } else if (databasetype == "Redis") {
            KiyoshiLogger.log(
                LogRecord(Level.INFO, "[Redis] pulling redis requests from HexaEcon [OK]"),
                "HexaEcon"
            )
            RedisManager(redishost, redisport)
        }
    }

    private fun events() {
        server.pluginManager.registerEvents(EventListener(), this)
    }

    private fun commands() {
        getCommand("eco")!!.setExecutor(EcoCommand())
        getCommand("wallet")!!.setExecutor(WalletCommand())
        getCommand("pay")!!.setExecutor(PayCommand())
    }

    override fun onEnable() {
        initialize()
        configs()
        database()
        events()
        commands()
        PAPI = server.pluginManager.getPlugin("PlaceholderAPI") != null
        if (PAPI) {
            KiyoshiLogger.log(LogRecord(Level.INFO, "PlaceholderAPI found."), "HexaEcon")
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                HexaEconPlaceHolders().register()
            } else {
                KiyoshiLogger.log(LogRecord(Level.INFO, "PlaceholderAPI found but not enabled."), "HexaEcon")
            }
        } else {
            KiyoshiLogger.log(LogRecord(Level.SEVERE, "PlaceholderAPI not found."), "HexaEcon")
        }
        printBanner()
    }

    override fun onDisable() {
        val databasetype = config.getString("DataBase.Type")!!
        MySQLManager = MySQLManager()
        SQLiteManager = SQLiteManager("$dataFolder/data.db")
        when (databasetype) {
            "h2" -> {
                try {
                    SQLiteManager!!.disconnect()
                } catch (_: ClassNotFoundException) {}
            }
            "MongoDB" -> {
                try {
                    MongoDBManager(mongohost, "hexaecon").close()
                } catch (_: ClassNotFoundException) {}
            }
            "MySQL" -> {
                try {
                    MySQLManager!!.disconnect()
                } catch (_: ClassNotFoundException) {}
            }
            "Redis" -> {
                try {
                    RedisManager(redishost, redisport).disconnect()
                } catch (_: ClassNotFoundException) {}
            }
        }
    }


    companion object {
        var plugin: HexaEcon by Delegates.notNull()
        var MySQLManager: MySQLManager? = null
        var SQLiteManager: SQLiteManager? = null
        var PAPI = false
    }

    private fun printBanner() {
        KiyoshiLogger.log(LogRecord(Level.INFO, "           HexaEcon by TheUwUAxolotl        "), "HexaEcon")
        KiyoshiLogger.log(LogRecord(Level.INFO, "***********************************************"), "HexaEcon")
        KiyoshiLogger.log(LogRecord(Level.INFO, "*                                             *"), "HexaEcon")
        KiyoshiLogger.log(LogRecord(Level.INFO, "*          HexaEcon has been enabled          *"), "HexaEcon")
        KiyoshiLogger.log(LogRecord(Level.INFO, "*                                             *"), "HexaEcon")
        KiyoshiLogger.log(LogRecord(Level.INFO, "***********************************************"), "HexaEcon")
    }

    fun getLanguages(): FileConfiguration {
        return languageconfig
    }

    private fun saveLanguages() {
        try {
            languageconfig.save(languagefile)
        } catch (e: IOException) {
            KiyoshiLogger.log(LogRecord(Level.SEVERE, "Could not save language configuration"), "HexaEcon")
        }
    }

    fun reloadLanguages() {
        languageconfig = YamlConfiguration.loadConfiguration(languagefile)
    }

    private fun replaceLegacyConfig() {
        val currentConfigFile = File("$dataFolder/config.yml")
        val legacyConfigFile = File("$dataFolder/config-legacy.yml")

        if (!legacyConfigFile.exists()) {
            val legacyConfigStream = getResource("config-legacy.yml")
            val outputStream = FileOutputStream(currentConfigFile)
            val channel = outputStream.channel
            channel.transferFrom(Channels.newChannel(legacyConfigStream!!), 0, Long.MAX_VALUE)

            channel.close()
            outputStream.close()
        }
    }
}