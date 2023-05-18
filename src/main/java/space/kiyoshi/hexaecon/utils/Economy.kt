@file:Suppress("UNUSED_PARAMETER", "unused", "LocalVariableName", "SpellCheckingInspection", "DEPRECATION")

package space.kiyoshi.hexaecon.utils

import com.iridium.iridiumcolorapi.IridiumColorAPI
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack


object Economy {
    private val itemtype = GetConfig.main().getString("Economy.Physical.Item")!!
    private val itemdisplayname = GetConfig.main().getString("Economy.Physical.DisplayName")!!
    private val itemnbtvalue = GetConfig.main().getInt("Economy.Physical.NBT.Value")
    private val itemlore: List<String> = GetConfig.main().getStringList("Economy.Physical.Lore")
    private val ignorelevelrestriction = GetConfig.main().getBoolean("Economy.Physical.Enchant.IgnoreLevelRestriction")
    private val enchantlevel = GetConfig.main().getInt("Economy.Physical.Enchant.Level")
    private val nms = NMSUtils
    fun addEconomy(player: Player?, amount: Int?): ItemStack {
        val econ = ItemStack(Material.valueOf(itemtype), amount!!)
        val econ_meta = econ.itemMeta
        NMSUtils.setMaxStackSize(econ, 1)
        econ_meta!!.setDisplayName(Format.hex(Format.color(IridiumColorAPI.process(itemdisplayname))))
        if (!(nms.checkLegacyVersion(nms.getCleanServerVersion()))) {
            econ_meta.setCustomModelData(itemnbtvalue)
        }
        econ_meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        val lore = ArrayList<String?>()
        lore.add(Format.hex(Format.color(IridiumColorAPI.process(itemlore[0]))))
        lore.add(Format.hex(Format.color(IridiumColorAPI.process(itemlore[1]))))
        econ_meta.lore = lore
        econ.itemMeta = econ_meta
        return econ
    }

    fun addNativeEconomy(player: Player?): ItemStack {
        val econ = ItemStack(Material.valueOf(itemtype))
        val econ_meta = econ.itemMeta
        NMSUtils.setMaxStackSize(econ, 1)
        econ_meta!!.setDisplayName(Format.hex(Format.color(IridiumColorAPI.process(itemdisplayname))))
        if (!(nms.checkLegacyVersion(nms.getCleanServerVersion()))) {
            econ_meta.setCustomModelData(itemnbtvalue)
        }
        econ_meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        val lore = ArrayList<String?>()
        lore.add(Format.hex(Format.color(IridiumColorAPI.process(itemlore[0]))))
        lore.add(Format.hex(Format.color(IridiumColorAPI.process(itemlore[1]))))
        econ_meta.lore = lore
        econ.itemMeta = econ_meta
        return econ
    }

    fun removeEconomyFromHand(player: Player, amount: Int) {
        if(nms.checkLegacyVersion(nms.getCleanServerVersion())){
            val handItem = player.itemInHand
            if (handItem.amount > amount) {
                handItem.amount = handItem.amount - amount
                player.setItemInHand(handItem)
            } else {
                player.setItemInHand(ItemStack(Material.AIR))
            }
        } else {
            val handItem = player.inventory.itemInMainHand
            if (handItem.amount > amount) {
                handItem.amount = handItem.amount - amount
                player.inventory.setItemInMainHand(handItem)
            } else {
                player.inventory.setItemInMainHand(ItemStack(Material.AIR))
            }
        }
    }
}