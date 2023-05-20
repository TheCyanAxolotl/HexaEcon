@file:Suppress("unused", "KotlinConstantConditions", "LocalVariableName")

package space.kiyoshi.hexaecon.api

import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import space.kiyoshi.hexaecon.HexaEcon
import space.kiyoshi.hexaecon.HexaEcon.Companion.plugin
import space.kiyoshi.hexaecon.functions.TableFunctionMongo
import space.kiyoshi.hexaecon.functions.TableFunctionRedis
import space.kiyoshi.hexaecon.functions.TableFunctionSQL
import space.kiyoshi.hexaecon.utils.Economy
import space.kiyoshi.hexaecon.utils.GetConfig
import space.kiyoshi.hexaecon.utils.NMSUtils
import java.io.File
import java.io.IOException
import java.sql.SQLException
import java.util.concurrent.CompletableFuture

object HexaEconAPI {

    @JvmStatic
    fun createBankAccount(player: Player, value: Int) {
        val databasetype = GetConfig.main().getString("DataBase.Type")!!
        GetConfig.generatePlayerConfig(player)
        when (databasetype) {
            "h2" -> {
                TableFunctionSQL.createTableAmountSQLite(player, value)
            }
            "MongoDB" -> {
                TableFunctionMongo.createCollectionAmount(player.name, value)
            }
            "MySQL" -> {
                TableFunctionSQL.createTableAmount(player, value)
            }
            "Redis" -> {
                TableFunctionRedis.createTableAmount(player.name, value)
            }
        }
    }

    @JvmStatic
    fun deleteBankAccount(player: Player) {
        val databasetype = GetConfig.main().getString("DataBase.Type")!!
        GetConfig.deletePlayerConfig(player)
        when (databasetype) {
            "h2" -> {
                TableFunctionSQL.dropTable(player)
            }
            "MongoDB" -> {
                TableFunctionMongo.dropCollection(player.name)
            }
            "MySQL" -> {
                TableFunctionSQL.dropTable(player)
            }
            "Redis" -> {
                TableFunctionRedis.dropTable(player.name)
            }
        }
    }

    @JvmStatic
    fun hasBankAccount(player: Player): Boolean {
        val bankaccount = File(plugin.dataFolder, "data/${player.name}")
        return bankaccount.exists()
    }
    
    @JvmStatic
    fun getPlayerBalance(player: Player): Int {
        val dataeconomyvalue = GetConfig.main().getString("DataBase.DataEconomyName")!!
        return when (GetConfig.main().getString("DataBase.Type")!!) {
            "h2" -> {
                TableFunctionSQL.selectAllFromTableAsStringSQLite(player.name).toString().replace("[", "").replace("]", "").toInt()
            }
            "MongoDB" -> {
                TableFunctionMongo.selectAllFromCollectionAsString(player.name).toString().replace("[", "").replace("]", "").toInt()
            }
            "MySQL" -> {
                val future = CompletableFuture.supplyAsync {
                    val stmt = HexaEcon.MySQLManager!!.getConnection()!!.createStatement()
                    val SQL = "SELECT * FROM " + player.name
                    val rs = stmt.executeQuery(SQL)
                    rs.next()
                    val value = rs.getInt(dataeconomyvalue)
                    rs.close()
                    stmt.close()
                    value
                }
                val value = future.get()
                value.toInt()
            }
            "Redis" -> {
                TableFunctionRedis.selectAllFromCollectionAsStringRedis(player.name).toString().replace("[", "").replace("]", "").toInt()
            }
            else -> {
                return 0
            }
        }
    }
    
    @JvmStatic
    fun addPlayerBalance(player: Player, value: Int) {
        val databasetype = GetConfig.main().getString("DataBase.Type")!!
        val dataeconomyvalue = GetConfig.main().getString("DataBase.DataEconomyName")!!
        val data_names2_sqlite =
            File(plugin.dataFolder.toString() + "/data/${player.name}/" + player.name + "_SQLite.txt")
        val data_names2_mysql =
            File(plugin.dataFolder.toString() + "/data/${player.name}/" + player.name + "_MySQL.txt")
        val data_names2_mongodb =
            File(plugin.dataFolder.toString() + "/data/${player.name}/" + player.name + "_MongoDB.txt")
        val data_names2_redis =
            File(plugin.dataFolder.toString() + "/data/${player.name}/" + player.name + "_Redis.txt")
        val data_names_config2_sqlite: FileConfiguration =
            YamlConfiguration.loadConfiguration(data_names2_sqlite)
        val data_names_config2_mysql: FileConfiguration =
            YamlConfiguration.loadConfiguration(data_names2_mysql)
        val data_names_config2_mongodb: FileConfiguration =
            YamlConfiguration.loadConfiguration(data_names2_mongodb)
        val data_names_config2_redis: FileConfiguration =
            YamlConfiguration.loadConfiguration(data_names2_redis)
        val data_names_sqlite = File(
            plugin.dataFolder.toString() + "/data/${player.name}/" + player
                .name + "_SQLite.txt"
        )
        val data_names_mysql = File(
            plugin.dataFolder.toString() + "/data/${player.name}/" + player
                .name + "_MySQL.txt"
        )
        val data_names_mongodb = File(
            plugin.dataFolder.toString() + "/data/${player.name}/" + player
                .name + "_MongoDB.txt"
        )
        val data_names_redis = File(
            plugin.dataFolder.toString() + "/data/${player.name}/" + player
                .name + "_Redis.txt"
        )
        val somasqlite =
            data_names_config2_sqlite.getInt("data.${dataeconomyvalue}") + value
        val somamysql =
            data_names_config2_mysql.getInt("data.${dataeconomyvalue}") + value
        val somamongodb =
            data_names_config2_mongodb.getInt("data.${dataeconomyvalue}") + value
        val somaredis =
            data_names_config2_redis.getInt("data.${dataeconomyvalue}") + value
        try {
            when (databasetype) {
                "h2" -> {
                    TableFunctionSQL.dropTableSQLite(player)
                }
                "MongoDB" -> {
                    TableFunctionMongo.dropCollection(player.name)
                }
                "MySQL" -> {
                    TableFunctionSQL.dropTable(player)
                }
                "Redis" -> {
                    TableFunctionRedis.dropTable(player.name)
                }
            }
        } catch (_: SQLException) {}
        try {
            when (databasetype) {
                "h2" -> {
                    TableFunctionSQL.createTableAmountSQLite(player, somasqlite)
                }
                "MongoDB" -> {
                    TableFunctionMongo.createCollectionAmount(player.name, somamongodb)
                }
                "MySQL" -> {
                    TableFunctionSQL.createTableAmount(player, somamysql)
                }
                "Redis" -> {
                    TableFunctionRedis.createTableAmount(player.name, somaredis)
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
    }
    
    @JvmStatic
    fun removePlayerBalance(player: Player, value: Int) {
        val databasetype = GetConfig.main().getString("DataBase.Type")!!
        val dataeconomyvalue = GetConfig.main().getString("DataBase.DataEconomyName")!!
        val data_names_sqlite =
            File(plugin.dataFolder.toString() + "/data/${player.name}/" + player.name + "_SQLite.txt")
        val data_names_mysql =
            File(plugin.dataFolder.toString() + "/data/${player.name}/" + player.name + "_MySQL.txt")
        val data_names_mongodb =
            File(plugin.dataFolder.toString() + "/data/${player.name}/" + player.name + "_MongoDB.txt")
        val data_names_redis =
            File(plugin.dataFolder.toString() + "/data/${player.name}/" + player.name + "_Redis.txt")
        val data_names_config_sqlite: FileConfiguration =
            YamlConfiguration.loadConfiguration(data_names_sqlite)
        val data_names_config_mysql: FileConfiguration =
            YamlConfiguration.loadConfiguration(data_names_mysql)
        val data_names_config_mongodb: FileConfiguration =
            YamlConfiguration.loadConfiguration(data_names_mongodb)
        val data_names_config_redis: FileConfiguration =
            YamlConfiguration.loadConfiguration(data_names_redis)
        val somasqlite =
            data_names_config_sqlite.getInt("data.${dataeconomyvalue}") - value
        val somamysql =
            data_names_config_mysql.getInt("data.${dataeconomyvalue}") - value
        val somamongodb =
            data_names_config_mongodb.getInt("data.${dataeconomyvalue}") - value
        val somaredis =
            data_names_config_redis.getInt("data.${dataeconomyvalue}") - value

        if (databasetype == "h2") {
            if (data_names_config_sqlite.getInt("data.${dataeconomyvalue}") >= value) {
                try {
                    when (databasetype) {
                        "h2" -> {
                            TableFunctionSQL.dropTableSQLite(player)
                        }

                        "MongoDB" -> {
                            TableFunctionMongo.dropCollection(player.name)
                        }

                        "MySQL" -> {
                            TableFunctionSQL.dropTable(player)
                        }

                        "Redis" -> {
                            TableFunctionRedis.dropTable(player.name)
                        }
                    }
                } catch (_: SQLException) {}
                try {
                    when (databasetype) {
                        "h2" -> {
                            TableFunctionSQL.createTableAmountSQLite(player, somasqlite)
                        }

                        "MongoDB" -> {
                            TableFunctionMongo.createCollectionAmount(player.name, somamongodb)
                        }

                        "MySQL" -> {
                            TableFunctionSQL.createTableAmount(player, somamysql)
                        }

                        "Redis" -> {
                            TableFunctionRedis.createTableAmount(player.name, somaredis)
                        }
                    }
                } catch (_: SQLException) {}
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
                } catch (_: IOException) {}
            }
        }
    }

    @JvmStatic
    fun canUseTexturePack(): Boolean {
        val nms = NMSUtils
        return nms.checkServerVersionUp(nms.getCleanServerVersion())
    }

    @JvmStatic
    fun giveEconomyCoin(player: Player, value: Int) {
        Economy.addEconomy(player, value)
    }

    @JvmStatic
    fun giveEconomyCoinNative(player: Player) {
        Economy.addNativeEconomy(player)
    }

    @JvmStatic
    fun checkServerVersionUp(version: String): Boolean {
        val targetVersion = "1.16"
        val versionPattern = """^(\d+)\.(\d+)(?:\.\d+)?$""".toRegex()

        val matchResult = versionPattern.matchEntire(version)

        if (matchResult != null) {
            val major = matchResult.groupValues[1].toInt()
            val minor = matchResult.groupValues[2].toInt()

            val targetMajor = targetVersion.substringBefore('.').toInt()
            val targetMinor = targetVersion.substringAfter('.').toInt()

            if (major < targetMajor) {
                return true
            } else if (major == targetMajor && minor <= targetMinor) {
                return true
            }
        }

        return false
    }

    @JvmStatic
    fun checkLegacyVersion(version: String): Boolean {
        return version.contains("1.8")
    }

    @JvmStatic
    fun getCleanServerVersion(): String {
        val versionString = Bukkit.getServer().version
        val regexPattern = """\d+\.\d+\.\d+""".toRegex()
        val matchResult = regexPattern.find(versionString)

        return matchResult?.value ?: "Unknown"
    }

    @JvmStatic
    fun config(): FileConfiguration {
        return plugin.config
    }

}