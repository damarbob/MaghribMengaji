package com.simsinfotekno.maghribmengaji.usecase

import android.util.Log
import android.widget.ProgressBar
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection

class OCRAsyncTask {

    interface IOCRCallBack {
        fun getOCRCallBackResult(response: String?)
        fun onOCRFailure(exception: Exception)
    }

    private lateinit var imageBase64: String
    private lateinit var language: String
    private lateinit var iOCRCallBack: IOCRCallBack
    private lateinit var progressBar: CircularProgressIndicator
    private val mApiKey = "K88528569888957"
    private val isOverlayRequired = false
    private val url = "https://api.ocr.space/parse/image" // OCR API Endpoints
    private lateinit var mProgressBar: CircularProgressIndicator

    operator fun invoke(
        imageBase64: String,
        language: String,
        iOCRCallBack: IOCRCallBack,
        progressBar: CircularProgressIndicator,
        lifecycleScope: LifecycleCoroutineScope
    ) {
        this.imageBase64 = imageBase64
        this.language = language
        this.iOCRCallBack = iOCRCallBack
        this.progressBar = progressBar

        lifecycleScope.launch {
            executeAsyncTask()
        }
    }

    suspend fun executeAsyncTask() {
        mProgressBar = progressBar
        try {
            mProgressBar.visibility = ProgressBar.VISIBLE

            val response = withContext(Dispatchers.IO) {
                sendPost()
            }

            mProgressBar.visibility = ProgressBar.GONE
            iOCRCallBack.getOCRCallBackResult(response)
            Log.d(TAG, response)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    private fun sendPost(): String {
        val obj = URL(url) // OCR API Endpoints
        val con = obj.openConnection() as HttpsURLConnection

        //add request header
        con.requestMethod = "POST"
        con.setRequestProperty("User-Agent", "Mozilla/5.0")
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5")
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        val postDataParams = JSONObject()
        postDataParams.put("apikey", mApiKey)
        postDataParams.put("isOverlayRequired", isOverlayRequired)
        postDataParams.put("base64image", imageBase64)
        postDataParams.put("language", language)

        // Send post request
        con.doOutput = true
        val wr = DataOutputStream(con.outputStream)
        wr.writeBytes(getPostDataString(postDataParams))
        wr.flush()
        wr.close()
        val `in` = BufferedReader(InputStreamReader(con.inputStream))
        var inputLine: String?
        val response = StringBuffer()
        while (`in`.readLine().also { inputLine = it } != null) {
            response.append(inputLine)
        }
        `in`.close()

        // Return result
        return response.toString()
    }

    @Throws(Exception::class)
    fun getPostDataString(params: JSONObject): String {
        val result = StringBuilder()
        var first = true
        val itr = params.keys()
        while (itr.hasNext()) {
            val key = itr.next()
            val value = params[key]
            if (first) first = false else result.append("&")
            result.append(URLEncoder.encode(key, "UTF-8"))
            result.append("=")
            result.append(URLEncoder.encode(value.toString(), "UTF-8"))
        }
        return result.toString()
    }

    companion object {
        private val TAG = OCRAsyncTask::class.simpleName
    }

    /*private val mApiKey = "K88528569888957"
    private val isOverlayRequired = false
    private val url = "https://api.ocr.space/parse/image" // OCR API Endpoints

    suspend fun execute(
        imageBase64: String,
        language: String,
        callback: IOCRCallBack
    ) {
        try {
            val response = withContext(Dispatchers.IO) {
                sendPost(imageBase64, language)
            }
            callback.getOCRCallBackResult(response)
        } catch (e: Exception) {
            callback.onOCRFailure(e)
        }
    }

    @Throws(Exception::class)
    private fun sendPost(imageBase64: String, language: String): String {
        val obj = URL(url) // OCR API Endpoints
        val con = obj.openConnection() as HttpsURLConnection

        // Add request header
        con.requestMethod = "POST"
        con.setRequestProperty("User-Agent", "Mozilla/5.0")
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5")
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        val postDataParams = JSONObject()
        postDataParams.put("apikey", mApiKey)
        postDataParams.put("isOverlayRequired", isOverlayRequired)
        postDataParams.put("base64image", imageBase64)
        postDataParams.put("language", language)

        // Send post request
        con.doOutput = true
        val wr = DataOutputStream(con.outputStream)
        wr.writeBytes(getPostDataString(postDataParams))
        wr.flush()
        wr.close()
        val `in` = BufferedReader(InputStreamReader(con.inputStream))
        var inputLine: String?
        val response = StringBuffer()
        while (`in`.readLine().also { inputLine = it } != null) {
            response.append(inputLine)
        }
        `in`.close()

        // Return result
        return response.toString()
    }

    @Throws(Exception::class)
    private fun getPostDataString(params: JSONObject): String {
        val result = StringBuilder()
        var first = true
        val itr = params.keys()
        while (itr.hasNext()) {
            val key = itr.next()
            val value = params[key]
            if (first) first = false else result.append("&")
            result.append(URLEncoder.encode(key, "UTF-8"))
            result.append("=")
            result.append(URLEncoder.encode(value.toString(), "UTF-8"))
        }
        return result.toString()
    }

    companion object {
        private val TAG = OCRAsyncTask::class.simpleName
    }*/
}
