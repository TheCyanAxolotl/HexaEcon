/*
 * Copyright (c) 2023. KiyoshiDevelopment
 *   All rights reserved.
 */

@file:Suppress("DEPRECATION", "SpellCheckingInspection")

package space.kiyoshi.hexaecon.events

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import space.kiyoshi.hexaecon.functions.TableFunctionMongo
import space.kiyoshi.hexaecon.functions.TableFunctionRedis
import space.kiyoshi.hexaecon.functions.TableFunctionSQL
import space.kiyoshi.hexaecon.utils.GetConfig
import java.sql.SQLException

class JoinEvent : Listener {
    private val hastextureenabled = GetConfig.main().getBoolean("TextureManager.Enabled")
    private val texture = GetConfig.main().getString("TextureManager.Texture")!!
    private val databasetype = GetConfig.main().getString("DataBase.Type")!!

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        try {
            when (databasetype) {
                "h2" -> {
                    TableFunctionSQL.createTableSQLite(event.player)
                }
                "MongoDB" -> {
                    TableFunctionMongo.createCollection(event.player.name)
                }
                "MySQL" -> {
                    TableFunctionSQL.createTable(event.player)
                }
                "Redis" -> {
                    TableFunctionRedis.createTable(event.player.name)
                }
            }
        } catch (_: SQLException) {}
        GetConfig.generatePlayerConfig(event.player)
        if (hastextureenabled) {
            event.player.setTexturePack(texture)
        }
    }
}