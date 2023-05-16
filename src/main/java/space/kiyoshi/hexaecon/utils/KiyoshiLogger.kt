package space.kiyoshi.hexaecon.utils

import org.bukkit.Bukkit
import java.util.logging.*


object KiyoshiLogger {
    private fun colorize(level: Level, message: String): String {
        val color: String = when (level) {
            Level.SEVERE -> "\u001B[31m"
            Level.WARNING -> "\u001B[33m"
            Level.INFO -> "\u001B[34m"
            Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST -> "\u001B[32m"
            else -> "\u001B[0m"
        }
        return "$color$message\u001B[0m"
    }

    fun log(record: LogRecord?, name: String) {
        if (record != null && Logger.getLogger(name).isLoggable(record.level)) {
            val message: String = colorize(record.level, record.message)
            Bukkit.getConsoleSender().sendMessage(message)
        }
    }

}