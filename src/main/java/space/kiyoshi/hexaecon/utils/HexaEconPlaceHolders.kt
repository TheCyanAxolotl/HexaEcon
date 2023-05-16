@file:Suppress("LocalVariableName")

package space.kiyoshi.hexaecon.utils

import com.iridium.iridiumcolorapi.IridiumColorAPI
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player
import space.kiyoshi.hexaecon.HexaEcon
import space.kiyoshi.hexaecon.utils.Language.formattedAmount
import java.util.concurrent.CompletableFuture

class HexaEconPlaceHolders: PlaceholderExpansion() {
    private val symbol = GetConfig.main().getString("Economy.Virtual.Symbol")
    private val dataeconomyvalue = GetConfig.main().getString("MySQL.DataEconomyName")!!
    private val version = HexaEcon.plugin.description.version
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
                Format.hex(
                    Format.color(
                        IridiumColorAPI.process(
                            formattedAmount()?.replace(
                                "%amount",
                                getBalance(player)
                            )?.replace("%symbol", symbol.toString())!!
                        )
                    )
                )
            }
            else -> null
        }
    }

    private fun getBalance(player: Player): String {
        val future = CompletableFuture.supplyAsync {
            val stmt = HexaEcon.SQL!!.getconnection()!!.createStatement()
            val SQL = "SELECT * FROM " + player.name
            val rs = stmt.executeQuery(SQL)
            rs.next()
            val value = rs.getInt(dataeconomyvalue)
            rs.close()
            stmt.close()
            value
        }
        val value = future.get()
        return value.toString()
    }
}
