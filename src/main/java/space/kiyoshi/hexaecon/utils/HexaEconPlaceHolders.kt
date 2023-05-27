/*
 * Copyright (c) 2023. KiyoshiDevelopment
 *   All rights reserved.
 */

@file:Suppress("LocalVariableName", "SpellCheckingInspection")

package space.kiyoshi.hexaecon.utils

import com.iridium.iridiumcolorapi.IridiumColorAPI
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player
import space.kiyoshi.hexaecon.HexaEcon
import space.kiyoshi.hexaecon.functions.TableFunctionMongo
import space.kiyoshi.hexaecon.functions.TableFunctionRedis
import space.kiyoshi.hexaecon.functions.TableFunctionSQL
import space.kiyoshi.hexaecon.utils.Language.formattedAmount
import java.util.concurrent.CompletableFuture

class HexaEconPlaceHolders : PlaceholderExpansion() {
    private val symbol = GetConfig.main().getString("Economy.Virtual.Symbol")
    private val dataeconomyvalue = GetConfig.main().getString("DataBase.DataEconomyName")!!
    private val version = HexaEcon.plugin.description.version
    private val databasetype = GetConfig.main().getString("DataBase.Type")!!
    override fun getIdentifier(): String {
        return "hexaecon"
    }

    override fun getAuthor(): String {
        return "TheUwUAxolotl"
    }

    override fun getVersion(): String {
        return "1.0.0"
    }

    override fun onPlaceholderRequest(player: Player, identifier: String): String? {
        return when (identifier) {
            "version" -> {
                version
            }

            "author" -> {
                "TheUwUAxolotl"
            }

            "balance" -> {
                getBalance(player)
            }

            "balance_formatted" -> {
                Format.hex(Format.color(IridiumColorAPI.process(formattedAmount()?.replace("%amountformatted%", Economy.formatBalance(getBalance(player)))!!)))
            }

            "balance_formatted_symbol" -> {
                Format.hex(Format.color(IridiumColorAPI.process(formattedAmount()?.replace("%amountformatted%", Economy.formatBalance(getBalance(player))+symbol.toString())!!)))
            }

            else -> null
        }
    }

    private fun getBalance(player: Player): String {
        return when (databasetype) {
            "h2" -> {
                TableFunctionSQL.selectAllFromTableAsStringSQLite(player.name).toString().replace("[", "").replace("]", "")
            }
            "MongoDB" -> {
                TableFunctionMongo.selectAllFromCollectionAsString(player.name).toString().replace("[", "").replace("]", "")
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
                    value.toString()
                }
                val value = future.get()
                value.toString()
            }
            "Redis" -> {
                TableFunctionRedis.selectAllFromCollectionAsStringRedis(player.name).toString().replace("[", "").replace("]", "")
            }
            else -> {
                return "Unknown DataBase (*Type)"
            }
        }
    }
}
