package com.simsinfotekno.maghribmengaji.model

data class CountryCode(val code: String, val name: String) {
    override fun toString(): String {
        return "$name ($code)"
    }
}
