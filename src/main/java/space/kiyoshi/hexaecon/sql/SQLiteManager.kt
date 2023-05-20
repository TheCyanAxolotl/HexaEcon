@file:Suppress("RedundantIf", "SpellCheckingInspection", "unused")

package space.kiyoshi.hexaecon.sql

import space.kiyoshi.hexaecon.HexaEcon
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
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
            println("Connected to SQLite database.")
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

    fun executeQuery(query: String): ResultSet? {
        try {
            val statement = connection?.createStatement()
            return statement?.executeQuery(query)
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return null
    }

    fun getConnection(): Connection? {
        return connection
    }
}