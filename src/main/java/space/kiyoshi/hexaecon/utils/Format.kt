@file:Suppress("unused", "SpellCheckingInspection")

package space.kiyoshi.hexaecon.utils

import org.bukkit.ChatColor
import java.awt.Color
import java.util.regex.Pattern

object Format {
    fun color(s: String?): String {
        return ChatColor.translateAlternateColorCodes('&', s!!)
    }

    fun rgb(red: Int?, green: Int?, blue: Int?, s: String?): String {
        return net.md_5.bungee.api.ChatColor.of(Color(red!!, green!!, blue!!)).toString() + color(s)
    }

    fun hex(message: String): String {
        @Suppress("NAME_SHADOWING") var message = message
        val pattern = Pattern.compile("#[a-fA-F0-9]{6}")
        var matcher = pattern.matcher(message)
        while (matcher.find()) {
            val hexCode = message.substring(matcher.start(), matcher.end())
            val replaceSharp = hexCode.replace('#', 'x')
            val ch = replaceSharp.toCharArray()
            val builder = StringBuilder("")
            for (c in ch) {
                builder.append("&$c")
            }
            message = message.replace(hexCode, builder.toString())
            matcher = pattern.matcher(message)
        }
        return ChatColor.translateAlternateColorCodes('&', message)
    }

    fun hasLetterSpecialAndMaybeDigit(string: String?): Boolean {
        return try {
            val letter = Pattern.compile("[a-zA-z]")
            val digit = Pattern.compile("[0-9]")
            val special = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]")
            val hasLetter = letter.matcher(string!!)
            val hasDigit = digit.matcher(string)
            val hasSpecial = special.matcher(string)
            hasLetter.find() || hasSpecial.find() && hasDigit.find()
        } catch (ignored: NumberFormatException) {
            false
        }
    }

    fun hasLetterAndMabyeDigit(string: String?): Boolean {
        return try {
            val letter = Pattern.compile("[a-zA-z]")
            val digit = Pattern.compile("[0-9]")
            val hasLetter = letter.matcher(string!!)
            val hasDigit = digit.matcher(string)
            hasLetter.find() && hasDigit.find()
        } catch (ignored: NumberFormatException) {
            false
        }
    }

    fun hasLetterAndSpecial(string: String?): Boolean {
        return try {
            val letter = Pattern.compile("[a-zA-z]")
            val special = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]")
            val hasLetter = letter.matcher(string!!)
            val hasSpecial = special.matcher(string)
            hasLetter.find() && hasSpecial.find()
        } catch (ignored: NumberFormatException) {
            false
        }
    }

    fun hasLetter(string: String?): Boolean {
        return try {
            val letter = Pattern.compile("[a-zA-z]")
            val hasLetter = letter.matcher(string!!)
            hasLetter.find()
        } catch (ignored: NumberFormatException) {
            false
        }
    }

    fun hasSpecial(string: String?): Boolean {
        return try {
            val special = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]")
            val hasSpecial = special.matcher(string!!)
            hasSpecial.find()
        } catch (ignored: NumberFormatException) {
            false
        }
    }
}