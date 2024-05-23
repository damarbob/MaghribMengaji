package com.simsinfotekno.maghribmengaji.usecase

import org.json.JSONObject

class ExtractTextFromOCRApiJSON {
    /**
     * Extract text from OCR API JSON
     */
    operator fun invoke(jsonString: String): String? {
        try {
            val jsonObject = JSONObject(jsonString)
            val parsedResults = jsonObject.getJSONArray("ParsedResults")
            if (parsedResults.length() > 0) {
                val firstResult = parsedResults.getJSONObject(0)
                return firstResult.optString("ParsedText")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}