package com.simsinfotekno.maghribmengaji.model

import android.app.Activity
import android.content.Context

class MaghribMengajiPref {

    companion object {

        val USER_NAME_KEY = "UserNameKey"
        val NOTIF_ENABLED_KEY = "NotificationsEnabled"
        val ML_KIT_SCANNER_ENABLED_KEY = "MLKitScannerEnabled"

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

    }
}