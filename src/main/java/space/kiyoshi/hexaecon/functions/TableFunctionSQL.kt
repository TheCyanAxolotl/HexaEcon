/*
 * Copyright (c) 2023. KiyoshiDevelopment
 *   All rights reserved.
 */

@file:Suppress("SpellCheckingInspection")

package space.kiyoshi.hexaecon.functions

import org.bukkit.entity.Player
import space.kiyoshi.hexaecon.HexaEcon
import space.kiyoshi.hexaecon.utils.DataManager
import java.sql.SQLException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

object TableFunctionSQL {
    private val dataeconomyvalue = DataManager.main().getString("DataBase.DataEconomyName")!!

    /********************************* MySQL *********************************/

    fun createTable(name: Player) {
        val tableName = name.name
        val defaultValue = 0L
        val sqlCreate = "CREATE TABLE IF NOT EXISTS $tableName ($dataeconomyvalue BIGINT)"
        val stmt = HexaEcon.MySQLManager!!.getConnection()!!.createStatement()
        stmt.executeUpdate(sqlCreate)
        val sqlCheck = "SELECT COUNT(*) AS count FROM $tableName"
        val resultSet = stmt?.executeQuery(sqlCheck)
        resultSet?.next()
        val rowCount = resultSet?.getInt("count") ?: 0
        resultSet?.close()
        if (rowCount == 0) {
            val sqlInsert = "INSERT INTO $tableName ($dataeconomyvalue) VALUES ($defaultValue)"
            stmt?.execute(sqlInsert)
        }
        stmt.close()
    }

    fun createTableAmount(name: Player, value: Long) {
        val tableName = name.name
        val sqlCreate = "CREATE TABLE IF NOT EXISTS $tableName ($dataeconomyvalue BIGINT)"
        val stmt = HexaEcon.MySQLManager!!.getConnection()!!.createStatement()
        stmt.execute(sqlCreate)
        stmt.executeUpdate("INSERT INTO $tableName ($dataeconomyvalue) VALUES ('$value')")
        stmt.close()
    }

    fun dropTable(name: Player) {
        val sqlDrop = "DROP TABLE " + name.name
        val stmt = HexaEcon.MySQLManager!!.getConnection()!!.createStatement()
        stmt.executeUpdate(sqlDrop)
        stmt.close()
    }

    fun getValueFromSQL(player: Player, dataeconomyvalue: String): Long {
        val future = CompletableFuture.supplyAsync {
            val stmt = HexaEcon.MySQLManager!!.getConnection()!!.createStatement()
            val SQL = "SELECT * FROM " + player.name
            val rs = stmt.executeQuery(SQL)
            rs.next()
            val value = rs.getLong(dataeconomyvalue)
            rs.close()
            stmt.close()
            value
        }
        return try {
            future.get()
        } catch (e: ExecutionException) {
            e.printStackTrace()
            0L
        }
    }

    /********************************* SQLite *********************************/


    fun createTableSQLite(name: Player) {
        val tableName = name.name
        val defaultValue = 0L
        val sqlCreate = """CREATE TABLE IF NOT EXISTS $tableName ($dataeconomyvalue BIGINT)"""
        val stmt = HexaEcon.SQLiteManager!!.getConnection()!!.createStatement()
        stmt.executeUpdate(sqlCreate)
        val sqlCheck = "SELECT COUNT(*) AS count FROM $tableName"
        val resultSet = stmt.executeQuery(sqlCheck)
        resultSet.next()
        val rowCount = resultSet.getInt("count")
        resultSet.close()
        if (rowCount == 0) {
            val sqlInsert = "INSERT INTO $tableName ($dataeconomyvalue) VALUES ($defaultValue)"
            stmt.execute(sqlInsert)
        }
        stmt.close()
    }

    fun createTableAmountSQLite(name: Player, value: Long) {
        val tableName = name.name
        val sqlCreate = """CREATE TABLE IF NOT EXISTS $tableName ($dataeconomyvalue BIGINT)"""
        val stmt = HexaEcon.SQLiteManager!!.getConnection()!!.createStatement()
        stmt.executeUpdate(sqlCreate)
        val columnName = dataeconomyvalue
        val sqlInsert = "INSERT INTO $tableName ($columnName) VALUES (?)"
        val preparedStatement = HexaEcon.SQLiteManager!!.getConnection()!!.prepareStatement(sqlInsert)
        preparedStatement.setLong(1, value)
        preparedStatement.executeUpdate()
        stmt.close()
    }

    fun dropTableSQLite(name: Player) {
        val tableName = name.name
        val sqlDrop = """DROP TABLE IF EXISTS $tableName"""
        val stmt = HexaEcon.SQLiteManager!!.getConnection()!!.createStatement()
        stmt.executeUpdate(sqlDrop)
        stmt.close()
    }


    @JvmStatic
    fun selectAllFromTableAsStringSQLite(tableName: String): List<String> {
        val results = mutableListOf<String>()
        try {
            val connection = HexaEcon.SQLiteManager?.getConnection()
            val statement = connection?.createStatement()
            val sql = "SELECT * FROM $tableName"
            val resultSet = statement?.executeQuery(sql)
            val metaData = resultSet?.metaData
            val columns = metaData?.columnCount ?: 0
            while (resultSet?.next() == true) {
                val rowValues = mutableListOf<String>()
                for (i in 1..columns) {
                    val columnName = metaData?.getColumnName(i)
                    val columnValue = resultSet.getString(columnName)
                    rowValues.add(columnValue)
                }
                results.add(rowValues.joinToString(", "))
            }
            resultSet?.close()
            statement?.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return results
    }
}