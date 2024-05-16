package com.simsinfotekno.maghribmengaji.usecase

class RemoveDiacritics {

    /**
     * Remove harakats or diacritics from string
     * TODO: Move to use case
     */
    operator fun invoke(arabicString: String): String {
        val diacritics = listOf(
            '\u064B',
            '\u064C',
            '\u064D',
            '\u064E',
            '\u064F',
            '\u0650',
            '\u0651',
            '\u0652',
            '\u0670'
        )
        val builder = StringBuilder()
        arabicString.forEach { char ->
            if (!diacritics.contains(char)) {
                builder.append(char)
            }
        }
        return builder.toString()
    }
}