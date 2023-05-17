@file:Suppress("DEPRECATION", "SpellCheckingInspection")

package space.kiyoshi.hexaecon.events

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import space.kiyoshi.hexaecon.functions.TableFunction
import space.kiyoshi.hexaecon.utils.GetConfig
import space.kiyoshi.hexaecon.utils.KiyoshiLogger
import java.sql.SQLException
import java.util.logging.Level
import java.util.logging.LogRecord

class JoinEvent : Listener {
    private val hastextureenabled = GetConfig.main().getBoolean("TextureManager.Enabled")
    private val texture = GetConfig.main().getString("TextureManager.Texture")!!
    private val databasetype = GetConfig.main().getString("DataBase.Type")!!
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        try {
            if(databasetype == "h2") {
                TableFunction.createTableSQLite(event.player)
            } else {
                TableFunction.createTable(event.player)
            }
        } catch (_: SQLException) {}
        GetConfig.generatePlayerConfig(event.player)
        if(hastextureenabled){
            event.player.setTexturePack(texture)
        }
    }
}