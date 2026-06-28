package com.example.fashgo.util

import java.text.NumberFormat
import java.util.Locale

object FormatUtils {
    private val formatter = NumberFormat.getNumberInstance(Locale("es", "CL"))

    fun formatPrice(amount: Double): String {
        return "$ ${formatter.format(amount)}"
    }
}
