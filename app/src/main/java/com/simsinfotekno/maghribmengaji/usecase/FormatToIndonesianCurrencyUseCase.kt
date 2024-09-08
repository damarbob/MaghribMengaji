package com.simsinfotekno.maghribmengaji.usecase

import java.text.NumberFormat
import java.util.Locale

class FormatToIndonesianCurrencyUseCase {
    // Function to format the balance in Indonesian Rupiah
    operator fun invoke(balance: Int?): String {
        return NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(balance ?: 0)
    }
}