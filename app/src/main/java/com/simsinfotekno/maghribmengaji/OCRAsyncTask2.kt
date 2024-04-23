package com.simsinfotekno.maghribmengaji

import android.app.Activity
import android.app.ProgressDialog
import android.util.Log
import android.widget.ProgressBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class OCRAsyncTask2(
    private val mActivity: Activity,
    private val mApiKey: String,
    private val isOverlayRequired: Boolean,
    private val imageBase64: String,
    private val language: String,
    private val iOCRCallBack: IOCRCallBack
) {
    private val url = "https://api.ocr.space/parse/image" // OCR API Endpoints
    private var mProgressDialog: ProgressDialog? = null
    private lateinit var mProgressBar: ProgressBar

    private val executor = Executors.newSingleThreadExecutor()

    suspend fun executeAsyncTask(progressBar: ProgressBar) {
        mProgressBar = progressBar
        try {
//            mProgressDialog = ProgressDialog(mActivity)
//            mProgressDialog!!.setTitle("Wait while processing....")
//            mProgressDialog!!.setCanceledOnTouchOutside(false)
//            mProgressDialog!!.setCancelable(false)
//            mProgressDialog!!.show()

            mProgressBar.visibility = ProgressBar.VISIBLE



            val response = withContext(Dispatchers.IO) {
                sendPost(mApiKey, isOverlayRequired, imageBase64, language)
            }

//            mProgressDialog?.dismiss()
            mProgressBar.visibility = ProgressBar.GONE
            iOCRCallBack.getOCRCallBackResult(response)
            Log.d(TAG, response)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    private fun sendPost(
        apiKey: String,
        isOverlayRequired: Boolean,
        imageBase64: String,
        language: String
    ): String {
        val obj = URL(url) // OCR API Endpoints
        val con = obj.openConnection() as HttpsURLConnection

        //add request header
        con.requestMethod = "POST"
        con.setRequestProperty("User-Agent", "Mozilla/5.0")
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5")
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        val postDataParams = JSONObject()
        postDataParams.put("apikey", apiKey)
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

        //return result
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
        private const val TAG = "OCRAsyncTask"
    }
}
