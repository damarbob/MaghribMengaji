package com.simsinfotekno.maghribmengaji.usecase

import org.json.JSONObject

class ExtractTextFromQuranAPIJSON {

    /**
     * Extract text from Quran API JSON
     */
    operator fun invoke(jsonString: String): String {
        val jsonObject = JSONObject(jsonString)
        val ayahsArray = jsonObject.getJSONObject("data").getJSONArray("ayahs")
        val stringBuilder = StringBuilder()
        for (i in 0 until ayahsArray.length()) {
            val ayahObject = ayahsArray.getJSONObject(i)
            val ayahText = ayahObject.getString("text")
            stringBuilder.append(ayahText).append("\n")
        }
        return stringBuilder.toString()
    }
}