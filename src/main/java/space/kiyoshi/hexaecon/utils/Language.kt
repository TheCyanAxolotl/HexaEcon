@file:Suppress("SpellCheckingInspection")

package space.kiyoshi.hexaecon.utils

import space.kiyoshi.hexaecon.HexaEcon.Companion.plugin


object Language {

    private fun getPrefix(): String? {
        return plugin.getLanguages().getString("Language.Prefix")
    }

    fun isConsolePlayer(): String {
        return getPrefix() + plugin.getLanguages().getString("Language.IsConsolePlayer")
    }

    fun accessDenied(): String {
        return getPrefix() + plugin.getLanguages().getString("Language.AccessDenied")
    }

    fun playerNotFound(): String {
        return getPrefix() + plugin.getLanguages().getString("Language.PlayerNotFound")
    }

    fun bankAmount(): String {
        return getPrefix() + plugin.getLanguages().getString("Language.BankAmount")
    }

    fun genericEarn(): String {
        return getPrefix() + plugin.getLanguages().getString("Language.GenericEarn")
    }

    fun generateToOther(): String {
        return getPrefix() + plugin.getLanguages().getString("Language.GenerateToOther")
    }

    fun invalidAmount(): String {
        return getPrefix() + plugin.getLanguages().getString("Language.InvalidAmount")
    }

    fun walletWithdrawAmount(): String {
        return getPrefix() + plugin.getLanguages().getString("Language.WalletWithdrawAmount")
    }

    fun walletWithdrawConverted(): String {
        return getPrefix() + plugin.getLanguages().getString("Language.WalletWithdrawConverted")
    }

    fun walletWithdrawRemainingAmount(): String {
        return getPrefix() + plugin.getLanguages().getString("Language.WalletWithdrawRemaningAmount")
    }

    fun walletWithdrawNoEnoughAmount(): String {
        return getPrefix() + plugin.getLanguages().getString("Language.WalletWithdrawNoEnoughAmount")
    }

    fun usageFormat(): String {
        return getPrefix() + plugin.getLanguages().getString("Language.UsageFormat")
    }

    // Usages

    fun usageConvertDeposit(): String? {
        return plugin.getLanguages().getString("Usages.EconConvertDeposit")
    }

    fun configurationReloaded(): String {
        return getPrefix() + plugin.getLanguages().getString("Language.ConfigurationReloaded")
    }

    // Formatted

    fun formattedAmount(): String? {
        return plugin.getLanguages().getString("Formatted.Amount")
    }
}
