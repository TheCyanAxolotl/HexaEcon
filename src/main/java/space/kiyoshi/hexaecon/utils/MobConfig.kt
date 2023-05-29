/*
 * Copyright (c) 2023. KiyoshiDevelopment
 *   All rights reserved.
 */

package space.kiyoshi.hexaecon.utils

import org.yaml.snakeyaml.Yaml
import space.kiyoshi.hexaecon.HexaEcon.Companion.plugin
import java.io.File
import java.io.FileReader
import java.io.IOException

data class MobConfig(
    val mobType: String = "",
    val enabled: Boolean = false,
    val earn: Int = 0
) {
    companion object {
        private val mobsConfig = getCustomMobsConfig()

        private fun getCustomMobsConfig(): Map<String, MobConfig> {
            val configFile = File("${plugin.dataFolder}/modules/custommobs.yml")
            val yaml = Yaml()
            val mobsConfig = mutableMapOf<String, MobConfig>()

            if (configFile.exists()) {
                try {
                    val fileReader = FileReader(configFile)
                    val yamlData = yaml.load<Map<String, Any>>(fileReader)

                    yamlData?.get("Mobs")?.let { mobsData ->
                        if (mobsData is Map<*, *>) {
                            for ((mobName, mobData) in mobsData) {
                                if (mobData is Map<*, *>) {
                                    val enabled = mobData["enabled"] as? Boolean
                                    val earn = mobData["earn"] as? Int

                                    if (mobName is String && enabled != null && earn != null) {
                                        val mobType = mobData["mobType"] as? String
                                        if (mobType != null) {
                                            mobsConfig[mobType] = MobConfig(mobType, enabled, earn)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    fileReader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            return mobsConfig
        }
    }

    fun getMobEarn(mobType: String): Int {
        return mobsConfig[mobType]?.earn ?: 0
    }

    fun getMobType(mobType: String): String {
        return mobsConfig[mobType]?.mobType ?: "Unknown MobType"
    }

    fun getMobEnabled(mobType: String): Boolean {
        return mobsConfig[mobType]?.enabled ?: false
    }
}
