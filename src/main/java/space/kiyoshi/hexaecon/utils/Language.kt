/*
 * Copyright (c) 2023. KiyoshiDevelopment
 *   All rights reserved.
 */

@file:Suppress("SpellCheckingInspection")

package space.kiyoshi.hexaecon.utils

import space.kiyoshi.hexaecon.HexaEcon.Companion.plugin


object Language {

    /********************************* Prefix *********************************/

    private fun getPrefix(): String? {
        return plugin.getLanguages().getString("Language.Prefix")
    }

    /********************************* Messages *********************************/

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

    fun bankAmountOthers(): String {
        return getPrefix() + plugin.getLanguages().getString("Language.BankAmountOthers")
    }

    fun genericEarn(): String {
        return getPrefix() + plugin.getLanguages().getString("Language.GenericEarn")
    }

    fun generateToOther(): String {
        return getPrefix() + plugin.getLanguages().getString("Language.GenerateToOther")
    }

    fun setToOther(): String {
        return getPrefix() + plugin.getLanguages().getString("Language.SetToOther")
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

    fun playerpayed(): String {
        return getPrefix() + plugin.getLanguages().getString("Language.PlayerPayed")
    }

    fun paymentrecived(): String {
        return getPrefix() + plugin.getLanguages().getString("Language.PlayerPaymentRecived")
    }

    fun cannotpayself(): String {
        return getPrefix() + plugin.getLanguages().getString("Language.CannotPaySelf")
    }

    fun removedEconFromPlayer(): String {
        return getPrefix() + plugin.getLanguages().getString("Language.RemovedEconFromPlayer")
    }

    fun cannotRemoveEconFromPlayer(): String {
        return getPrefix() + plugin.getLanguages().getString("Language.CannotRemoveEconFromPlayer")
    }

    fun usageFormat(): String {
        return getPrefix() + plugin.getLanguages().getString("Language.UsageFormat")
    }

    /********************************* Usages *********************************/

    fun usageConvertDeposit(): String? {
        return plugin.getLanguages().getString("Usages.EconConvertDeposit")
    }

    fun usagePayment(): String? {
        return plugin.getLanguages().getString("Usages.Pay")
    }

    fun configurationReloaded(): String {
        return getPrefix() + plugin.getLanguages().getString("Language.ConfigurationReloaded")
    }

    /********************************* Formatted *********************************/

    fun formattedAmount(): String? {
        return plugin.getLanguages().getString("Formatted.Amount")
    }
}
