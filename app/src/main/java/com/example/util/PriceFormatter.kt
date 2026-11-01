package com.example.util

import java.text.NumberFormat
import java.util.Locale

object PriceFormatter {
    private val trLocale = Locale("tr", "TR")

    fun format(price: Double): String {
        val format = NumberFormat.getCurrencyInstance(trLocale)
        // Ensure standard display with ₺
        return format.format(price).replace("TRY", "₺").replace("TL", "₺").trim()
    }

    fun format(priceStr: String?): String {
        if (priceStr.isNullOrBlank()) return "₺0,00"
        val clean = priceStr.replace(Regex("[^0-9.,]"), "").replace(",", ".")
        val doubleVal = clean.toDoubleOrNull() ?: 0.0
        return format(doubleVal)
    }

    fun formatMonthly(priceStr: String?): String {
        val formatted = format(priceStr)
        return "$formatted / ay"
    }

    fun formatYearly(priceStr: String?): String {
        val formatted = format(priceStr)
        return "$formatted / yıl"
    }
}
