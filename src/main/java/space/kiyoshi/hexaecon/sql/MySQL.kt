@file:Suppress("RedundantIf", "SpellCheckingInspection")

package space.kiyoshi.hexaecon.sql

import space.kiyoshi.hexaecon.utils.GetConfig
import space.kiyoshi.hexaecon.utils.NMSUtils
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class MySQL {
    private val nms = NMSUtils
    private val host = GetConfig.main().getString("MySQL.Host")!!
    private val port = GetConfig.main().getString("MySQL.Port")!!
    private val database = GetConfig.main().getString("MySQL.DataBase")!!
    private val username = GetConfig.main().getString("MySQL.UserName")!!
    private val password = GetConfig.main().getString("MySQL.Password")!!
    private var connection: Connection? = null
    val isConnected: Boolean
        get() = if (connection == null) false else true

    @Throws(ClassNotFoundException::class, SQLException::class)
    fun connect() {
        if (!isConnected) {
            connection = try {
                if(nms.checkServerVersion(nms.getCleanServerVersion())){
                    Class.forName("com.mysql.jdbc.Driver")
                } else {
                    Class.forName("com.mysql.cj.jdbc.Driver")
                }
                val unicode = "useSSL=false&autoReconnect=true&useUnicode=yes&characterEncoding=UTF-8&allowPublicKeyRetrieval=true"
                DriverManager.getConnection(
                    "jdbc:mysql://" +
                            host + ":" + port + "/" + database + "?" + unicode, username, password
                )
            } catch (e: Exception) {
                return e.printStackTrace()
                //return KiyoshiLogger.log(LogRecord(Level.SEVERE, "[MySQL] error while pulling requests from HexaEcon [1]"), "HexaEcon").also { plugin.server.shutdown() }
            }
        }
    }

    fun disconnect() {
        if (isConnected) {
            try {
                connection!!.close()
            } catch (e: SQLException) {
                return e.printStackTrace()
                //return KiyoshiLogger.log(LogRecord(Level.SEVERE, "[MySQL] error while pulling requests from HexaEcon [1]"), "HexaEcon").also { plugin.server.shutdown() }
            }
        }
    }

    fun getconnection(): Connection? {
        return connection
    }
}