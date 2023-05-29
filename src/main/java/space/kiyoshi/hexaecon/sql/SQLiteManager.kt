/*
 * Copyright (c) 2023. KiyoshiDevelopment
 *   All rights reserved.
 */

@file:Suppress("RedundantIf", "SpellCheckingInspection")

package space.kiyoshi.hexaecon.sql

import space.kiyoshi.hexaecon.HexaEcon
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class SQLiteManager(private val databasePath: String) {
    private var connection: Connection? = null

    val isConnected: Boolean
        get() = if (connection == null) false else true

    @Throws(ClassNotFoundException::class, SQLException::class)
    fun connect() {
        try {
            Class.forName("org.sqlite.JDBC")
            connection = DriverManager.getConnection("jdbc:sqlite:$databasePath")
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            val connection = HexaEcon.SQLiteManager?.getConnection()
            connection?.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun getConnection(): Connection? {
        return connection
    }
}