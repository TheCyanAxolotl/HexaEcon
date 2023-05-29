/*
 * Copyright (c) 2023. KiyoshiDevelopment
 *   All rights reserved.
 */

@file:Suppress("RedundantIf", "SpellCheckingInspection")

package space.kiyoshi.hexaecon.sql

import space.kiyoshi.hexaecon.utils.DataManager
import space.kiyoshi.hexaecon.utils.KiyoshiLogger
import space.kiyoshi.hexaecon.utils.NMSUtils
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.logging.Level
import java.util.logging.LogRecord

class MySQLManager {
    private val nms = NMSUtils
    private val host = DataManager.main().getString("MySQL.Host")!!
    private val port = DataManager.main().getInt("MySQL.Port")
    private val database = DataManager.main().getString("MySQL.DataBase")!!
    private val username = DataManager.main().getString("MySQL.UserName")!!
    private val password = DataManager.main().getString("MySQL.Password")!!
    private var connection: Connection? = null
    val isConnected: Boolean
        get() = if (connection == null) false else true

    @Throws(ClassNotFoundException::class, SQLException::class)
    fun connect() {
        if (!isConnected) {
            connection = try {
                if (nms.checkServerVersionUp(nms.getCleanServerVersion())) {
                    Class.forName("com.mysql.jdbc.Driver")
                } else {
                    Class.forName("com.mysql.cj.jdbc.Driver")
                }
                val unicode = "useSSL=false&autoReconnect=true&useUnicode=yes&characterEncoding=UTF-8&allowPublicKeyRetrieval=true"
                DriverManager.getConnection("jdbc:mysql://$host:$port/$database?$unicode", username, password)
            } catch (e: Exception) {
                return e.printStackTrace().also { KiyoshiLogger.log(LogRecord(Level.SEVERE, "[MySQL] error while pulling requests from HexaEcon [1]"), "HexaEcon") }
            }
        }
    }

    fun disconnect() {
        if (isConnected) {
            try {
                connection!!.close()
            } catch (e: SQLException) {
                return e.printStackTrace().also { KiyoshiLogger.log(LogRecord(Level.SEVERE, "[MySQL] error while pulling requests from HexaEcon [1]"), "HexaEcon") }
            }
        }
    }

    fun getConnection(): Connection? {
        return connection
    }
}