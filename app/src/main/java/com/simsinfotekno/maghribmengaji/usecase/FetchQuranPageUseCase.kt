package com.simsinfotekno.maghribmengaji.usecase

import android.os.Handler
import android.os.Looper
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class FetchQuranPageUseCase(private val resultHandler: ResultHandler) {

    private val myExecutor = Executors.newSingleThreadExecutor()
    private val myHandler = Handler(Looper.getMainLooper())

    interface ResultHandler {
        fun onSuccess(result: String)
        fun onFailure(message: String)
    }

    operator fun invoke(page: Int) {
        myExecutor.execute {
            var result = ""
            var urlConnection: HttpURLConnection? = null
            try {
                val url = URL("https://api.alquran.cloud/v1/page/$page/quran-uthmani")
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "GET"
                val inputStream = urlConnection.inputStream
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    stringBuilder.append(line).append("\n")
                }
                bufferedReader.close()
                result = stringBuilder.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                urlConnection?.disconnect()
            }

            myHandler.post {
                if (result.isNotEmpty()) {
                    resultHandler.onSuccess(result)
                } else {
                    resultHandler.onFailure("Failed to fetch data")
                }
            }
        }
    }
}