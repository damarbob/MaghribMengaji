package com.simsinfotekno.maghribmengaji

import android.app.Activity
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection


/**
 * Created by suhasbachewar on 10/5/16.
 */
class OCRAsyncTask(
    private val mActivity: Activity,
    private val mApiKey: String,
    isOverlayRequired: Boolean,
    imageBase64: String,
    language: String,
    iOCRCallBack: IOCRCallBack
) :
    AsyncTask<Any?, Any?, Any?>() {
    var url = "https://api.ocr.space/parse/image" // OCR API Endpoints
    private var isOverlayRequired = false
    private val mImageBase64: String
    private val mLanguage: String
    private var mProgressDialog: ProgressDialog? = null
    private val mIOCRCallBack: IOCRCallBack

    init {
        this.isOverlayRequired = isOverlayRequired
        mImageBase64 = imageBase64
        mLanguage = language
        mIOCRCallBack = iOCRCallBack
    }

    override fun onPreExecute() {
        mProgressDialog = ProgressDialog(mActivity)
        mProgressDialog!!.setTitle("Wait while processing....")
        mProgressDialog!!.setCanceledOnTouchOutside(false)
        mProgressDialog!!.setCancelable(false)
        mProgressDialog!!.show()
        super.onPreExecute()
    }

    override fun doInBackground(params: Array<Any?>): String? {
        try {
            return sendPost(mApiKey, isOverlayRequired, mImageBase64, mLanguage)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
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
        postDataParams.put("apikey", apiKey) //TODO Add your Registered API key
        postDataParams.put("isOverlayRequired", isOverlayRequired)
        postDataParams.put("base64image", imageBase64)
        postDataParams.put("language", language)
//        postDataParams.put("filetype", "BMP")


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

    override fun onPostExecute(result: Any?) {
        super.onPostExecute(result)
        if (mProgressDialog != null && mProgressDialog!!.isShowing) mProgressDialog!!.dismiss()
        val response = result as String?
        mIOCRCallBack.getOCRCallBackResult(response)
        Log.d(TAG, response.toString())
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
        private val TAG = OCRAsyncTask::class.java.name
    }
}

