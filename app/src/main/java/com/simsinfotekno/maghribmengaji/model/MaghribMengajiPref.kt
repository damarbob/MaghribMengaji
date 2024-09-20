package com.simsinfotekno.maghribmengaji.model

import android.app.Activity
import android.content.Context

class MaghribMengajiPref {

    companion object {

        val USER_NAME_KEY = "UserNameKey"
        val NOTIF_ENABLED_KEY = "NotificationsEnabled"
        const val ML_KIT_SCANNER_ENABLED_KEY = "MLKitScannerEnabled"
        const val QR_CODE_ENABLED_KEY = "QRCodeEnabled"
        const val QR_CODE_FAILURE_COUNTER = "QRCodeFailureCounter"
        const val QURAN_VOLUME_INFAQ = "QuranVolumeInfaq"

        fun readString(activity: Activity, key: String?): String? {
            return activity.getPreferences(Context.MODE_PRIVATE).getString(key, null)
        }

        fun readString(activity: Activity, key: String?, defaultValue: String?): String? {
            return activity.getPreferences(Context.MODE_PRIVATE).getString(key, defaultValue)
        }

        fun saveString(activity: Activity, key: String?, value: String?) {
            val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString(key, value)
            editor.apply()
        }

        fun readBoolean(activity: Activity, key: String, defaultValue: Boolean?): Boolean {
            return activity.getPreferences(Context.MODE_PRIVATE).getBoolean(key,
                defaultValue ?: false
            )
        }

        fun saveBoolean(activity: Activity, key: String?, value: Boolean) {
            val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean(key, value)
            editor.apply()
        }

        fun readInt(activity: Activity, key: String, defaultValue: Int?): Int {
            return activity.getPreferences(Context.MODE_PRIVATE).getInt(key,
                defaultValue ?: 0
            )
        }

        fun saveInt(activity: Activity, key: String?, value: Int) {
            val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putInt(key, value)
            editor.apply()
        }

        fun readSharedInt(context: Context, sharedPreference: String, key: String, defaultValue: Int?): Int {
            return context.getSharedPreferences(sharedPreference, Context.MODE_PRIVATE).getInt(key, defaultValue ?: 0)
        }

        fun saveSharedInt(context: Context, sharedPreference: String, key: String, value: Int) {
            context.getSharedPreferences(sharedPreference, Context.MODE_PRIVATE).edit().apply{
                putInt(key, value)
                apply()
            }
        }

        fun readSharedLong(context: Context, sharedPreference: String, key: String, defaultValue: Long?): Long {
            return context.getSharedPreferences(sharedPreference, Context.MODE_PRIVATE).getLong(key, defaultValue ?: 0L)
        }

        fun saveSharedLong(context: Context, sharedPreference: String, key: String, value: Long) {
            context.getSharedPreferences(sharedPreference, Context.MODE_PRIVATE).edit().apply{
                putLong(key, value)
                apply()
            }
        }

    }
}