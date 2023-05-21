package space.kiyoshi.hexaecon.api

import org.bukkit.entity.Player
import space.kiyoshi.hexaecon.functions.TableFunctionMongo
import space.kiyoshi.hexaecon.functions.TableFunctionRedis
import space.kiyoshi.hexaecon.functions.TableFunctionSQL
import space.kiyoshi.hexaecon.utils.GetConfig

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

}