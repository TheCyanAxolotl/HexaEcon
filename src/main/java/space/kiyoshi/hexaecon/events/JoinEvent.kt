@file:Suppress("DEPRECATION", "SpellCheckingInspection")

package space.kiyoshi.hexaecon.events

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import space.kiyoshi.hexaecon.functions.TableFunction
import space.kiyoshi.hexaecon.utils.GetConfig
import java.sql.SQLException

class JoinEvent : Listener {
    private val hastextureenabled = GetConfig.main().getBoolean("TextureManager.Enabled")
    private val texture = GetConfig.main().getString("TextureManager.Texture")!!
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        try {
            TableFunction.createTable(event.player)
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        GetConfig.generatePlayerConfig(event.player)
        if(hastextureenabled){
            event.player.setTexturePack(texture)
        }
    }
}