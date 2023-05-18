package space.kiyoshi.hexaecon.utils

import org.bukkit.Bukkit
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Field

object NMSUtils {
    @JvmStatic
    fun setMaxStackSize(item: ItemStack, i: Int) {
        try {
            val field: Field = Item::class.java.getDeclaredField("maxStackSize")
            field.isAccessible = true
            field.setInt(item, i)
        } catch (_: Exception) {
        }
    }

    fun checkServerVersion(version: String): Boolean {
        val targetVersion = "1.16"
        val versionPattern = """^(\d+)\.(\d+)(?:\.\d+)?$""".toRegex()

        val matchResult = versionPattern.matchEntire(version)

        if (matchResult != null) {
            val major = matchResult.groupValues[1].toInt()
            val minor = matchResult.groupValues[2].toInt()

            val targetMajor = targetVersion.substringBefore('.').toInt()
            val targetMinor = targetVersion.substringAfter('.').toInt()

            if (major < targetMajor) {
                return true
            } else if (major == targetMajor && minor <= targetMinor) {
                return true
            }
        }

        return false
    }

    fun getCleanServerVersion(): String {
        val versionString = Bukkit.getServer().version
        val regexPattern = """\d+\.\d+\.\d+""".toRegex()
        val matchResult = regexPattern.find(versionString)

        return matchResult?.value ?: "Unknown"
    }


}