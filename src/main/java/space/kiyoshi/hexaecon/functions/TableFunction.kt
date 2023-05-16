@file:Suppress("SpellCheckingInspection")

package space.kiyoshi.hexaecon.functions

import org.bukkit.entity.Player
import space.kiyoshi.hexaecon.HexaEcon
import space.kiyoshi.hexaecon.utils.GetConfig
import java.sql.SQLException

object TableFunction {
    private val dataeconomyvalue = GetConfig.main().getString("MySQL.DataEconomyName")!!
    @Throws(SQLException::class)
    fun createTable(name: Player ) {
        val sqlCreate = ("CREATE TABLE IF NOT EXISTS " + name.name
                + "($dataeconomyvalue INT(255))")
        val stmt = HexaEcon.SQL!!.getconnection()!!.createStatement()
        stmt.execute("start transaction")
        stmt.execute(sqlCreate)
        stmt.executeUpdate("INSERT INTO " + name.name + "($dataeconomyvalue) VALUES('" + 0 + "')")
        stmt.execute("commit")
        stmt.close()
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun createTableAmount(name: Player, value: Int) {
        val sqlCreate = ("CREATE TABLE IF NOT EXISTS " + name.name
                + "($dataeconomyvalue INT(255))")
        val stmt = HexaEcon.SQL!!.getconnection()!!.createStatement()
        stmt.execute("start transaction")
        stmt.execute(sqlCreate)
        stmt.executeUpdate("INSERT INTO " + name.name + "($dataeconomyvalue) VALUES('" + value + "')")
        stmt.execute("commit")
        stmt.close()
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun dropTable(name: Player) {
        val sqlDrop = "DROP TABLE " + name.name
        val stmt = HexaEcon.SQL!!.getconnection()!!.createStatement()
        stmt.execute("start transaction")
        stmt.executeUpdate(sqlDrop)
        stmt.execute("commit")
        stmt.close()
    }
}