@file:Suppress("SpellCheckingInspection", "SameParameterValue")

package space.kiyoshi.hexaecon

import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import space.kiyoshi.hexaecon.commands.EcoCommand
import space.kiyoshi.hexaecon.commands.PayCommand
import space.kiyoshi.hexaecon.events.AnimalsDeath
import space.kiyoshi.hexaecon.events.EventsListener
import space.kiyoshi.hexaecon.events.JoinEvent
import space.kiyoshi.hexaecon.events.MonsterDeath
import space.kiyoshi.hexaecon.sql.MySQLManager
import space.kiyoshi.hexaecon.sql.SQLiteManager
import space.kiyoshi.hexaecon.utils.HexaEconPlaceHolders
import space.kiyoshi.hexaecon.utils.KiyoshiLogger
import space.kiyoshi.hexaecon.utils.NMSUtils
import space.kiyoshi.hexaecon.commands.WalletCommand
import java.io.File
import java.io.IOException
import java.util.logging.Level
import java.util.logging.LogRecord
import kotlin.properties.Delegates

class HexaEcon : JavaPlugin() {
    private val nms = NMSUtils
    private var lang = File(dataFolder, "language")
    private var languagefile = File("$dataFolder/language/language.yml")
    private var languageconfig: FileConfiguration = YamlConfiguration.loadConfiguration(languagefile)
    private fun initialize() {
        saveDefaultConfig()
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
                languageconfig["Formatted.Amount"] = "&e%amount&6%symbol"
                languageconfig.createSection("Language")
                languageconfig["Language.Prefix"] = "&e[&9HexaEcon&e]&r "
                languageconfig["Language.IsConsolePlayer"] = "&esorry but you cannot execute this command in console."
                languageconfig["Language.BankAmount"] = "&eyou have &6%amount &e%valuename."
                languageconfig["Language.GenericEarn"] = "&e+ &6%amount &e%valuename."
                languageconfig["Language.ConfigurationReloaded"] = "&econfiguration files successfully reloaded."
                languageconfig["Language.GenerateToOther"] = "&e+ &6%amount &e%valuename generated by &6%p"
                languageconfig["Language.AccessDenied"] =
                    "&eyou don't have access to this command permission required: &6%perm"
                languageconfig["Language.InvalidAmount"] =
                    "&erequired minimum &61 &e%valuename &eor check if is a valid number."
                languageconfig["Language.WalletWithdrawAmount"] = "&e- &6%amount &e%valuename."
                languageconfig["Language.PlayerNotFound"] = "&eplayer &6%p &enot found or not online."
                languageconfig["Language.WalletWithdrawConverted"] = "&6%amount &e%valuename converted."
                languageconfig["Language.WalletWithdrawRemaningAmount"] = "&ethey still remain %valuename &6%amount."
                languageconfig["Language.WalletWithdrawNoEnoughAmount"] =
                    "&eyou don't have enough %valuename, you have &6%amount &e%valuename."
                languageconfig["Language.PlayerPayed"] = "&eYou have successfully sent to &6%p &6%amount &e%valuename."
                languageconfig["Language.PlayerPaymentRecived"] =
                    "&eYou recived a payment of &6%amount &e%valuename &eby &6%p."
                languageconfig["Language.CannotPaySelf"] = "&eYou cannot pay yourself."
                languageconfig["Language.UsageFormat"] = "&eUsage: %u"
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
        } else if (databasetype == "MySQL") {
            val worlds = Bukkit.getWorlds()
            for (world in worlds) {
                world.save()
            }
            MySQLManager = MySQLManager()
            MySQLManager?.connect()
            if (MySQLManager?.isConnected == true) {
                KiyoshiLogger.log(
                    LogRecord(Level.INFO, "[MySQL] pulling mysql requests from HexaEcon [OK]"),
                    "HexaEcon"
                )
            }
        }
    }

    private fun events() {
        server.pluginManager.registerEvents(MonsterDeath(), this)
        server.pluginManager.registerEvents(AnimalsDeath(), this)
        server.pluginManager.registerEvents(JoinEvent(), this)
        server.pluginManager.registerEvents(EventsListener(), this)
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
        if (databasetype == "h2") {
            try {
                SQLiteManager!!.disconnect()
            } catch (_: ClassNotFoundException) {
            }
        } else if (databasetype == "MySQL") {
            try {
                MySQLManager!!.disconnect()
            } catch (_: ClassNotFoundException) {
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

}